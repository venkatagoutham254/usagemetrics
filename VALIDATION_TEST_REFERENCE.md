# Validation Test Reference Guide

## Product Information
- **Product 24**: API type
- **Product 25**: FLATFILE type  
- **Product 2**: SQL type
- **Product 3**: LLM type

## Token
```
eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJhZm9yby1jdXN0b21lcnNlcnZpY2UiLCJzdWIiOiJnb3d0aGFtMUBhZm9yby5haSIsInN0YXR1cyI6IkFDVElWRSIsIm9yZ0lkIjoyLCJpYXQiOjE3Njk1MjkxOTQsImV4cCI6MTc3MDEzMzk5NH0.04OBQayu-N2woVhGW2reO-6lUtrbZrPwEy_uvGwtz5M
```

---

## API_CALL UOM (Product 24 - API Type)

### Dimensions & Valid Values

#### 1. statusCode (NUMBER)
- **Valid**: Any number (200, 404, 500, 201, etc.)
- **Invalid**: Strings ("not_a_number", "error", "success")

#### 2. method (STRING)
- **Valid**: Any non-empty string (GET, POST, PUT, DELETE, PATCH)
- **Invalid**: Empty string "", spaces only "   "

#### 3. endpoint (STRING)
- **Valid**: Any non-empty string (/api/users, /v1/products, /health)
- **Invalid**: Empty string "", spaces only "   "

#### 4. region (ENUM)
- **Valid Values**: 
  - us-east-1
  - us-west-1
  - us-west-2
  - eu-west-1
  - eu-central-1
  - ap-southeast-1
  - ap-northeast-1
  - ap-south-1
  - sa-east-1
  - ca-central-1
  - me-south-1
  - af-south-1
- **Invalid**: mars-west-1, invalid-region, xyz

#### 5. responseTime (NUMBER)
- **Valid**: Any number (100, 250.5, 1000, 50.25)
- **Invalid**: Strings ("slow", "fast", "quick")

#### 6. apiKey (STRING)
- **Valid**: Any non-empty string (sk-1234567890, api_key_xyz)
- **Invalid**: Empty string "", spaces only "   "

---

## FILE UOM (Product 25 - FLATFILE Type)

### Dimensions & Valid Values

#### 1. fileName (STRING)
- **Valid**: Any non-empty string (report.pdf, data.csv, image.png)
- **Invalid**: Empty string "", spaces only "   "

#### 2. fileType (ENUM)
- **Valid Values**:
  - pdf
  - doc, docx
  - xls, xlsx
  - ppt, pptx
  - txt
  - csv
  - json
  - xml
  - html
  - css
  - js
  - jpg, jpeg
  - png
  - gif
  - svg
  - mp4
  - mp3
  - zip
  - rar
  - tar
  - gz
  - 7z
  - other
- **Invalid**: hologram, fake, xyz

#### 3. fileSize (NUMBER)
- **Valid**: Any number (1024, 1048576, 500.5)
- **Invalid**: Strings ("large", "small", "huge")

#### 4. uploadTime (DATE)
- **Valid**: ISO date formats
  - 2024-01-15
  - 2024-01-15T10:30:00
  - 2024-12-31
- **Invalid**: "not-a-date", "yesterday", "2024-13-45"

#### 5. source (ENUM)
- **Valid Values**:
  - web
  - api
  - mobile_app
  - batch
  - realtime
  - manual
  - scheduled
  - webhook
  - import
  - export
  - sync
- **Invalid**: telepathy, magic, xyz

---

## QUERY_EXECUTION UOM (Product 2 - SQL Type)

### Dimensions & Valid Values

#### 1. queryType (ENUM)
- **Valid Values**:
  - select
  - insert
  - update
  - delete
  - create
  - alter
  - drop
  - truncate
  - merge
  - upsert
  - bulk_insert
  - view
  - stored_procedure
  - function
- **Invalid**: magic_query, invalid, xyz

#### 2. executionTime (NUMBER)
- **Valid**: Any number (250, 100.5, 1000)
- **Invalid**: Strings ("fast", "slow", "quick")

#### 3. cached (BOOLEAN)
- **Valid**: true, false
- **Invalid**: yes, no, 1, 0, YES, NO

#### 4. rowCount (NUMBER)
- **Valid**: Any number (1000, 500, 10000)
- **Invalid**: Strings ("many_rows", "lots", "few")

#### 5. userId (STRING)
- **Valid**: Any non-empty string (user_12345, admin, test_user)
- **Invalid**: Empty string "", spaces only "   "

---

## CELL UOM (Product 2 - SQL Type)

### Dimensions & Valid Values

#### 1. queryType (ENUM)
- **Same as QUERY_EXECUTION queryType**

#### 2. executionTime (NUMBER)
- **Valid**: Any number (150, 250.75, 500)
- **Invalid**: Strings ("quick", "slow", "fast")

#### 3. cached (BOOLEAN)
- **Valid**: true, false
- **Invalid**: yes, no, 1, 0

#### 4. rowCount (NUMBER)
- **Valid**: Any number (100, 500, 1000)
- **Invalid**: Strings ("many", "few", "lots")

#### 5. userId (STRING)
- **Valid**: Any non-empty string (user_abc, admin_123)
- **Invalid**: Empty string "", spaces only "   "

---

## TOKEN UOM (Product 3 - LLM Type)

### Dimensions & Valid Values

#### 1. tokenCount (NUMBER)
- **Valid**: Any number (5000, 1000, 2500.5)
- **Invalid**: Strings ("many", "lots", "huge")

#### 2. tokenType (ENUM)
- **Valid Values**:
  - prompt
  - completion
  - input
  - output
  - total
  - system
  - user
  - assistant
- **Invalid**: magic, invalid_type, xyz

#### 3. computeTier (ENUM)
- **Valid Values**:
  - standard
  - premium
  - enterprise
  - free
  - trial
  - batch
  - realtime
  - priority
- **Invalid**: ultra, mega, super

#### 4. modelName (STRING)
- **Valid**: Any non-empty string (gpt-4, claude-3, llama-2)
- **Invalid**: Empty string "", spaces only "   "

#### 5. userId (STRING)
- **Valid**: Any non-empty string (user_abc123, customer_xyz)
- **Invalid**: Empty string "", spaces only "   "

---

## PROMPT_TOKEN UOM (Product 3 - LLM Type)

### Dimensions & Valid Values

#### 1. tokenCount (NUMBER)
- **Valid**: Any number (2500, 1000, 3000)
- **Invalid**: Strings ("lots", "many", "few")

#### 2. tokenType (ENUM)
- **Same as TOKEN tokenType**

#### 3. computeTier (ENUM)
- **Same as TOKEN computeTier**

#### 4. modelName (STRING)
- **Valid**: Any non-empty string (gpt-4, claude-3)
- **Invalid**: Empty string "", spaces only "   "

#### 5. userId (STRING)
- **Valid**: Any non-empty string (user_123)
- **Invalid**: Empty string "", spaces only "   "

---

## COMPLETION_TOKEN UOM (Product 3 - LLM Type)

### Dimensions & Valid Values

#### 1. tokenCount (NUMBER)
- **Valid**: Any number (3500, 2000, 5000)
- **Invalid**: Strings ("huge", "massive", "lots")

#### 2. tokenType (ENUM)
- **Same as TOKEN tokenType**

#### 3. computeTier (ENUM)
- **Same as TOKEN computeTier**

#### 4. modelName (STRING)
- **Valid**: Any non-empty string (gpt-4-turbo)
- **Invalid**: Empty string "", spaces only "   "

#### 5. userId (STRING)
- **Valid**: Any non-empty string (user_xyz)
- **Invalid**: Empty string "", spaces only "   "

---

## Additional UOMs Available (Not Tested Yet)

### REQUEST UOM
- Dimensions: requestId, userAgent, ipAddress, region, timestamp

### TRANSACTION UOM
- Dimensions: transactionId, status, amount, currency, paymentMethod

### HIT UOM
- Dimensions: pageUrl, userId, device, browser, timeSpent

### DELIVERY UOM
- Dimensions: deliveryId, status, region, deliveryTime

### MB UOM
- Dimensions: fileSizeMB, compressed, region, transferType

### RECORD/ROW UOM
- Dimensions: rowCount, sourceSystem, schemaVersion, isValid

---

## Common ENUM Values Reference

### status (ENUM)
- pending
- processing
- completed
- failed
- cancelled
- success
- error

### currency (ENUM)
- USD
- EUR
- GBP
- JPY
- INR
- AUD
- CAD
- CHF
- CNY

### paymentMethod (ENUM)
- credit_card
- debit_card
- paypal
- bank_transfer
- wire_transfer
- crypto
- wallet
- cash

### device (ENUM)
- desktop
- mobile
- tablet
- smartwatch
- tv
- iot

### browser (ENUM)
- chrome
- firefox
- safari
- edge
- opera
- brave
- other

### transferType (ENUM)
- upload
- download
- sync
- backup
- restore
- migration

---

## Quick Test Examples

### Test Invalid NUMBER
```json
{
  "dimension": "TOKEN_COUNT",
  "operator": "<=",
  "value": "many"  // ❌ Should fail
}
```

### Test Valid NUMBER
```json
{
  "dimension": "TOKEN_COUNT",
  "operator": "<=",
  "value": "5000"  // ✅ Should pass
}
```

### Test Invalid ENUM
```json
{
  "dimension": "REGION_API",
  "operator": "equals",
  "value": "mars-west-1"  // ❌ Should fail
}
```

### Test Valid ENUM
```json
{
  "dimension": "REGION_API",
  "operator": "equals",
  "value": "us-east-1"  // ✅ Should pass
}
```

### Test Invalid BOOLEAN
```json
{
  "dimension": "CACHED",
  "operator": "is true",
  "value": "yes"  // ❌ Should fail
}
```

### Test Valid BOOLEAN
```json
{
  "dimension": "CACHED",
  "operator": "is true",
  "value": "true"  // ✅ Should pass
}
```

### Test Invalid DATE
```json
{
  "dimension": "UPLOAD_TIME",
  "operator": "after",
  "value": "not-a-date"  // ❌ Should fail
}
```

### Test Valid DATE
```json
{
  "dimension": "UPLOAD_TIME",
  "operator": "after",
  "value": "2024-01-15"  // ✅ Should pass
}
```

---

## Testing Checklist

### For Each Dimension:
- [ ] Test with valid value - should create metric successfully
- [ ] Test with invalid value - should return 400 BAD_REQUEST
- [ ] Verify error message shows valid options (for ENUMs)
- [ ] Verify error message describes expected format (for NUMBER, DATE, BOOLEAN)

### Data Type Coverage:
- [x] NUMBER - Tested (18 tests)
- [x] STRING - Tested (6 tests)
- [x] BOOLEAN - Tested (4 tests)
- [x] DATE - Tested (2 tests)
- [x] ENUM - Tested (17 tests)

### Product Type Coverage:
- [x] API (Product 24) - Tested
- [x] FLATFILE (Product 25) - Tested
- [x] SQL (Product 2) - Tested
- [x] LLM (Product 3) - Tested

**Total Tests Executed: 47 tests**
**All Validations: ✅ Working Correctly**
