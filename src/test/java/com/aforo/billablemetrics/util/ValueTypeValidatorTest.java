package com.aforo.billablemetrics.util;

import com.aforo.billablemetrics.enums.DimensionDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ValueTypeValidatorTest {

    @Test
    @DisplayName("Should validate NUMBER type - accepts valid numbers")
    void testValidNumberValues() {
        // Test with statusCode dimension (NUMBER type)
        DimensionDefinition statusCode = DimensionDefinition.STATUS_CODE;
        
        assertTrue(ValueTypeValidator.isValidValueForType(statusCode, "200"));
        assertTrue(ValueTypeValidator.isValidValueForType(statusCode, "404"));
        assertTrue(ValueTypeValidator.isValidValueForType(statusCode, "500"));
        assertTrue(ValueTypeValidator.isValidValueForType(statusCode, "0"));
        assertTrue(ValueTypeValidator.isValidValueForType(statusCode, "-1"));
        assertTrue(ValueTypeValidator.isValidValueForType(statusCode, "3.14"));
        assertTrue(ValueTypeValidator.isValidValueForType(statusCode, "100.5"));
    }

    @Test
    @DisplayName("Should validate NUMBER type - rejects invalid numbers")
    void testInvalidNumberValues() {
        DimensionDefinition responseTime = DimensionDefinition.RESPONSE_TIME;
        
        assertFalse(ValueTypeValidator.isValidValueForType(responseTime, "abc"));
        assertFalse(ValueTypeValidator.isValidValueForType(responseTime, "12abc"));
        assertFalse(ValueTypeValidator.isValidValueForType(responseTime, "abc123"));
        assertFalse(ValueTypeValidator.isValidValueForType(responseTime, ""));
        assertFalse(ValueTypeValidator.isValidValueForType(responseTime, "  "));
        assertFalse(ValueTypeValidator.isValidValueForType(responseTime, "1.2.3"));
        assertFalse(ValueTypeValidator.isValidValueForType(responseTime, "NaN"));
    }

    @Test
    @DisplayName("Should validate STRING type - accepts any non-empty string")
    void testValidStringValues() {
        DimensionDefinition endpoint = DimensionDefinition.ENDPOINT;
        
        assertTrue(ValueTypeValidator.isValidValueForType(endpoint, "/api/users"));
        assertTrue(ValueTypeValidator.isValidValueForType(endpoint, "GET"));
        assertTrue(ValueTypeValidator.isValidValueForType(endpoint, "123"));
        assertTrue(ValueTypeValidator.isValidValueForType(endpoint, "special!@#$%"));
    }

    @Test
    @DisplayName("Should validate STRING type - rejects empty strings")
    void testInvalidStringValues() {
        DimensionDefinition requestId = DimensionDefinition.REQUEST_ID;
        
        assertFalse(ValueTypeValidator.isValidValueForType(requestId, ""));
        assertFalse(ValueTypeValidator.isValidValueForType(requestId, "   "));
        assertFalse(ValueTypeValidator.isValidValueForType(requestId, null));
    }

    @Test
    @DisplayName("Should validate BOOLEAN type - accepts true/false")
    void testValidBooleanValues() {
        DimensionDefinition compressed = DimensionDefinition.COMPRESSED;
        
        assertTrue(ValueTypeValidator.isValidValueForType(compressed, "true"));
        assertTrue(ValueTypeValidator.isValidValueForType(compressed, "false"));
        assertTrue(ValueTypeValidator.isValidValueForType(compressed, "TRUE"));
        assertTrue(ValueTypeValidator.isValidValueForType(compressed, "FALSE"));
        assertTrue(ValueTypeValidator.isValidValueForType(compressed, "True"));
        assertTrue(ValueTypeValidator.isValidValueForType(compressed, "False"));
    }

    @Test
    @DisplayName("Should validate BOOLEAN type - rejects non-boolean values")
    void testInvalidBooleanValues() {
        DimensionDefinition cached = DimensionDefinition.CACHED;
        
        assertFalse(ValueTypeValidator.isValidValueForType(cached, "yes"));
        assertFalse(ValueTypeValidator.isValidValueForType(cached, "no"));
        assertFalse(ValueTypeValidator.isValidValueForType(cached, "1"));
        assertFalse(ValueTypeValidator.isValidValueForType(cached, "0"));
        assertFalse(ValueTypeValidator.isValidValueForType(cached, ""));
        assertFalse(ValueTypeValidator.isValidValueForType(cached, "maybe"));
    }

    @Test
    @DisplayName("Should validate DATE type - accepts various date formats")
    void testValidDateValues() {
        DimensionDefinition timestamp = DimensionDefinition.TIMESTAMP;
        
        assertTrue(ValueTypeValidator.isValidValueForType(timestamp, "2024-01-15"));
        assertTrue(ValueTypeValidator.isValidValueForType(timestamp, "2024-12-31"));
        assertTrue(ValueTypeValidator.isValidValueForType(timestamp, "2024-01-15 10:30:00"));
        assertTrue(ValueTypeValidator.isValidValueForType(timestamp, "2024-01-15T10:30:00"));
        assertTrue(ValueTypeValidator.isValidValueForType(timestamp, "01/15/2024"));
        assertTrue(ValueTypeValidator.isValidValueForType(timestamp, "15/01/2024"));
    }

    @Test
    @DisplayName("Should validate DATE type - rejects invalid dates")
    void testInvalidDateValues() {
        DimensionDefinition uploadTime = DimensionDefinition.UPLOAD_TIME;
        
        assertFalse(ValueTypeValidator.isValidValueForType(uploadTime, "not-a-date"));
        assertFalse(ValueTypeValidator.isValidValueForType(uploadTime, "2024"));
        assertFalse(ValueTypeValidator.isValidValueForType(uploadTime, "13/32/2024"));
        assertFalse(ValueTypeValidator.isValidValueForType(uploadTime, ""));
        assertFalse(ValueTypeValidator.isValidValueForType(uploadTime, "123456"));
    }

    @Test
    @DisplayName("Should validate ENUM type - region values")
    void testValidEnumRegionValues() {
        DimensionDefinition region = DimensionDefinition.REGION_API;
        
        assertTrue(ValueTypeValidator.isValidValueForType(region, "us-east-1"));
        assertTrue(ValueTypeValidator.isValidValueForType(region, "us-west-2"));
        assertTrue(ValueTypeValidator.isValidValueForType(region, "eu-west-1"));
        assertTrue(ValueTypeValidator.isValidValueForType(region, "ap-south-1"));
        // Case insensitive
        assertTrue(ValueTypeValidator.isValidValueForType(region, "US-EAST-1"));
    }

    @Test
    @DisplayName("Should validate ENUM type - rejects invalid region values")
    void testInvalidEnumRegionValues() {
        DimensionDefinition region = DimensionDefinition.REGION_REQUEST;
        
        assertFalse(ValueTypeValidator.isValidValueForType(region, "invalid-region"));
        assertFalse(ValueTypeValidator.isValidValueForType(region, ""));
        assertFalse(ValueTypeValidator.isValidValueForType(region, "123"));
    }

    @Test
    @DisplayName("Should validate ENUM type - payment method values")
    void testValidEnumPaymentMethodValues() {
        DimensionDefinition paymentMethod = DimensionDefinition.PAYMENT_METHOD;
        
        assertTrue(ValueTypeValidator.isValidValueForType(paymentMethod, "credit_card"));
        assertTrue(ValueTypeValidator.isValidValueForType(paymentMethod, "paypal"));
        assertTrue(ValueTypeValidator.isValidValueForType(paymentMethod, "bank_transfer"));
        assertTrue(ValueTypeValidator.isValidValueForType(paymentMethod, "CREDIT_CARD"));
    }

    @Test
    @DisplayName("Should validate ENUM type - currency values")
    void testValidEnumCurrencyValues() {
        DimensionDefinition currency = DimensionDefinition.CURRENCY;
        
        assertTrue(ValueTypeValidator.isValidValueForType(currency, "USD"));
        assertTrue(ValueTypeValidator.isValidValueForType(currency, "EUR"));
        assertTrue(ValueTypeValidator.isValidValueForType(currency, "GBP"));
        assertTrue(ValueTypeValidator.isValidValueForType(currency, "usd")); // case insensitive
    }

    @Test
    @DisplayName("Should validate ENUM type - device values")
    void testValidEnumDeviceValues() {
        DimensionDefinition device = DimensionDefinition.DEVICE;
        
        assertTrue(ValueTypeValidator.isValidValueForType(device, "desktop"));
        assertTrue(ValueTypeValidator.isValidValueForType(device, "mobile"));
        assertTrue(ValueTypeValidator.isValidValueForType(device, "tablet"));
        assertTrue(ValueTypeValidator.isValidValueForType(device, "MOBILE"));
    }

    @Test
    @DisplayName("Should normalize boolean values to lowercase")
    void testNormalizeBooleanValues() {
        DimensionDefinition compressed = DimensionDefinition.COMPRESSED;
        
        assertEquals("true", ValueTypeValidator.normalizeValue(compressed, "TRUE"));
        assertEquals("false", ValueTypeValidator.normalizeValue(compressed, "FALSE"));
        assertEquals("true", ValueTypeValidator.normalizeValue(compressed, "True"));
    }

    @Test
    @DisplayName("Should normalize enum values to standard format")
    void testNormalizeEnumValues() {
        DimensionDefinition currency = DimensionDefinition.CURRENCY;
        
        assertEquals("USD", ValueTypeValidator.normalizeValue(currency, "usd"));
        assertEquals("EUR", ValueTypeValidator.normalizeValue(currency, "eur"));
        assertEquals("GBP", ValueTypeValidator.normalizeValue(currency, "gbp"));
    }

    @Test
    @DisplayName("Should provide appropriate error messages")
    void testErrorMessages() {
        DimensionDefinition statusCode = DimensionDefinition.STATUS_CODE;
        DimensionDefinition region = DimensionDefinition.REGION_API;
        DimensionDefinition cached = DimensionDefinition.CACHED;
        
        String numberError = ValueTypeValidator.getValidationErrorMessage(statusCode, "abc");
        assertTrue(numberError.contains("must be a valid number"));
        assertTrue(numberError.contains("statusCode"));
        
        String enumError = ValueTypeValidator.getValidationErrorMessage(region, "invalid");
        assertTrue(enumError.contains("must be one of"));
        assertTrue(enumError.contains("region"));
        
        String booleanError = ValueTypeValidator.getValidationErrorMessage(cached, "yes");
        assertTrue(booleanError.contains("must be either 'true' or 'false'"));
        assertTrue(booleanError.contains("cached"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"100", "200", "300", "400", "500"})
    @DisplayName("Should accept various HTTP status codes")
    void testHttpStatusCodes(String statusCode) {
        DimensionDefinition statusCodeDim = DimensionDefinition.STATUS_CODE;
        assertTrue(ValueTypeValidator.isValidValueForType(statusCodeDim, statusCode));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "100", "1000", "10000", "999999"})
    @DisplayName("Should accept various token counts")
    void testTokenCounts(String tokenCount) {
        DimensionDefinition tokenCountDim = DimensionDefinition.TOKEN_COUNT;
        assertTrue(ValueTypeValidator.isValidValueForType(tokenCountDim, tokenCount));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "12abc", "!@#", ""})
    @DisplayName("Should reject non-numeric values for amount field")
    void testInvalidAmountValues(String amount) {
        DimensionDefinition amountDim = DimensionDefinition.AMOUNT;
        assertFalse(ValueTypeValidator.isValidValueForType(amountDim, amount));
    }
}
