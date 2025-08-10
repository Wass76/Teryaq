# Pharmacy Registration Test Plan

## Test Objective
Verify that the `isActive` field is properly set to `true` after a user completes pharmacy registration using the `/complete-registration` endpoint.

## Test Steps

### 1. Initial State Check
- Create a new pharmacy (this should set `isActive = false`)
- Verify that the pharmacy entity has `isActive = false`
- Verify that the `/me` endpoint returns `isAccountActive = false`

### 2. Complete Registration Test
- Call the `/complete-registration` endpoint with all required data:
  ```
  POST /api/v1/pharmacy/complete-registration
  Parameters:
  - newPassword: "newPassword123"
  - location: "123 Main Street, City"
  - managerFirstName: "John"
  - managerLastName: "Doe"
  - pharmacyPhone: "+1234567890"
  - pharmacyEmail: "pharmacy@example.com"
  - openingHours: "9:00 AM - 6:00 PM"
  ```

### 3. Verification Steps
- Verify that the response includes `isActive: true`
- Verify that the pharmacy entity in the database has `isActive = true`
- Verify that the `/me` endpoint now returns `isAccountActive = true`
- Verify that the new pharmacy endpoint returns `isActive: true`

## Expected Results

### Before Complete Registration:
```json
{
  "id": 1,
  "pharmacyName": "Test Pharmacy",
  "licenseNumber": "LIC123",
  "address": null,
  "email": null,
  "phoneNumber": "+1234567890",
  "isActive": false
}
```

### After Complete Registration:
```json
{
  "id": 1,
  "pharmacyName": "Test Pharmacy",
  "licenseNumber": "LIC123",
  "address": "123 Main Street, City",
  "email": "pharmacy@example.com",
  "phoneNumber": "+1234567890",
  "openingHours": "9:00 AM - 6:00 PM",
  "isActive": true
}
```

## Database Verification
```sql
-- Check pharmacy table
SELECT id, name, license_number, address, phone_number, is_active 
FROM pharmacy 
WHERE id = 1;

-- Expected result: is_active should be true
```

## API Endpoints to Test
1. `POST /api/v1/pharmacy/complete-registration` - Complete registration
2. `GET /api/v1/user/me` - Check user info and account activation
3. `GET /api/v1/pharmacy/{pharmacyId}` - Check pharmacy details
4. `GET /api/v1/pharmacy/all` - Check all pharmacies (admin only)

## Notes
- The `isActive` field is automatically calculated based on whether the pharmacy has complete registration data
- Required fields for activation: `name`, `licenseNumber`, `address`, `phoneNumber`
- The field is updated in real-time when registration is completed
- All pharmacy-related responses now include the `isActive` status
