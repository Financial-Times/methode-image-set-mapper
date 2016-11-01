package com.ft.methodeimagesetmapper.validation;

import com.ft.methodeimagesetmapper.model.EomFile;
import com.ft.methodeimagesetmapper.model.EomFileContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublishingValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishingValidator.class);

    public boolean isValidForPublishing(EomFile eomFile) {
        String contentType = eomFile.getType();
        return EomFileContentType.IMAGE.getContentType().equals(contentType) && isImageValidForPublishingToSemanticStack(eomFile);
    }

    private boolean isImageValidForPublishingToSemanticStack(EomFile eomFile) {
        return !missingImageBytes(eomFile);
    }

    private boolean missingImageBytes(EomFile eomFile) {
        byte[] value = eomFile.getValue();
        if (value == null || eomFile.getValue().length == 0) {
            LOGGER.info(String.format("Image [%s] has no image bytes.", eomFile.getUuid()));
            return true;
        }
        return false;
    }
}
