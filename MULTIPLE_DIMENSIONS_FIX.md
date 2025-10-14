# Multiple Dimensions with Same Name - Fix Documentation

## Problem Fixed
Previously, when creating or updating billable metrics, you could not add multiple usage conditions with the same dimension name (e.g., multiple "statusCode" conditions). The system would only keep one condition per dimension name.

## Solution Implemented

### 1. Multiple Dimensions with Same Name Support
- **Creation**: You can now create billable metrics with multiple usage conditions having the same dimension name
- **Update**: You can now update billable metrics to include multiple usage conditions with the same dimension name

### 2. Optional Operator and Value
- **Operator**: If not provided, the system will use the first valid operator for that dimension
- **Value**: If not provided, the system will use a sensible default based on the dimension's data type

## Examples

### Example 1: Creating with Multiple Status Code Conditions
```json
{
  "metricName": "API Calls with Multiple Status Codes",
  "productId": 100,
  "unitOfMeasure": "API_CALL",
  "aggregationFunction": "COUNT",
  "aggregationWindow": "PER_DAY",
  "usageConditions": [
    {
      "dimension": "STATUS_CODE",
      "operator": ">=",
      "value": "200"
    },
    {
      "dimension": "STATUS_CODE",
      "operator": "<",
      "value": "400"
    },
    {
      "dimension": "STATUS_CODE",
      "operator": "!=",
      "value": "404"
    }
  ]
}
```

### Example 2: Creating with Only Dimension (Operator and Value Optional)
```json
{
  "metricName": "API Calls with Default Conditions",
  "productId": 100,
  "unitOfMeasure": "API_CALL",
  "aggregationFunction": "COUNT",
  "aggregationWindow": "PER_DAY",
  "usageConditions": [
    {
      "dimension": "STATUS_CODE"
      // operator and value will be set automatically
    },
    {
      "dimension": "METHOD",
      "operator": "equals"
      // value will be set automatically
    },
    {
      "dimension": "ENDPOINT",
      "value": "/api/users"
      // operator will be set automatically
    }
  ]
}
```

## Default Values Applied

### Default Operators
- The system uses the first valid operator from the dimension's allowed operators list
- For STATUS_CODE: "=" (equals)
- For METHOD: "equals"
- For ENDPOINT: "startsWith"

### Default Values by Data Type
- **NUMBER**: "0"
- **STRING**: "" (empty string)
- **BOOLEAN**: "true"
- **DATE**: "2024-01-01"
- **ENUM**: "default"

## Technical Changes Made

### 1. BillableMetricServiceImpl.java
- Modified `enrichUsageConditions()` method to handle optional operator and value
- Added `getDefaultOperatorForDimension()` helper method
- Added `getDefaultValueForDimension()` helper method
- Updated `updateMetric()` method to use list instead of map for usage conditions

### 2. Key Benefits
- **Flexibility**: Users can provide just the dimension and let the system handle defaults
- **Multiple Conditions**: Support for multiple conditions with the same dimension name
- **Backward Compatibility**: Existing code that provides operator and value still works
- **Validation**: All conditions are still properly validated

## Usage Scenarios

### Scenario 1: API Rate Limiting
Create conditions for different status code ranges:
- statusCode >= 200 (successful requests)
- statusCode >= 400 (client errors)
- statusCode >= 500 (server errors)

### Scenario 2: Method-based Billing
Create conditions for different HTTP methods:
- method = "GET"
- method = "POST"
- method = "PUT"

### Scenario 3: Quick Setup
Just specify dimensions and let the system handle the rest:
- dimension: STATUS_CODE (system sets operator="=" and value="0")
- dimension: METHOD (system sets operator="equals" and value="")
