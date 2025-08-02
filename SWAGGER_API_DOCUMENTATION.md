# Teryaq Pharmacy Management System - Swagger API Documentation

## Overview

The Teryaq Pharmacy Management System is a comprehensive platform for managing pharmacy operations, including user management, product management, sales, purchases, inventory, and customer debt management. This document provides a complete overview of all available API endpoints.

## Base URL
- **Production**: `http://159.198.75.161:13000`
- **Local**: `http://localhost:3000`

## Authentication
All API endpoints require JWT Bearer token authentication. Include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## API Endpoints Summary

### 1. User Management APIs

#### User Controller (`/api/v1/users`)
- `GET /api/v1/users/me` - Get current user information

#### Role Controller (`/api/v1/roles`)
- `GET /api/v1/roles` - Get all roles (PLATFORM_ADMIN only)
- `GET /api/v1/roles/{id}` - Get role by ID (PLATFORM_ADMIN only)
- `POST /api/v1/roles` - Create new role (PLATFORM_ADMIN only)
- `PUT /api/v1/roles/{id}` - Update role (PLATFORM_ADMIN only)
- `DELETE /api/v1/roles/{id}` - Delete role (PLATFORM_ADMIN only)
- `GET /api/v1/roles/{id}/permissions` - Get role permissions (PLATFORM_ADMIN only)
- `PUT /api/v1/roles/{id}/permissions` - Update role permissions (PLATFORM_ADMIN only)

#### Supplier Controller (`/api/suppliers`)
- `GET /api/suppliers` - Get all suppliers
- `GET /api/suppliers/{id}` - Get supplier by ID
- `POST /api/suppliers` - Create new supplier
- `PUT /api/suppliers/{id}` - Update supplier
- `DELETE /api/suppliers/{id}` - Delete supplier
- `GET /api/suppliers/search` - Search suppliers by name

#### Pharmacy Controller (`/api/v1/pharmacy`)
- `POST /api/v1/pharmacy/login` - Pharmacy manager login
- `POST /api/v1/pharmacy/complete-registration` - Complete pharmacy registration (PHARMACY_MANAGER only)
- `GET /api/v1/pharmacy/all` - Get all pharmacies (PLATFORM_ADMIN only)

#### Employee Controller (`/api/v1/employees`)
- `POST /api/v1/employees` - Add new employee (PHARMACY_MANAGER only)
- `GET /api/v1/employees` - Get all employees in pharmacy (PHARMACY_MANAGER only)
- `PUT /api/v1/employees/{employeeId}` - Update employee (PHARMACY_MANAGER only)
- `DELETE /api/v1/employees/{employeeId}` - Delete employee (PHARMACY_MANAGER only)
- `POST /api/v1/employees/{employeeId}/working-hours` - Create working hours for employee (PHARMACY_MANAGER only)

#### Admin Controller (`/api/v1/admin`)
- `POST /api/v1/admin/login` - Admin login
- `POST /api/v1/admin/pharmacies` - Create new pharmacy (PLATFORM_ADMIN only)

### 2. Customer Management APIs

#### Customer Controller (`/api/v1/customers`)
- `GET /api/v1/customers` - Get all customers
- `GET /api/v1/customers/{id}` - Get customer by ID
- `GET /api/v1/customers/search` - Search customers by name
- `GET /api/v1/customers/with-debts` - Get customers with debts
- `GET /api/v1/customers/with-active-debts` - Get customers with active debts
- `GET /api/v1/customers/debt-range` - Get customers by debt range
- `POST /api/v1/customers` - Create customer
- `PUT /api/v1/customers/{id}` - Update customer
- `DELETE /api/v1/customers/{id}` - Delete customer

#### Customer Debt Controller (`/api/v1/customer-debts`)
- `POST /api/v1/customer-debts` - Create new customer debt
- `GET /api/v1/customer-debts/{debtId}` - Get debt by ID
- `GET /api/v1/customer-debts/customer/{customerId}` - Get customer debts
- `GET /api/v1/customer-debts/customer/{customerId}/status/{status}` - Get customer debts by status
- `GET /api/v1/customer-debts/customer/{customerId}/total` - Get customer total debt
- `POST /api/v1/customer-debts/pay` - Pay debt
- `GET /api/v1/customer-debts/overdue` - Get overdue debts
- `GET /api/v1/customer-debts/overdue/total` - Get total overdue debts
- `GET /api/v1/customer-debts/status/{status}` - Get debts by status
- `GET /api/v1/customer-debts/date-range` - Get debts by date range
- `GET /api/v1/customer-debts/amount-range` - Get debts by amount range
- `PUT /api/v1/customer-debts/{debtId}/status` - Update debt status
- `GET /api/v1/customer-debts/statistics` - Get debt statistics
- `DELETE /api/v1/customer-debts/{debtId}` - Delete debt

### 3. Sale Management APIs

#### Sale Controller (`/api/sales`)
- `POST /api/sales` - Create new sale invoice (EMPLOYEE only)
- `GET /api/sales/{id}` - Get sale invoice by ID (EMPLOYEE only)
- `POST /api/sales/{id}/cancel` - Cancel sale invoice (EMPLOYEE only)

#### Payment Controller (`/api/payment`)
- `GET /api/payment/types` - Get available payment types
- `GET /api/payment/methods` - Get available payment methods
- `GET /api/payment/methods/{paymentType}` - Get compatible payment methods
- `POST /api/payment/validate` - Validate payment

### 4. Purchase Management APIs

#### Purchase Order Controller (`/api/purchase-orders`)
- `POST /api/purchase-orders` - Create new purchase order
- `GET /api/purchase-orders/{id}` - Get purchase order by ID
- `GET /api/purchase-orders` - Get all purchase orders
- `GET /api/purchase-orders/paginated` - Get paginated purchase orders
- `GET /api/purchase-orders/status/{status}` - Get purchase orders by status
- `GET /api/purchase-orders/status/{status}/paginated` - Get paginated purchase orders by status
- `POST /api/purchase-orders/{id}/cancel` - Cancel purchase order

#### Purchase Invoice Controller (`/api/purchase-invoices`)
- `POST /api/purchase-invoices` - Create new purchase invoice
- `GET /api/purchase-invoices/{id}` - Get purchase invoice by ID
- `GET /api/purchase-invoices` - Get all purchase invoices
- `GET /api/purchase-invoices/paginated` - Get paginated purchase invoices

### 5. Product Management APIs

#### Product Type Controller (`/api/v1/types`)
- `GET /api/v1/types` - Get all product types
- `GET /api/v1/types/{id}` - Get product type by ID
- `POST /api/v1/types` - Create new product type (PLATFORM_ADMIN only)
- `PUT /api/v1/types/{id}` - Update product type (PLATFORM_ADMIN only)
- `DELETE /api/v1/types/{id}` - Delete product type (PLATFORM_ADMIN only)

#### Product Category Controller (`/api/v1/categories`)
- `GET /api/v1/categories` - Get all product categories
- `GET /api/v1/categories/{id}` - Get product category by ID
- `POST /api/v1/categories` - Create new product category (PLATFORM_ADMIN only)
- `PUT /api/v1/categories/{id}` - Update product category (PLATFORM_ADMIN only)
- `DELETE /api/v1/categories/{id}` - Delete product category (PLATFORM_ADMIN only)

#### Product Form Controller (`/api/v1/Forms`)
- `GET /api/v1/Forms` - Get all product forms
- `GET /api/v1/Forms/{id}` - Get product form by ID
- `POST /api/v1/Forms` - Create new product form (PLATFORM_ADMIN only)
- `PUT /api/v1/Forms/{id}` - Update product form (PLATFORM_ADMIN only)
- `DELETE /api/v1/Forms/{id}` - Delete product form (PLATFORM_ADMIN only)

#### Manufacturer Controller (`/api/v1/manufacturers`)
- `GET /api/v1/manufacturers` - Get all manufacturers
- `GET /api/v1/manufacturers/{id}` - Get manufacturer by ID
- `POST /api/v1/manufacturers` - Create new manufacturer (PLATFORM_ADMIN or PHARMACY_MANAGER only)
- `PUT /api/v1/manufacturers/{id}` - Update manufacturer (PLATFORM_ADMIN only)
- `DELETE /api/v1/manufacturers/{id}` - Delete manufacturer (PLATFORM_ADMIN only)

#### Master Product Controller (`/api/v1/master_products`)
- `GET /api/v1/master_products` - Get all master products
- `GET /api/v1/master_products/{id}` - Get master product by ID
- `POST /api/v1/master_products` - Create new master product (PLATFORM_ADMIN only)
- `PUT /api/v1/master_products/{id}` - Update master product (PLATFORM_ADMIN only)
- `DELETE /api/v1/master_products/{id}` - Delete master product (PLATFORM_ADMIN only)

#### Pharmacy Product Controller (`/api/v1/pharmacy_products`)
- `GET /api/v1/pharmacy_products` - Get all pharmacy products
- `GET /api/v1/pharmacy_products/pharmacy/{pharmacyId}` - Get pharmacy products by pharmacy ID (PLATFORM_ADMIN only)
- `GET /api/v1/pharmacy_products/{id}` - Get pharmacy product by ID
- `POST /api/v1/pharmacy_products` - Create new pharmacy product
- `PUT /api/v1/pharmacy_products/{id}` - Update pharmacy product
- `DELETE /api/v1/pharmacy_products/{id}` - Delete pharmacy product

#### Product Search Controller (`/api/v1/search`)
- `GET /api/v1/search/products` - Search products by keyword
- `GET /api/v1/search/all-products` - Get all products

### 6. Stock Management APIs

#### Stock Controller (`/api/v1/stock`)
- `GET /api/v1/stock/product/{productId}` - Get stock items for a specific product
- `GET /api/v1/stock/product/{productId}/available` - Get available stock for a specific product
- `GET /api/v1/stock/product/{productId}/quantity` - Get total quantity for a specific product
- `GET /api/v1/stock/product/{productId}/check-availability` - Check if quantity is available for a product
- `GET /api/v1/stock/expired` - Get expired items
- `GET /api/v1/stock/expiring-soon` - Get items expiring soon
- `GET /api/v1/stock/report/{productType}` - Get stock report by product type
- `GET /api/v1/stock/comprehensive-report` - Get comprehensive stock report
- `GET /api/v1/stock/all` - Get all stock items
- `GET /api/v1/stock/all-with-product-info` - Get all stock items with product information

### 7. Language Management APIs

#### Language Controller (`/api/v1/languages`)
- `GET /api/v1/languages` - Get all languages
- `GET /api/v1/languages/{id}` - Get language by ID

## Response Codes

- **200** - Success
- **201** - Created
- **204** - No Content
- **400** - Bad Request
- **401** - Unauthorized
- **403** - Forbidden
- **404** - Not Found
- **409** - Conflict
- **429** - Too Many Requests
- **500** - Internal Server Error

## Role-Based Access Control

The system implements role-based access control with the following roles:

- **PLATFORM_ADMIN**: Full system access
- **PHARMACY_MANAGER**: Pharmacy-specific operations
- **EMPLOYEE**: Sales and basic operations

## Language Support

Most endpoints support language localization through the `lang` parameter:
- `en` - English (default)
- `ar` - Arabic

## Pagination

Many list endpoints support pagination with the following parameters:
- `page` - Page number (0-based)
- `size` - Number of items per page
- `sortBy` - Sort field
- `direction` - Sort direction (asc/desc)

## Examples

### Authentication
```bash
curl -X POST "http://localhost:3000/api/v1/pharmacy/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "manager@pharmacy.com", "password": "password123"}'
```

### Get Products
```bash
curl -X GET "http://localhost:3000/api/v1/master_products?lang=en&page=0&size=10" \
  -H "Authorization: Bearer <your-jwt-token>"
```

### Create Sale
```bash
curl -X POST "http://localhost:3000/api/sales" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 2,
        "unitPrice": 10.50
      }
    ],
    "paymentType": "CASH",
    "paymentMethod": "CASH"
  }'
```

## Swagger UI

Access the interactive Swagger UI documentation at:
- **Production**: `http://159.198.75.161:13000/swagger-ui.html`
- **Local**: `http://localhost:3000/swagger-ui.html`

## Contact

For technical support or questions about the API:
- **Name**: Wassem Tenbakji
- **Email**: wasee.tenbakji@gmail.com
- **LinkedIn**: https://www.linkedin.com/in/wassem-tenbakji-a078a5206

## License

All Copyrights reserved to Wassem Tenbakji
Terms of Service: 1 month after applying 