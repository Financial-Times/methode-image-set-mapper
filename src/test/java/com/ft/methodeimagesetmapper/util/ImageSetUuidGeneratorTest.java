package com.ft.methodeimagesetmapper.util;

import org.junit.Test;

import java.util.UUID;

import static com.ft.methodeimagesetmapper.util.ImageSetUuidGenerator.fromImageUuid;
import static com.ft.methodeimagesetmapper.util.ImageSetUuidGenerator.originalImageUuid;
import static org.junit.Assert.assertEquals;

public class ImageSetUuidGeneratorTest {

    private static final String ORIGINAL_UUID = "d7625378-d4cd-11e2-bce1-002128161462";
    private static final String IMAGE_SET_UUID = fromImageUuid(java.util.UUID.fromString(ORIGINAL_UUID)).toString();

    @Test
    public void testOriginalImageUuid() {
        assertEquals(originalImageUuid(UUID.fromString(IMAGE_SET_UUID)).toString(), ORIGINAL_UUID);
    }
}
