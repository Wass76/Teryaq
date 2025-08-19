# Working Hours Endpoint Update

## Overview
The `PUT /{employeeId}/working-hours` endpoint has been updated to accept multiple working hours configurations, similar to how working hours are handled when creating a new employee.

## Changes Made

### 1. New DTO: `UpsertWorkingHoursRequestDTO`
- **File**: `src/main/java/com/Teryaq/user/dto/UpsertWorkingHoursRequestDTO.java`
- **Purpose**: Accepts a list of working hours requests instead of a single request
- **Structure**: Contains `List<CreateWorkingHoursRequestDTO> workingHoursRequests`

### 2. Updated Controller Method
- **File**: `src/main/java/com/Teryaq/user/controller/EmployeeController.java`
- **Method**: `upsertWorkingHoursForEmployee`
- **Change**: Now accepts `UpsertWorkingHoursRequestDTO` instead of `CreateWorkingHoursRequestDTO`
- **API Documentation**: Updated to reflect multiple working hours support

### 3. Updated Service Method
- **File**: `src/main/java/com/Teryaq/user/service/EmployeeService.java`
- **Method**: `upsertWorkingHoursForEmployee`
- **Change**: Now processes multiple working hours requests in a loop
- **Logic**: Each request is processed using the existing `upsertWorkingHoursForMultipleDays` method

## API Usage

### Endpoint
```
PUT /api/v1/employees/{employeeId}/working-hours
```

### Request Body Structure
```json
{
  "workingHoursRequests": [
    {
      "daysOfWeek": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
      "shifts": [
        {
          "startTime": "09:00",
          "endTime": "17:00",
          "description": "Morning Shift"
        }
      ]
    },
    {
      "daysOfWeek": ["SATURDAY"],
      "shifts": [
        {
          "startTime": "10:00",
          "endTime": "16:00",
          "description": "Weekend Shift"
        }
      ]
    }
  ]
}
```

### Example Request for Employee ID 102
```json
{
  "workingHoursRequests": [
    {
      "daysOfWeek": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
      "shifts": [
        {
          "startTime": "08:00",
          "endTime": "16:00",
          "description": "Regular Shift"
        }
      ]
    },
    {
      "daysOfWeek": ["SATURDAY", "SUNDAY"],
      "shifts": [
        {
          "startTime": "09:00",
          "endTime": "15:00",
          "description": "Weekend Shift"
        }
      ]
    }
  ]
}
```

## Benefits

1. **Consistency**: Now matches the create employee endpoint behavior
2. **Flexibility**: Can set different working hours for different groups of days
3. **Efficiency**: Single API call to set multiple working hours configurations
4. **Maintainability**: Reuses existing working hours logic

## Validation

- **NotEmpty**: Working hours requests list cannot be empty
- **Individual Validation**: Each `CreateWorkingHoursRequestDTO` is validated separately
- **Time Format**: Times must be in `HH:mm` format (e.g., "09:00", "17:00")
- **Day Validation**: Days must be valid `DayOfWeek` enum values

## Error Handling

- **400**: Invalid request data or validation errors
- **403**: Insufficient permissions (requires PHARMACY_MANAGER role)
- **404**: Employee not found
- **500**: Internal server error

## Migration Notes

- **Backward Compatibility**: This is a breaking change
- **Old Format**: Previously accepted single working hours request
- **New Format**: Now requires wrapping in `workingHoursRequests` array
- **Testing**: Ensure all existing integrations are updated to use new format
