package com.ft.methodeimagesetmapper.resource;

import com.ft.api.jaxrs.errors.ClientError;
import com.ft.api.jaxrs.errors.ServerError;
import com.ft.api.util.transactionid.TransactionIdUtils;
import com.ft.content.model.Content;
import com.ft.methodeimagesetmapper.exception.ContentMapperException;
import com.ft.methodeimagesetmapper.exception.MethodeContentNotSupportedException;
import com.ft.methodeimagesetmapper.exception.TransformationException;
import com.ft.methodeimagesetmapper.exception.ValidationException;
import com.ft.methodeimagesetmapper.messaging.MessageProducingContentMapper;
import com.ft.methodeimagesetmapper.model.EomFile;
import com.ft.methodeimagesetmapper.service.MethodeImageSetMapper;
import com.ft.methodeimagesetmapper.validation.PublishingValidator;
import com.ft.uuidutils.DeriveUUID;
import com.ft.uuidutils.DeriveUUID.Salts;
import com.ft.uuidutils.UUIDValidation;
import java.util.Date;
import java.util.UUID;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class MethodeImageSetResource {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodeImageSetResource.class);

  private static final String CHARSET_UTF_8 = ";charset=utf-8";

  private static final String CONTENT_TYPE_NOT_SUPPORTED = "Unsupported type - not an image set.";
  private static final String CONTENT_CANNOT_BE_MAPPED = "Content cannot be mapped.";
  private static final String UNABLE_TO_WRITE_JSON_MESSAGE = "Unable to write JSON for message";

  private final MethodeImageSetMapper methodeImageSetMapper;
  private final MessageProducingContentMapper messageProducingContentMapper;
  private final PublishingValidator publishingValidator;


  public MethodeImageSetResource(MethodeImageSetMapper methodeImageSetMapper,
      MessageProducingContentMapper messageProducingContentMapper,
      PublishingValidator publishingValidator) {
    this.methodeImageSetMapper = methodeImageSetMapper;
    this.messageProducingContentMapper = messageProducingContentMapper;
    this.publishingValidator = publishingValidator;
  }

  @POST
  @Path("/map")
  @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
  public final Content mapImageSet(EomFile methodeContent, @Context HttpHeaders httpHeaders) {
    LOGGER.info("Mapping content with uuid [{}]", methodeContent.getUuid());
    return getModelAndHandleExceptions(methodeContent, httpHeaders, (transactionId) ->
        methodeImageSetMapper
            .mapImageSet(methodeContent.getUuid(), methodeContent, transactionId, new Date()));
  }

  @POST
  @Path("/map-uuid-gen")
  @Produces(MediaType.APPLICATION_JSON + CHARSET_UTF_8)
  public final Content mapImageSetWithUUIDGen(EomFile methodeContent, @Context HttpHeaders httpHeaders) {
    if (!publishingValidator.isValidForPublishing(methodeContent)) {
      LOGGER.info("Skip message [{}] of type [{}]", methodeContent.getUuid(), methodeContent.getType());
      throw ClientError.status(422).error(CONTENT_CANNOT_BE_MAPPED).exception();
    } 
      
    final UUID methodeUuid = UUID.fromString(methodeContent.getUuid());
    final UUID imageSetUuid = DeriveUUID.with(Salts.IMAGE_SET).from(methodeUuid);
    final String uuid = imageSetUuid.toString();    
    LOGGER.info("Importing content [{}] of type [{}] as image set [{}].",
      methodeContent.getUuid(), methodeContent.getType(), uuid);
   
    return getModelAndHandleExceptions(methodeContent, httpHeaders, (transactionId) ->
        methodeImageSetMapper
            .mapImageSet(uuid, methodeContent, transactionId, new Date()));
  }

  @POST
  @Path("/ingest")
  public final void ingestImageSet(EomFile methodeContent, @Context HttpHeaders httpHeaders) {
    LOGGER.info("Ingesting content with uuid [{}]", methodeContent.getUuid());
    getModelAndHandleExceptions(methodeContent, httpHeaders, (transactionId) ->
        messageProducingContentMapper
            .mapImageSet(methodeContent.getUuid(), methodeContent, transactionId, new Date()));
  }

  private Content getModelAndHandleExceptions(EomFile methodeContent, HttpHeaders headers,
      Action<Content> getContentModel) {
    final String transactionId = TransactionIdUtils.getTransactionIdOrDie(headers);
    try {
      UUIDValidation.of(methodeContent.getUuid());
      if (publishingValidator.isValidForPublishing(methodeContent)) {
        return getContentModel.perform(transactionId);
      }
      throw new TransformationException();
    } catch (IllegalArgumentException | ValidationException e) {
      throw ClientError.status(422).error(e.getMessage()).exception(e);
    } catch (MethodeContentNotSupportedException e) {
      throw ClientError.status(422).error(CONTENT_TYPE_NOT_SUPPORTED).exception(e);
    } catch (TransformationException e) {
      throw ClientError.status(422).error(CONTENT_CANNOT_BE_MAPPED).exception(e);
    } catch (ContentMapperException e) {
      throw ServerError.status(500).error(UNABLE_TO_WRITE_JSON_MESSAGE).exception(e);
    }
  }

  private interface Action<T> {

    T perform(String transactionId);
  }
}
