# Complaint Management API Documentation

## Overview
The Complaint Management API provides comprehensive functionality for managing pharmacy complaints. This system allows pharmacy managers and employees to create, view, and manage complaints, while providing management with tools to respond and track complaint status.

## Features
- **Create Complaints**: Pharmacy managers and employees can create complaints
- **View Complaints**: Access complaints based on user role and pharmacy association
- **Update Complaints**: Management can update complaint status and add responses
- **Delete Complaints**: Authorized users can delete complaints
- **Statistics**: Get complaint statistics by status
- **Filtering**: Filter complaints by status, date range, and pharmacy
- **Audit Trail**: Comprehensive audit logging for all complaint operations

## Authentication & Authorization
All endpoints require JWT authentication. The system uses role-based access control:

- **PHARMACY_MANAGER**: Can create, view, update, and delete complaints for their pharmacy
- **PHARMACY_EMPLOYEE**: Can create and view complaints for their pharmacy
- **PLATFORM_ADMIN**: Can perform all operations across all pharmacies

## API Endpoints

### 1. Create Complaint
**POST** `/api/complaints`

Creates a new complaint. The pharmacy ID is automatically filled based on the current user's pharmacy.

**Authorization**: `PHARMACY_MANAGER`, `PHARMACY_EMPLOYEE`

**Request Body**:
```json
{
  "title": "System Performance Issue",
  "description": "The system is running very slowly during peak hours, affecting customer service.",
  "additionalData": "{\"priority\": \"high\", \"category\": \"technical\"}"
}
```

**Response** (201 Created):
```json
{
  "success": true,
  "message": "Complaint created successfully",
  "data": {
    "id": 1,
    "title": "System Performance Issue",
    "description": "The system is running very slowly during peak hours, affecting customer service.",
    "pharmacyId": 1,
    "createdBy": 1,
    "status": "PENDING",
    "response": null,
    "respondedBy": null,
    "respondedAt": null,
    "createdAt": "2024-12-01T10:30:00",
    "updatedAt": "2024-12-01T10:30:00",
    "updatedBy": null,
    "additionalData": "{\"priority\": \"high\", \"category\": \"technical\"}"
  }
}
```

### 2. Get Complaint by ID
**GET** `/api/complaints/{id}`

Retrieves a specific complaint by its ID.

**Authorization**: `PHARMACY_MANAGER`, `PHARMACY_EMPLOYEE`, `PLATFORM_ADMIN`

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Complaint retrieved successfully",
  "data": {
    "id": 1,
    "title": "System Performance Issue",
    "description": "The system is running very slowly during peak hours, affecting customer service.",
    "pharmacyId": 1,
    "createdBy": 1,
    "status": "PENDING",
    "response": null,
    "respondedBy": null,
    "respondedAt": null,
    "createdAt": "2024-12-01T10:30:00",
    "updatedAt": "2024-12-01T10:30:00",
    "updatedBy": null,
    "additionalData": "{\"priority\": \"high\", \"category\": \"technical\"}"
  }
}
```

### 3. Get All Complaints
**GET** `/api/complaints`

Retrieves all complaints for the current user's pharmacy with pagination.

**Authorization**: `PHARMACY_MANAGER`, `PHARMACY_EMPLOYEE`, `PLATFORM_ADMIN`

**Query Parameters**:
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `sort`: Sort criteria (optional)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Complaints retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "System Performance Issue",
        "description": "The system is running very slowly during peak hours, affecting customer service.",
        "pharmacyId": 1,
        "createdBy": 1,
        "status": "PENDING",
        "response": null,
        "respondedBy": null,
        "respondedAt": null,
        "createdAt": "2024-12-01T10:30:00",
        "updatedAt": "2024-12-01T10:30:00",
        "updatedBy": null,
        "additionalData": "{\"priority\": \"high\", \"category\": \"technical\"}"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": {
        "sorted": false
      }
    },
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true,
    "numberOfElements": 1
  }
}
```

### 4. Get Complaints by Status
**GET** `/api/complaints/status/{status}`

Retrieves complaints filtered by status for the current user's pharmacy.

**Authorization**: `PHARMACY_MANAGER`, `PHARMACY_EMPLOYEE`, `PLATFORM_ADMIN`

**Path Parameters**:
- `status`: Complaint status (`PENDING`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `REJECTED`)

**Query Parameters**:
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Complaints retrieved successfully",
  "data": [
    {
      "id": 1,
      "title": "System Performance Issue",
      "description": "The system is running very slowly during peak hours, affecting customer service.",
      "pharmacyId": 1,
      "createdBy": 1,
      "status": "PENDING",
      "response": null,
      "respondedBy": null,
      "respondedAt": null,
      "createdAt": "2024-12-01T10:30:00",
      "updatedAt": "2024-12-01T10:30:00",
      "updatedBy": null,
      "additionalData": "{\"priority\": \"high\", \"category\": \"technical\"}"
    }
  ]
}
```

### 5. Update Complaint
**PUT** `/api/complaints/{id}`

Updates complaint status and adds management response.

**Authorization**: `PHARMACY_MANAGER`, `PLATFORM_ADMIN`

**Request Body**:
```json
{
  "status": "IN_PROGRESS",
  "response": "We are investigating this issue and will provide an update soon.",
  "additionalData": "{\"priority\": \"high\", \"category\": \"technical\", \"assignedTo\": \"IT Team\"}"
}
```

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Complaint updated successfully",
  "data": {
    "id": 1,
    "title": "System Performance Issue",
    "description": "The system is running very slowly during peak hours, affecting customer service.",
    "pharmacyId": 1,
    "createdBy": 1,
    "status": "IN_PROGRESS",
    "response": "We are investigating this issue and will provide an update soon.",
    "respondedBy": 2,
    "respondedAt": "2024-12-01T11:00:00",
    "createdAt": "2024-12-01T10:30:00",
    "updatedAt": "2024-12-01T11:00:00",
    "updatedBy": 2,
    "additionalData": "{\"priority\": \"high\", \"category\": \"technical\", \"assignedTo\": \"IT Team\"}"
  }
}
```

### 6. Delete Complaint
**DELETE** `/api/complaints/{id}`

Deletes a complaint. Only complaint creators, pharmacy managers, or platform admins can delete complaints.

**Authorization**: `PHARMACY_MANAGER`, `PLATFORM_ADMIN`

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Complaint deleted successfully",
  "data": null
}
```

### 7. Get Complaint Statistics
**GET** `/api/complaints/statistics`

Retrieves complaint statistics (count by status) for the current user's pharmacy.

**Authorization**: `PHARMACY_MANAGER`, `PLATFORM_ADMIN`

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Statistics retrieved successfully",
  "data": {
    "PENDING": 5,
    "IN_PROGRESS": 2,
    "RESOLVED": 10,
    "CLOSED": 8,
    "REJECTED": 1
  }
}
```

### 8. Get Complaints Needing Response
**GET** `/api/complaints/needing-response`

Retrieves complaints that are pending or in progress and need management response.

**Authorization**: `PHARMACY_MANAGER`, `PLATFORM_ADMIN`

**Response** (200 OK):
```json
{
  "success": true,
  "message": "Complaints retrieved successfully",
  "data": [
    {
      "id": 1,
      "title": "System Performance Issue",
      "description": "The system is running very slowly during peak hours, affecting customer service.",
      "pharmacyId": 1,
      "createdBy": 1,
      "status": "PENDING",
      "response": null,
      "respondedBy": null,
      "respondedAt": null,
      "createdAt": "2024-12-01T10:30:00",
      "updatedAt": "2024-12-01T10:30:00",
      "updatedBy": null,
      "additionalData": "{\"priority\": \"high\", \"category\": \"technical\"}"
    }
  ]
}
```

## Data Models

### ComplaintStatus Enum
```java
public enum ComplaintStatus {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    CLOSED("Closed"),
    REJECTED("Rejected");
}
```

### Complaint Entity
```java
@Entity
@Table(name = "complaints")
public class Complaint {
    private Long id;
    private String title;
    private String description;
    private Long pharmacyId;
    private Long createdBy;
    private ComplaintStatus status;
    private String response;
    private Long respondedBy;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long updatedBy;
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private String userType;
    private String additionalData;
}
```

## Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "error": "Invalid input data",
  "message": "Title is required"
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "error": "User not authenticated"
}
```

### 403 Forbidden
```json
{
  "success": false,
  "error": "User not authorized to create complaints"
}
```

### 404 Not Found
```json
{
  "success": false,
  "error": "Complaint not found with ID: 999"
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "error": "Failed to create complaint: Database connection error"
}
```

## Security Features

1. **JWT Authentication**: All endpoints require valid JWT tokens
2. **Role-Based Access Control**: Different permissions based on user roles
3. **Pharmacy Isolation**: Users can only access complaints from their pharmacy
4. **Audit Logging**: Comprehensive tracking of all operations
5. **Input Validation**: Server-side validation for all input data
6. **SQL Injection Protection**: Using JPA repositories with parameterized queries

## Database Schema

The complaints table includes:
- Primary key and foreign key relationships
- Comprehensive audit fields
- Indexes for performance optimization
- Proper data types and constraints

## Usage Examples

### Creating a Complaint
```bash
curl -X POST "http://localhost:8080/api/complaints" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "System Performance Issue",
    "description": "The system is running very slowly during peak hours.",
    "additionalData": "{\"priority\": \"high\"}"
  }'
```

### Updating Complaint Status
```bash
curl -X PUT "http://localhost:8080/api/complaints/1" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "IN_PROGRESS",
    "response": "We are investigating this issue."
  }'
```

### Getting Complaint Statistics
```bash
curl -X GET "http://localhost:8080/api/complaints/statistics" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

This comprehensive complaint management system provides a complete solution for handling pharmacy complaints with proper security, audit trails, and role-based access control.
