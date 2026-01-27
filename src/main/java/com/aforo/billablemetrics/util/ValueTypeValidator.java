package com.aforo.billablemetrics.util;

import com.aforo.billablemetrics.enums.DataType;
import com.aforo.billablemetrics.enums.DimensionDefinition;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class ValueTypeValidator {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile("^(true|false)$", Pattern.CASE_INSENSITIVE);
    
    // Date formats to try
    private static final String[] DATE_FORMATS = {
        "yyyy-MM-dd",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "MM/dd/yyyy",
        "dd/MM/yyyy"
    };

    // Market-standard enum values for each enum field
    private static final Map<String, Set<String>> ENUM_VALUES = Map.ofEntries(
        // Region enums (common across multiple UOMs)
        Map.entry("region", Set.of("us-east-1", "us-west-1", "us-west-2", "eu-west-1", "eu-central-1", 
                                   "ap-southeast-1", "ap-northeast-1", "ap-south-1", "sa-east-1", 
                                   "ca-central-1", "me-south-1", "af-south-1")),
        
        // Transaction status
        Map.entry("status", Set.of("pending", "completed", "failed", "cancelled", "processing", 
                                   "refunded", "partially_refunded")),
        
        // Currency codes (ISO 4217)
        Map.entry("currency", Set.of("USD", "EUR", "GBP", "JPY", "CNY", "INR", "CAD", "AUD", 
                                     "CHF", "NZD", "SEK", "NOK", "SGD", "HKD", "KRW", "BRL", "MXN")),
        
        // Payment methods
        Map.entry("paymentMethod", Set.of("credit_card", "debit_card", "paypal", "bank_transfer", 
                                          "wire_transfer", "apple_pay", "google_pay", "stripe", 
                                          "square", "venmo", "cash", "check", "cryptocurrency")),
        
        // Device types
        Map.entry("device", Set.of("desktop", "mobile", "tablet", "smart_tv", "console", 
                                   "wearable", "embedded", "unknown")),
        
        // Browser types
        Map.entry("browser", Set.of("chrome", "firefox", "safari", "edge", "opera", "brave", 
                                    "ie", "samsung_browser", "uc_browser", "vivaldi", "tor", "unknown")),
        
        // Token types for LLM
        Map.entry("tokenType", Set.of("prompt", "completion", "total", "input", "output", 
                                      "system", "assistant", "user")),
        
        // Compute tiers for LLM
        Map.entry("computeTier", Set.of("standard", "premium", "enterprise", "free", "trial", 
                                        "batch", "realtime", "priority")),
        
        // File types
        Map.entry("fileType", Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", 
                                     "txt", "csv", "json", "xml", "html", "css", "js", 
                                     "jpg", "jpeg", "png", "gif", "svg", "mp4", "mp3", 
                                     "zip", "rar", "tar", "gz", "7z", "other")),
        
        // Source systems
        Map.entry("source", Set.of("web", "mobile_app", "api", "batch", "manual", "import", 
                                   "export", "sync", "webhook", "scheduled", "realtime")),
        
        // Transfer types
        Map.entry("transferType", Set.of("upload", "download", "streaming", "batch", "realtime", 
                                         "sync", "async", "p2p", "cdn", "direct")),
        
        // Query types
        Map.entry("queryType", Set.of("select", "insert", "update", "delete", "create", "drop", 
                                      "alter", "truncate", "merge", "upsert", "bulk_insert", 
                                      "stored_procedure", "function", "view"))
    );

    /**
     * Validates if the value matches the expected data type for the dimension
     */
    public static boolean isValidValueForType(DimensionDefinition dimension, String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        DataType type = dimension.getType();
        String trimmedValue = value.trim();
        
        return switch (type) {
            case NUMBER -> isValidNumber(trimmedValue);
            case STRING -> isValidString(trimmedValue);
            case BOOLEAN -> isValidBoolean(trimmedValue);
            case DATE -> isValidDate(trimmedValue);
            case ENUM -> isValidEnum(dimension.getDimension(), trimmedValue);
        };
    }

    /**
     * Gets a detailed error message for invalid value
     */
    public static String getValidationErrorMessage(DimensionDefinition dimension, String value) {
        DataType type = dimension.getType();
        String dimensionName = dimension.getDimension();
        
        return switch (type) {
            case NUMBER -> String.format("Value '%s' for dimension '%s' must be a valid number (integer or decimal)", 
                                        value, dimensionName);
            case STRING -> String.format("Value '%s' for dimension '%s' must be a non-empty string", 
                                        value, dimensionName);
            case BOOLEAN -> String.format("Value '%s' for dimension '%s' must be either 'true' or 'false'", 
                                         value, dimensionName);
            case DATE -> String.format("Value '%s' for dimension '%s' must be a valid date (supported formats: yyyy-MM-dd, yyyy-MM-dd HH:mm:ss, etc.)", 
                                      value, dimensionName);
            case ENUM -> {
                Set<String> validValues = ENUM_VALUES.get(dimensionName);
                if (validValues != null) {
                    yield String.format("Value '%s' for dimension '%s' must be one of: %s", 
                                       value, dimensionName, validValues);
                } else {
                    yield String.format("Value '%s' for dimension '%s' is not a valid enum value", 
                                       value, dimensionName);
                }
            }
        };
    }

    private static boolean isValidNumber(String value) {
        return NUMBER_PATTERN.matcher(value).matches();
    }

    private static boolean isValidString(String value) {
        // String can be anything non-empty
        return !value.isEmpty();
    }

    private static boolean isValidBoolean(String value) {
        return BOOLEAN_PATTERN.matcher(value).matches();
    }

    private static boolean isValidDate(String value) {
        // Try multiple date formats
        for (String format : DATE_FORMATS) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                if (format.contains("HH")) {
                    LocalDateTime.parse(value, formatter);
                } else {
                    LocalDate.parse(value, formatter);
                }
                return true;
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }
        
        // Also try ISO formats
        try {
            LocalDateTime.parse(value);
            return true;
        } catch (DateTimeParseException e1) {
            try {
                LocalDate.parse(value);
                return true;
            } catch (DateTimeParseException e2) {
                return false;
            }
        }
    }

    private static boolean isValidEnum(String dimensionName, String value) {
        Set<String> validValues = ENUM_VALUES.get(dimensionName);
        if (validValues == null) {
            // If no specific enum values defined, accept any non-empty string
            return !value.isEmpty();
        }
        
        // Case-insensitive comparison for enum values
        return validValues.stream()
            .anyMatch(v -> v.equalsIgnoreCase(value));
    }

    /**
     * Normalizes the value based on the data type
     */
    public static String normalizeValue(DimensionDefinition dimension, String value) {
        if (value == null || value.trim().isEmpty()) {
            return value;
        }
        
        DataType type = dimension.getType();
        String trimmedValue = value.trim();
        
        return switch (type) {
            case BOOLEAN -> trimmedValue.toLowerCase();
            case ENUM -> {
                // Normalize enum values to match the standard format
                String dimensionName = dimension.getDimension();
                Set<String> validValues = ENUM_VALUES.get(dimensionName);
                if (validValues != null) {
                    // Find the matching value (case-insensitive) and return the standard format
                    yield validValues.stream()
                        .filter(v -> v.equalsIgnoreCase(trimmedValue))
                        .findFirst()
                        .orElse(trimmedValue);
                }
                yield trimmedValue;
            }
            default -> trimmedValue;
        };
    }
}
