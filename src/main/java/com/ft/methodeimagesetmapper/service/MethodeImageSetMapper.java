package com.ft.methodeimagesetmapper.service;

import com.ft.content.model.Content;
import com.ft.content.model.Copyright;
import com.ft.content.model.Identifier;
import com.ft.content.model.Member;
import com.ft.methodeimagesetmapper.exception.MethodeContentNotSupportedException;
import com.ft.methodeimagesetmapper.exception.TransformationException;
import com.ft.methodeimagesetmapper.model.EomFile;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSortedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class MethodeImageSetMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodeImageSetMapper.class);
    private static final String IMAGE_TYPE = "Image";
    private static final String MEDIATYPE_PREFIX = "image/";
    private static final String DEFAULT_MEDIATYPE = "image/jpeg";
    private static final String SOURCE_METHODE = "http://api.ft.com/system/FTCOM-METHODE";
    private static final String FORMAT_UNSUPPORTED = "%s is not an %s.";
    private static final String DATE_FORMAT = "yyyyMMddHHmmss";

    public Content mapImageSet(String uuid, EomFile eomFile, String transactionId, Date lastModifiedDate) {
        return transformAndHandleExceptions(eomFile, () -> transformEomFileToContent(uuid, eomFile, transactionId, lastModifiedDate)
                .withMembers(ImmutableSortedSet.of(new Member(eomFile.getUuid())))
                .withPixelHeight(null)
                .withPixelWidth(null)
                .withCopyright(null)
                .build());
    }

    Content transformAndHandleExceptions(EomFile eomFile, Action<Content> transformAction) {
        if (!isEomTypeSupported(eomFile)) {
            throw new MethodeContentNotSupportedException(String.format(FORMAT_UNSUPPORTED, eomFile.getUuid(), IMAGE_TYPE));
        }
        try {
            return transformAction.perform();
        } catch (ParserConfigurationException | XPathExpressionException | IOException e) {
            throw new TransformationException(e);
        }
    }

    private boolean isEomTypeSupported(final EomFile eomFile) {
        return IMAGE_TYPE.equals(eomFile.getType());
    }

    private Content.Builder transformEomFileToContent(final String uuid, final EomFile eomFile, final String transactionId, final Date lastModifiedDate)
            throws IOException, XPathExpressionException, ParserConfigurationException {
        final DocumentBuilder documentBuilder = getDocumentBuilder();
        final XPath xpath = XPathFactory.newInstance().newXPath();
        String caption = null;
        String altText = null;
        String copyrightNotice = null;
        try {
            final Document attributesDocument = documentBuilder.parse(new InputSource(new StringReader(eomFile.getAttributes())));
            caption = xpath.evaluate("/meta/picture/web_information/caption", attributesDocument);
            altText = xpath.evaluate("/meta/picture/web_information/alt_tag", attributesDocument);

            String manualCopyright, onlineCopyright;

            onlineCopyright = xpath.evaluate("/meta/picture/web_information/online-source", attributesDocument);
            manualCopyright = xpath.evaluate("/meta/picture/web_information/manual-source", attributesDocument);

            copyrightNotice = firstOf(onlineCopyright, manualCopyright);

            if (copyrightNotice != null && !copyrightNotice.contains("©")) {
                copyrightNotice = "© " + copyrightNotice;
            }

        } catch (SAXException ex) {
            LOGGER.warn("Failed retrieving attributes XML of image {}. Moving on without adding relevant properties.", eomFile.getUuid(), ex);
        }
        Integer width = null;
        Integer height = null;
        String mediaType = DEFAULT_MEDIATYPE;
        try {
            final Document systemAttributesDocument = documentBuilder.parse(new InputSource(new StringReader(eomFile.getSystemAttributes())));
            width = transformWidth(eomFile.getUuid(), xpath.evaluate("/props/imageInfo/width", systemAttributesDocument));
            height = transformHeight(eomFile.getUuid(), xpath.evaluate("/props/imageInfo/height", systemAttributesDocument));
            final String mediaTypeSuffix = xpath.evaluate("/props/imageInfo/fileType", systemAttributesDocument);
            if (!mediaTypeSuffix.isEmpty()) {
                mediaType = MEDIATYPE_PREFIX + mediaTypeSuffix.toLowerCase();
            }
        } catch (SAXException ex) {
            LOGGER.warn("Failed retrieving system attributes XML of image {}. Moving on without adding relevant properties.", eomFile.getUuid(), ex);
        }

        Date publishDate = null;
        try {
            final Document usageTicketsDocument = documentBuilder.parse(new InputSource(new StringReader(eomFile.getUsageTickets())));
            publishDate = transformDate(eomFile.getUuid(), xpath.evaluate("/tl/t[tp = 'web_publication'][count(/tl/t[tp = 'web_publication'])]/cd", usageTicketsDocument));
        } catch (SAXException ex) {
            LOGGER.warn("Failed retrieving usage tickets of image {}. Moving on without adding relevant properties.", eomFile.getUuid(), ex);
        }

        return Content.builder()
                .withUuid(UUID.fromString(uuid))
                .withIdentifiers(ImmutableSortedSet.of(new Identifier(SOURCE_METHODE, uuid)))
                .withDescription(altText)
                .withTitle(caption)
                .withPixelWidth(width)
                .withPixelHeight(height)
                .withPublishedDate(publishDate)
                .withPublishReference(transactionId)
                .withLastModified(lastModifiedDate)
                .withCopyright(Copyright.noticeOnly(copyrightNotice))
                .withMediaType(mediaType);
    }

    private String firstOf(String... strings) {
        for (String raw : strings) {
            if (!Strings.isNullOrEmpty(raw)) {
                String trimmed = raw.trim();
                if (!Strings.isNullOrEmpty(trimmed)) {
                    return trimmed;
                }
            }
        }
        return null;
    }

    private Integer transformWidth(final String uuid, final String widthString) {
        try {
            return Integer.parseInt(widthString);
        } catch (NumberFormatException ex) {
            LOGGER.warn("Width was not supplied by the source for uuid {} and width '{}'.", uuid, widthString);
        }
        return null;
    }

    private Integer transformHeight(final String uuid, final String heightString) {
        try {
            return Integer.parseInt(heightString);
        } catch (NumberFormatException ex) {
            LOGGER.warn("Height couldn't be converted to an integer for uuid {} and height '{}'.", uuid, heightString);
        }
        return null;
    }

    private Date transformDate(final String uuid, final String dateString) {
        final DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        try {
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            return dateFormat.parse(dateString);
        } catch (ParseException ex) {
            LOGGER.warn("Date couldn't be parsed for uuid {} and raw value '{}'.", uuid, dateString);
        }
        return null;
    }

    private DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return documentBuilderFactory.newDocumentBuilder();
    }

    interface Action<T> {
        T perform() throws ParserConfigurationException, XPathExpressionException, IOException;
    }
}
