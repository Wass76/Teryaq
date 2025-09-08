# Bulk Master Product Creation Endpoint

## Endpoint Details
- **URL**: `POST /api/v1/master_products/bulk`
- **Authentication**: Requires PLATFORM_ADMIN role
- **Content-Type**: `application/json`

## Request Format

### Headers
```
Authorization: Bearer <your-jwt-token>
Content-Type: application/json
```

### Query Parameters
- `lang` (optional): Language code (default: "ar")

### Request Body
Array of MProductDTORequest objects:

```json
[
  {
    "tradeName": "Panadol",
    "scientificName": "Paracetamol",
    "concentration": "500mg",
    "size": "20 tablets",
    "refPurchasePrice": 1500,
    "refSellingPrice": 2000,
    "notes": "Pain relief and fever reducer",
    "tax": 5.0,
    "barcode": "1234567890123",
    "requiresPrescription": false,
    "typeId": 1,
    "formId": 1,
    "manufacturerId": 1,
    "categoryIds": [1],
    "translations": [
      {
        "tradeName": "بانادول",
        "scientificName": "باراسيتامول",
        "lang": "ar"
      }
    ]
  },
  {
    "tradeName": "Augmentin",
    "scientificName": "Amoxicillin + Clavulanic Acid",
    "concentration": "625mg",
    "size": "14 tablets",
    "refPurchasePrice": 8500,
    "refSellingPrice": 10000,
    "notes": "Broad spectrum antibiotic",
    "tax": 5.0,
    "barcode": "1234567890124",
    "requiresPrescription": true,
    "typeId": 1,
    "formId": 2,
    "manufacturerId": 2,
    "categoryIds": [2],
    "translations": [
      {
        "tradeName": "أوجمنتين",
        "scientificName": "أموكسيسيلين + حمض الكلافولانيك",
        "lang": "ar"
      }
    ]
  }
]
```

## Response Format

### Success Response (200 OK)
```json
[
  {
    "id": 1,
    "tradeName": "Panadol",
    "scientificName": "Paracetamol",
    "concentration": "500mg",
    "size": "20 tablets",
    "refPurchasePrice": 1500.0,
    "refSellingPrice": 2000.0,
    "notes": "Pain relief and fever reducer",
    "tax": 5.0,
    "barcode": "1234567890123",
    "requiresPrescription": false,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  {
    "id": 2,
    "tradeName": "Augmentin",
    "scientificName": "Amoxicillin + Clavulanic Acid",
    "concentration": "625mg",
    "size": "14 tablets",
    "refPurchasePrice": 8500.0,
    "refSellingPrice": 10000.0,
    "notes": "Broad spectrum antibiotic",
    "tax": 5.0,
    "barcode": "1234567890124",
    "requiresPrescription": true,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

### Error Responses

#### 400 Bad Request
```json
{
  "error": "Product list cannot be empty"
}
```

#### 403 Forbidden
```json
{
  "error": "Access denied - insufficient permissions"
}
```

#### 500 Internal Server Error
```json
{
  "error": "Internal server error"
}
```

## Features

### Error Handling
- **Graceful Error Handling**: If some products fail to create, the endpoint will continue processing others
- **Duplicate Barcode Detection**: Automatically skips products with duplicate barcodes
- **Individual Product Validation**: Each product is validated independently using the same validation logic as single product creation
- **Detailed Error Logging**: Failed products are logged with specific error messages

### Performance & Safety
- **Code Reuse**: Uses the existing `insertMasterProduct` method to prevent code duplication and ensure consistency
- **Transaction Safety**: Each product is processed independently using the same business logic
- **Memory Efficient**: Processes products one by one to avoid memory issues
- **Production Safe**: Leverages tested and proven single product creation logic

## Usage Examples

### Using curl
```bash
curl -X POST "http://localhost:8080/api/v1/master_products/bulk?lang=en" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d @master_products_seed_data.json
```

### Using the provided seed data
You can use the `master_products_seed_data.json` file created earlier:

```bash
curl -X POST "http://localhost:8080/api/v1/master_products/bulk?lang=ar" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d @master_products_seed_data.json
```

## Notes
- The endpoint requires PLATFORM_ADMIN role
- All products must have unique barcodes
- Translations are optional but recommended for Arabic support
- The endpoint returns only successfully created products
- Failed products are logged to the console for debugging
