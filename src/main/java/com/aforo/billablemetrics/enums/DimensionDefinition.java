package com.aforo.billablemetrics.enums;

import java.util.List;
import java.util.Map;

public enum DimensionDefinition {

    // 🔵 UOM: api_call
    STATUS_CODE("api_call", "statusCode", DataType.NUMBER, List.of("=", "!=", ">", "<", ">=", "<=")),
    METHOD("api_call", "method", DataType.STRING, List.of("equals", "contains", "notEquals")),
    ENDPOINT("api_call", "endpoint", DataType.STRING, List.of("startsWith", "contains", "equals")),
    REGION_API("api_call", "region", DataType.ENUM, List.of("equals", "notEquals", "in")),
    RESPONSE_TIME("api_call", "responseTime", DataType.NUMBER, List.of(">", "<", ">=", "<=")),
    API_KEY("api_call", "apiKey", DataType.STRING, List.of("equals", "notEquals", "contains")),

    // 🔵 UOM: request
    REQUEST_ID("request", "requestId", DataType.STRING, List.of("equals", "contains")),
    USER_AGENT("request", "userAgent", DataType.STRING, List.of("contains", "startsWith")),
    IP_ADDRESS("request", "ipAddress", DataType.STRING, List.of("equals", "contains", "startsWith")),
    REGION_REQUEST("request", "region", DataType.ENUM, List.of("equals", "in", "notEquals")),
    TIMESTAMP("request", "timestamp", DataType.DATE, List.of("before", "after", "between")),

    // 🔵 UOM: transaction
    TRANSACTION_ID("transaction", "transactionId", DataType.STRING, List.of("equals", "notEquals")),
    STATUS("transaction", "status", DataType.ENUM, List.of("equals", "notEquals", "in")),
    AMOUNT("transaction", "amount", DataType.NUMBER, List.of(">", "<", ">=", "<=")),
    CURRENCY("transaction", "currency", DataType.ENUM, List.of("equals", "notEquals")),
    PAYMENT_METHOD("transaction", "paymentMethod", DataType.ENUM, List.of("equals", "notEquals", "in")),

    // 🔵 UOM: hit
    PAGE_URL("hit", "pageUrl", DataType.STRING, List.of("contains", "startsWith", "equals")),
    USER_ID_HIT("hit", "userId", DataType.STRING, List.of("equals", "notEquals")),
    DEVICE("hit", "device", DataType.ENUM, List.of("equals", "in", "notEquals")),
    BROWSER("hit", "browser", DataType.ENUM, List.of("equals", "notEquals", "in")),
    TIME_SPENT("hit", "timeSpent", DataType.NUMBER, List.of(">", "<", ">=", "<=")),




    //  LLM TOKENS UOM's
    // 🔵 UOM: token
    MODEL_NAME("token", "modelName", DataType.STRING, List.of("equals", "startsWith")),
    TOKEN_TYPE("token", "tokenType", DataType.ENUM, List.of("equals", "notEquals")),
    TOKEN_COUNT("token", "tokenCount", DataType.NUMBER, List.of(">", "<", ">=", "<=")),
    COMPUTE_TIER("token", "computeTier", DataType.ENUM, List.of("equals", "in")),
    USER_ID_TOKEN("token", "userId", DataType.STRING, List.of("equals", "contains")),

    // 🔵 UOM: completion_token
    MODEL_NAME_COMPLETION_TOKEN("completion_token", "modelName", DataType.STRING, List.of("equals", "startsWith")),
    TOKEN_TYPE_COMPLETION_TOKEN("completion_token", "tokenType", DataType.ENUM, List.of("equals", "notEquals")),
    TOKEN_COUNT_COMPLETION_TOKEN("completion_token", "tokenCount", DataType.NUMBER, List.of(">", "<", ">=", "<=")),
    COMPUTE_TIER_COMPLETION_TOKEN("completion_token", "computeTier", DataType.ENUM, List.of("equals", "in")),
    USER_ID_COMPLETION_TOKEN("completion_token", "userId", DataType.STRING, List.of("equals", "contains")),

    // 🔵 UOM: prompt_token
    MODEL_NAME_PROMPT_TOKEN("prompt_token", "modelName", DataType.STRING, List.of("equals", "startsWith")),
    TOKEN_TYPE_PROMPT_TOKEN("prompt_token", "tokenType", DataType.ENUM, List.of("equals", "notEquals")),
    TOKEN_COUNT_PROMPT_TOKEN("prompt_token", "tokenCount", DataType.NUMBER, List.of(">", "<", ">=", "<=")),
    COMPUTE_TIER_PROMPT_TOKEN("prompt_token", "computeTier", DataType.ENUM, List.of("equals", "in")),
    USER_ID_PROMPT_TOKEN("prompt_token", "userId", DataType.STRING, List.of("equals", "contains")),


//FLAT FILE RELATED

    // 🔵 UOM: file
    FILE_NAME("file", "fileName", DataType.STRING, List.of("equals", "contains", "endsWith")),
    FILE_TYPE("file", "fileType", DataType.ENUM, List.of("equals", "notEquals", "in")),
    FILE_SIZE("file", "fileSize", DataType.NUMBER, List.of(">", "<", ">=", "<=")),
    UPLOAD_TIME("file", "uploadTime", DataType.DATE, List.of("before", "after", "between")),
    SOURCE("file", "source", DataType.ENUM, List.of("equals", "notEquals", "in")),

    // 🔵 UOM: delivery
    DELIVERY_ID("delivery", "deliveryId", DataType.STRING, List.of("equals", "notEquals")),
    DELIVERY_STATUS("delivery", "status", DataType.ENUM, List.of("equals", "notEquals", "in")),
    DELIVERY_REGION("delivery", "region", DataType.ENUM, List.of("equals", "in", "notEquals")),
    DELIVERY_TIME("delivery", "deliveryTime", DataType.DATE, List.of("before", "after", "between")),

    // 🔵 UOM: MB
    FILE_SIZE_MB("MB", "fileSizeMB", DataType.NUMBER, List.of(">", "<", ">=", "<=")),
    COMPRESSED("MB", "compressed", DataType.BOOLEAN, List.of("is true", "is false")),
    REGION_MB("MB", "region", DataType.ENUM, List.of("equals", "in")),
    TRANSFER_TYPE("MB", "transferType", DataType.ENUM, List.of("equals", "notEquals", "in")),

    // 🔵 UOM: record
    ROW_COUNT("record", "rowCount", DataType.NUMBER, List.of(">", "<", ">=", "<=")),
    SOURCE_SYSTEM("record", "sourceSystem", DataType.STRING, List.of("equals", "notEquals")),
    SCHEMA_VERSION("record", "schemaVersion", DataType.STRING, List.of("equals", "startsWith")),
    IS_VALID("record", "isValid", DataType.BOOLEAN, List.of("is true", "is false")),


    // 🔵 UOM: row
    ROW_COUNT_ROW("row", "rowCount", DataType.NUMBER, List.of(">", "<", ">=", "<=")),
    SOURCE_SYSTEM_ROW("row", "sourceSystem", DataType.STRING, List.of("equals", "notEquals")),
    SCHEMA_VERSION_ROW("row", "schemaVersion", DataType.STRING, List.of("equals", "startsWith")),
    IS_VALID_ROW("row", "isValid", DataType.BOOLEAN, List.of("is true", "is false")),
    

    // FLAT FILE RELATED 



    

    // 🔵 UOM: query_execution
    QUERY_TYPE("query_execution", "queryType", DataType.ENUM, List.of("equals", "notEquals", "in")),
    EXECUTION_TIME("query_execution", "executionTime", DataType.NUMBER, List.of(">", "<", ">=", "<=")),
    CACHED("query_execution", "cached", DataType.BOOLEAN, List.of("is true", "is false")),
    ROW_COUNT_QUERY("query_execution", "rowCount", DataType.NUMBER, List.of(">", "<", ">=", "<=")),
    USER_ID_QUERY("query_execution", "userId", DataType.STRING, List.of("equals", "contains")),


    // 🔵 UOM: cell
    QUERY_TYPE_CELL("cell", "queryType", DataType.ENUM, List.of("equals", "notEquals", "in")),
    EXECUTION_TIME_CELL("cell", "executionTime", DataType.NUMBER, List.of(">", "<", ">=", "<=")),
    CACHED_CELL("cell", "cached", DataType.BOOLEAN, List.of("is true", "is false")),
    ROW_COUNT_QUERY_CELL("cell", "rowCount", DataType.NUMBER, List.of(">", "<", ">=", "<=")),
    USER_ID_QUERY_CELL("cell", "userId", DataType.STRING, List.of("equals", "contains"));    
    




    private final String uom;
    private final String dimension;
    private final DataType type;
    private final List<String> validOperators;

    DimensionDefinition(String uom, String dimension, DataType type, List<String> validOperators) {
        this.uom = uom;
        this.dimension = dimension;
        this.type = type;
        this.validOperators = validOperators;
    }

    public String getUom() {
        return uom;
    }

    public String getDimension() {
        return dimension;
    }

    public DataType getType() {
        return type;
    }

    public List<String> getValidOperators() {
        return validOperators;
    }
}
