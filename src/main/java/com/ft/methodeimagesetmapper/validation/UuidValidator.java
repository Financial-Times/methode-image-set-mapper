package com.ft.methodeimagesetmapper.validation;


import com.ft.methodeimagesetmapper.exception.ValidationException;

import java.util.UUID;

public class UuidValidator {

    public void validate(String uuid) throws ValidationException {
        try {
            final UUID parsedUuid = UUID.fromString(uuid);
            if (!parsedUuid.toString().equals(uuid.toLowerCase())) {
                throw new ValidationException(String.format("Invalid UUID: [%s], does not conform to RFC 4122", uuid));
            }
        } catch (final IllegalArgumentException | NullPointerException e) {
            throw new ValidationException(String.format("Invalid UUID: [%s], does not conform to RFC 4122", uuid));
        }
    }

}
