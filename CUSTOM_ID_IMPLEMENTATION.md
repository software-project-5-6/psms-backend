# Custom ID Implementation Guide

## Overview

This implementation changes User IDs and Project IDs from auto-increment Long values to custom 4-character strings with the format: **2 letters + 2 numbers**.

### ID Format Examples:

- **Users**: `UA12`, `UB34`, `UZ99` (prefix: `U`)
- **Projects**: `PA12`, `PB34`, `PZ99` (prefix: `P`)

---

## Changes Made

### 1. Backend Files Modified

#### ✅ New Utility Class

- **`IdGenerator.java`** - Generates custom IDs
  - `generateId()` - Creates random 4-char ID (2 letters + 2 digits)
  - `generateIdWithPrefix(String prefix)` - Creates ID with prefix (e.g., "U" for users, "P" for projects)

#### ✅ Entity Classes Updated

- **`User.java`**
  - Changed `id` from `Long` to `String`
  - Added `@PrePersist` method to auto-generate IDs
  - Format: `U` + 1 letter + 2 digits (e.g., `UA12`)
- **`Project.java`**
  - Changed `id` from `Long` to `String`
  - Added `@PrePersist` method to auto-generate IDs
  - Format: `P` + 1 letter + 2 digits (e.g., `PA12`)

#### ✅ DTO Classes Updated

- **`UserDto.java`** - Changed `id` from `Long` to `String`
- **`ProjectDto.java`** - Changed `id` from `Long` to `String`
- **`ProjectWithUsersDto.java`** - Changed `id` from `Long` to `String`
- **`ProjectInvitationDTO.java`** - Changed `projectId` from `Long` to `String`

---

## Migration Steps

### ⚠️ WARNING: This migration will DELETE ALL EXISTING DATA!

**Before proceeding:**

1. ✅ Backup your database
2. ✅ Ensure no active users are in the system
3. ✅ Inform your team about the downtime

### Step 1: Backup Database

```bash
# MySQL backup command
mysqldump -u root -p psms_db > backup_before_migration.sql
```

### Step 2: Run Migration Script

```bash
# Connect to MySQL
mysql -u root -p psms_db

# Run the migration SQL file
source MIGRATION_CUSTOM_IDS.sql
```

Or manually execute the SQL in your MySQL client/workbench.

### Step 3: Restart Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Step 4: Test the Implementation

#### Create a Test User

When a new user signs up through AWS Cognito and logs in, they will automatically get a custom ID like `UA12`, `UB45`, etc.

#### Create a Test Project

```bash
# Example API call
POST http://localhost:8080/api/projects
{
  "projectName": "Test Project",
  "description": "Testing custom IDs"
}

# Response will have ID like "PA12", "PB45", etc.
```

---

## Frontend Changes (Optional)

### If you display IDs in the UI:

The IDs are now **4-character strings** instead of numbers.

#### Before:

```javascript
console.log(project.id); // Output: 123
```

#### After:

```javascript
console.log(project.id); // Output: "PA12"
```

#### Type changes in TypeScript/PropTypes (if applicable):

```typescript
// Before
interface Project {
  id: number;
  projectName: string;
}

// After
interface Project {
  id: string; // Changed from number to string
  projectName: string;
}
```

---

## ID Generation Logic

### How IDs are Generated:

1. **User Registration**: When a user signs up → `@PrePersist` in `User.java` triggers → generates `U` + random letter + 2 random digits
2. **Project Creation**: When a project is created → `@PrePersist` in `Project.java` triggers → generates `P` + random letter + 2 random digits

### Collision Handling:

- **Probability**: With 26 letters × 10 digits × 10 digits = **2,600 possible combinations per prefix**
- **If collision occurs**: Database will throw a unique constraint violation (very rare)
- **Recommended**: Add retry logic in service layer if needed (future enhancement)

### Example Implementation with Retry (Optional):

```java
// In ProjectService.java
public Project createProject(ProjectDto dto) {
    int maxRetries = 5;
    int attempt = 0;

    while (attempt < maxRetries) {
        try {
            Project project = new Project();
            // ... set other fields
            return projectRepository.save(project);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                attempt++;
                // @PrePersist will generate a new ID on next save attempt
            } else {
                throw e;
            }
        }
    }
    throw new RuntimeException("Failed to generate unique project ID after " + maxRetries + " attempts");
}
```

---

## Verification

### 1. Check User IDs:

```sql
SELECT id, email, full_name FROM users;
```

Expected output:

```
+------+----------------------+-------------+
| id   | email                | full_name   |
+------+----------------------+-------------+
| UA12 | john@example.com     | John Doe    |
| UB34 | jane@example.com     | Jane Smith  |
+------+----------------------+-------------+
```

### 2. Check Project IDs:

```sql
SELECT id, project_name FROM projects;
```

Expected output:

```
+------+------------------+
| id   | project_name     |
+------+------------------+
| PA12 | Website Redesign |
| PB45 | Mobile App       |
+------+------------------+
```

### 3. Check Foreign Keys:

```sql
SELECT * FROM project_user_roles;
```

Expected output:

```
+----+------------+---------+---------+
| id | project_id | user_id | role    |
+----+------------+---------+---------+
|  1 | PA12       | UA12    | MANAGER |
|  2 | PA12       | UB34    | VIEWER  |
+----+------------+---------+---------+
```

---

## Rollback Plan

If something goes wrong:

### Option 1: Restore from Backup

```bash
mysql -u root -p psms_db < backup_before_migration.sql
```

### Option 2: Revert Code Changes

1. Revert all entity changes (`User.java`, `Project.java`)
2. Revert all DTO changes
3. Delete `IdGenerator.java`
4. Restore original database schema
5. Rebuild and restart backend

---

## Benefits of Custom IDs

✅ **User-Friendly**: Easy to read and communicate (e.g., "Project PA12")  
✅ **Short & Clean**: Only 4 characters vs long numbers  
✅ **Professional**: Looks more polished in URLs and UIs  
✅ **Memorable**: Easier for users to remember  
✅ **Secure**: Doesn't expose sequential information about system size

---

## Support

If you encounter issues:

1. Check backend logs for errors
2. Verify database schema matches migration script
3. Ensure all foreign key references use VARCHAR(4)
4. Test with fresh user registration
5. Contact development team if problems persist

---

## Next Steps

After successful migration:

1. ✅ Test user registration flow
2. ✅ Test project creation
3. ✅ Test invitation system
4. ✅ Verify frontend displays IDs correctly
5. ✅ Update API documentation if needed
6. ✅ Inform users about the change

---

**Migration Date**: _[To be filled when executed]_  
**Executed By**: _[To be filled when executed]_  
**Status**: _[Pending/Success/Failed]_
