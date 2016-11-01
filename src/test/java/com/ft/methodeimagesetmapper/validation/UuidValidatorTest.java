package com.ft.methodeimagesetmapper.validation;

import com.ft.methodeimagesetmapper.exception.ValidationException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class UuidValidatorTest {

    private static final String UUID = "d7625378-d4cd-11e2-bce1-002128161462";
    private static final String MALFORMED_UUID = "d7625-d4cd-11e2-bce1-002462";

    private UuidValidator uuidValidator;

    @Before
    public void setUp() {
        uuidValidator = new UuidValidator();
    }

    @Test
    public void testValidate() {
        Exception result = null;
        try {
            uuidValidator.validate(UUID);
        } catch (ValidationException e) {
            result = e;
        }
        assertNull(result);
    }

    @Test(expected = ValidationException.class)
    public void testValidateEmptyString() {
        uuidValidator.validate("");
    }

    @Test(expected = ValidationException.class)
    public void testValidateMalformedUUID() {
        uuidValidator.validate(MALFORMED_UUID);
    }
}
