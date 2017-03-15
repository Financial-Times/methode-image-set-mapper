package com.ft.methodeimagesetmapper.service;

import com.ft.content.model.Content;
import com.ft.content.model.Distribution;
import com.ft.content.model.Member;
import com.ft.methodeimagesetmapper.exception.MethodeContentNotSupportedException;
import com.ft.methodeimagesetmapper.exception.TransformationException;
import com.ft.methodeimagesetmapper.model.EomFile;
import com.google.common.collect.ImmutableSortedSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import static com.ft.methodeimagesetmapper.util.ImageSetUuidGenerator.fromImageUuid;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MethodeImageModelMapperTest {

    private static final String UUID = "d7625378-d4cd-11e2-bce1-002128161462";
    private static final String METHODE_IDENTIFIER_AUTHORITY = "http://api.ft.com/system/FTCOM-METHODE";
    private static final String TRANSACTION_ID = "tid_ptvw9xpnhv";
    private static final Date LAST_MODIFIED_DATE = new Date(300L);
    private static final String FORMAT_UNSUPPORTED = "%s is not an %s.";
    private static final String IMAGE_SET_UUID = fromImageUuid(java.util.UUID.fromString(UUID)).toString();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private final MethodeImageSetMapper methodeImageSetMapper = new MethodeImageSetMapper();

    @Test
    public void testTransformImageSetThrowsIfTypeNotImage() {
        final EomFile eomFile = new EomFile(UUID, "article", null, "attributes", "workflow", "sysattributes", "usageTickets", LAST_MODIFIED_DATE);
        exception.expect(MethodeContentNotSupportedException.class);
        exception.expectMessage(String.format(FORMAT_UNSUPPORTED, UUID, "Image"));

        methodeImageSetMapper.mapImageSet(IMAGE_SET_UUID, eomFile, TRANSACTION_ID, LAST_MODIFIED_DATE);
    }

    public EomFile createSampleMethodeImage() throws Exception {
        final String attributes = loadFile("sample-attributes.xml");
        final String systemAttributes = loadFile("sample-system-attributes.xml");
        final String usageTickets = loadFile("sample-usage-tickets.xml");
        return new EomFile(UUID, "Image", null, attributes, "", systemAttributes, usageTickets, LAST_MODIFIED_DATE);
    }

    public String loadFile(final String filename) throws Exception {
        final URI uri = getClass().getClassLoader().getResource(filename).toURI();
        return new String(Files.readAllBytes(Paths.get(uri)), "UTF-8");
    }

    @Test
    public void shouldNotSupplyCopyrightForImageSets() throws Exception {
        final EomFile eomFile = createSampleMethodeImage();

        final Content content = methodeImageSetMapper.mapImageSet(IMAGE_SET_UUID, eomFile, TRANSACTION_ID, LAST_MODIFIED_DATE);

        assertThat(content.getCopyright(), nullValue());
    }

    @Test
    public void testTransformsImageSetCorrectly() throws Exception {
        final EomFile eomFile = createSampleMethodeImage();

        final Content content = methodeImageSetMapper.mapImageSet(IMAGE_SET_UUID, eomFile, TRANSACTION_ID, LAST_MODIFIED_DATE);

        assertThat(content.getUuid(), equalTo(IMAGE_SET_UUID));
        assertThat(content.getIdentifiers().first().getAuthority(), equalTo(METHODE_IDENTIFIER_AUTHORITY));
        assertThat(content.getIdentifiers().first().getIdentifierValue(), equalTo(IMAGE_SET_UUID));
        assertThat(content.getTitle(), equalTo("Fruits of the soul"));
        assertThat(content.getDescription(), equalTo("Picture with fruits"));
        assertThat(content.getMediaType(), equalTo("image/jpeg"));
        assertThat(content.getPublishedDate(), equalTo(new Date(1412088300000l)));
        assertThat(content.getMembers(), equalTo(ImmutableSortedSet.of(new Member(UUID))));
        assertThat(content.getPixelWidth(), nullValue());
        assertThat(content.getPixelHeight(), nullValue());
        assertThat(content.getPublishReference(), equalTo(TRANSACTION_ID));
        assertThat(content.getFirstPublishedDate(), equalTo(new Date(1412088300000l)));
        assertThat(content.getCanBeDistributed(), equalTo(Distribution.VERIFY));
    }

    @Test
    public void testTransformsImageSetCorrectlyIfWidthHeightDateIncorrect() throws Exception {
        final String attributes = loadFile("sample-attributes.xml");
        final String systemAttributes = loadFile("sample-system-attributes.xml")
                .replace("2048", "two thousand").replace("1152", "one thousand");
        final String usageTickets = loadFile("sample-usage-tickets.xml").replace("20140930144500", "my birthday");
        final EomFile eomFile = new EomFile(UUID, "Image", null, attributes, "", systemAttributes, usageTickets, LAST_MODIFIED_DATE);

        final Content content = methodeImageSetMapper.mapImageSet(IMAGE_SET_UUID, eomFile, TRANSACTION_ID, LAST_MODIFIED_DATE);

        assertThat(content.getUuid(), equalTo(IMAGE_SET_UUID));
        assertThat(content.getIdentifiers().first().getAuthority(), equalTo(METHODE_IDENTIFIER_AUTHORITY));
        assertThat(content.getIdentifiers().first().getIdentifierValue(), equalTo(IMAGE_SET_UUID));
        assertThat(content.getTitle(), equalTo("Fruits of the soul"));
        assertThat(content.getDescription(), equalTo("Picture with fruits"));
        assertThat(content.getMediaType(), equalTo("image/jpeg"));
        assertThat(content.getMembers(), equalTo(ImmutableSortedSet.of(new Member(UUID))));
        assertThat(content.getPublishedDate(), nullValue());
        assertThat(content.getPixelWidth(), nullValue());
        assertThat(content.getPixelHeight(), nullValue());
        assertThat(content.getInternalBinaryUrl(), nullValue());
        assertThat(content.getPublishReference(), equalTo(TRANSACTION_ID));
        assertThat(content.getFirstPublishedDate(), nullValue());
        assertThat(content.getCanBeDistributed(), equalTo(Distribution.VERIFY));
    }

    @Test
    public void testTransformImageSetNoExceptionIfUnrelatedXml() throws Exception {
        final String attributes = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                "<!DOCTYPE meta SYSTEM \"/SysConfig/Classify/FTImages/classify.dtd\">" +
                "<meta>empty</meta>";
        final String systemAttributes = "<props>empty as well</props>";
        final String usageTickets = "<html>empty as well</html>";
        final EomFile eomFile = new EomFile(UUID, "Image", null, attributes, "", systemAttributes, usageTickets, LAST_MODIFIED_DATE);

        final Content content = methodeImageSetMapper.mapImageSet(IMAGE_SET_UUID, eomFile, TRANSACTION_ID, LAST_MODIFIED_DATE);

        assertThat(content.getUuid(), equalTo(IMAGE_SET_UUID));
        assertThat(content.getIdentifiers().first().getAuthority(), equalTo(METHODE_IDENTIFIER_AUTHORITY));
        assertThat(content.getIdentifiers().first().getIdentifierValue(), equalTo(IMAGE_SET_UUID));
        assertThat(content.getMembers(), equalTo(ImmutableSortedSet.of(new Member(UUID))));
        assertThat(content.getMediaType(), equalTo("image/jpeg"));
        assertThat(content.getPublishReference(), equalTo(TRANSACTION_ID));
    }

    @Test
    public void testTransformImageSetNoExceptionIfEmptyAttributes() throws Exception {
        final EomFile eomFile = new EomFile(UUID, "Image", null, "", "", "", "", null);

        final Content content = methodeImageSetMapper.mapImageSet(IMAGE_SET_UUID, eomFile, TRANSACTION_ID, LAST_MODIFIED_DATE);

        assertThat(content.getUuid(), equalTo(IMAGE_SET_UUID));
        assertThat(content.getIdentifiers().first().getAuthority(), equalTo(METHODE_IDENTIFIER_AUTHORITY));
        assertThat(content.getIdentifiers().first().getIdentifierValue(), equalTo(IMAGE_SET_UUID));
        assertThat(content.getMembers(), equalTo(ImmutableSortedSet.of(new Member(UUID))));
        assertThat(content.getMediaType(), equalTo("image/jpeg"));
        assertThat(content.getPublishReference(), equalTo(TRANSACTION_ID));
    }

    @Test(expected = TransformationException.class)
    public void testTransformAndHandleExceptionsThrowsTransformationException() {
        final EomFile eomFile = new EomFile(UUID, "Image", null, "", "", "", "", null);
        methodeImageSetMapper.transformAndHandleExceptions(eomFile, () -> {
            throw new IOException();
        });
    }

}
