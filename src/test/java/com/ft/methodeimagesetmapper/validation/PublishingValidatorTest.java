package com.ft.methodeimagesetmapper.validation;

import com.ft.methodeimagesetmapper.model.EomFile;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class PublishingValidatorTest {

    private static final String UUID = "d7625378-d4cd-11e2-bce1-002128161462";

    private PublishingValidator publishingValidator;

    @Before
    public void setUp() {
        publishingValidator = new PublishingValidator();
    }

    @Test
    public void testIsValidForPublishing() throws Exception {
        EomFile methodeContent = createSampleMethodeImage("Image", "Sample Image Body".getBytes());
        assertTrue(publishingValidator.isValidForPublishing(methodeContent));
    }

    @Test
    public void testIsValidForPublishingEmptyBody() throws Exception {
        EomFile methodeContent = createSampleMethodeImage("Image", null);
        assertFalse(publishingValidator.isValidForPublishing(methodeContent));
    }

    @Test
    public void testIsValidForPublishingNotImage() throws Exception {
        EomFile methodeContent = createSampleMethodeImage("Content", "Sample Image Body".getBytes());
        assertFalse(publishingValidator.isValidForPublishing(methodeContent));
    }

    private EomFile createSampleMethodeImage(String type, byte[] imageBody) throws Exception {
        final String attributes = loadFile("sample-attributes.xml");
        final String systemAttributes = loadFile("sample-system-attributes.xml");
        final String usageTickets = loadFile("sample-usage-tickets.xml");
        return new EomFile(UUID, type, imageBody, attributes, "", systemAttributes, usageTickets, new Date());
    }

    private String loadFile(final String filename) throws Exception {
        URL resource = getClass().getClassLoader().getResource(filename);
        if (resource != null) {
            final URI uri = resource.toURI();
            return new String(Files.readAllBytes(Paths.get(uri)), "UTF-8");
        }
        return null;
    }
}
