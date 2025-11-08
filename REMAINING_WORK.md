# Custom ID Implementation - Remaining Work

## ✅ Completed So Far

1. ✅ Created `IdGenerator.java` utility class
2. ✅ Updated `Project` entity (changed `id` from `Long` to `String`, added `@PrePersist`)
3. ✅ Updated `User` entity (changed `id` from `Long` to `String`, added `@PrePersist`)
4. ✅ Updated DTOs: `ProjectDto`, `UserDto`, `ProjectWithUsersDto`, `ProjectInvitationDTO`
5. ✅ Updated `ProjectRepository` to `JpaRepository<Project, String>`
6. ✅ Updated `UserRepository` to `JpaRepository<User, String>`
7. ✅ Updated `IProjectService` interface method signatures
8. ✅ Updated `ProjectServiceImpl` implementation
9. ✅ Partially updated `ProjectController`
10. ✅ Created database migration SQL script
11. ✅ Created comprehensive documentation

## ⚠️ Remaining Compilation Errors (11 total)

### Errors to Fix:

#### 1-2. ProjectController.java (lines 48, 54)

- **Issue**: Some methods still expect/return `Long` instead of `String`
- **Fix**: Update method signatures and path variables

#### 3. ProjectInvitationController.java (line 88)

- **Issue**: Trying to convert String to Long
- **Fix**: Update parameter type from `Long` to `String`

#### 4-5. ProjectMapper.java (lines 52, 57)

- **Issue**: Trying to convert String/IDs in mapper methods
- **Fix**: Update mapping logic for String IDs

#### 6-8. ProjectInvitationServiceImpl.java (lines 48, 53, 199)

- **Issue**: Methods expecting Long but receiving String
- **Fix**: Update service method parameters

#### 9-11. UserServiceImpl.java (lines 36, 65, 68)

- **Issue**: User ID type mismatches
- **Fix**: Update all ID-related parameters to `String`

---

## 🔧 Quick Fix Guide

### Step 1: Find All Remaining `Long` ID References

```bash
cd backend
grep -r "Long.*Id" src/main/java/com/majstro/psms/backend --include="*.java" | grep -v "invitationId"
```

### Step 2: Replace Pattern

For each file with errors, replace:

- `Long projectId` → `String projectId`
- `Long userId` → `String userId`
- `@PathVariable Long` → `@PathVariable String`

### Step 3: Update UserService Interface

```bash
nano src/main/java/com/majstro/psms/backend/service/IUserService.java
```

Change all ID parameters from `Long` to `String`

### Step 4: Update Mapper Classes

```bash
nano src/main/java/com/majstro/psms/backend/mapper/ProjectMapper.java
nano src/main/java/com/majstro/psms/backend/mapper/UserMapper.java
```

Update any ID-related mapping logic

### Step 5: Fix Controllers

```bash
nano src/main/java/com/majstro/psms/backend/controller/ProjectController.java
nano src/main/java/com/majstro/psms/backend/controller/ProjectInvitationController.java
nano src/main/java/com/majstro/psms/backend/controller/UserController.java
```

Update all `@PathVariable Long id` to `@PathVariable String id`

### Step 6: Fix Service Implementations

```bash
nano src/main/java/com/majstro/psms/backend/service/impl/UserServiceImpl.java
nano src/main/java/com/majstro/psms/backend/service/impl/ProjectInvitationServiceImpl.java
```

Update method signatures to use `String` for IDs

### Step 7: Rebuild

```bash
mvn clean compile
```

---

## 🎯 Systematic Approach

Since this is a large refactoring, here's the systematic approach:

### 1. Update All Service Interfaces

- IUserService.java
- IProjectInvitationService.java
- Any other service interfaces

### 2. Update All Service Implementations

- UserServiceImpl.java
- ProjectInvitationServiceImpl.java

### 3. Update All Controllers

- UserController.java
- ProjectInvitationController.java
- Complete ProjectController.java

### 4. Update All Mappers

- UserMapper.java
- ProjectMapper.java

### 5. Update Any Other Repository Interfaces

Search for other repositories that might reference Project or User entities

---

## 🚀 After Fixing Compilation Errors

### 1. Run Full Build

```bash
mvn clean install
```

### 2. Run Database Migration

```bash
mysql -u root -p psms_db < MIGRATION_CUSTOM_IDS.sql
```

### 3. Test the Application

```bash
mvn spring-boot:run
```

### 4. Test API Endpoints

```bash
# Create a user (via signup)
# Should get ID like "UA12"

# Create a project
POST http://localhost:8080/api/projects
# Should get ID like "PA12"

# Get project by ID
GET http://localhost:8080/api/projects/PA12
```

---

## 📝 Files Modified Summary

### ✅ Completed:

1. IdGenerator.java (NEW)
2. Project.java
3. User.java
4. ProjectDto.java
5. UserDto.java
6. ProjectWithUsersDto.java
7. ProjectInvitationDTO.java
8. ProjectRepository.java
9. UserRepository.java
10. IProjectService.java
11. ProjectServiceImpl.java
12. ProjectController.java (Partially)

### ⏳ Need Completion:

13. ProjectController.java (lines 48, 54)
14. ProjectInvitationController.java
15. UserController.java
16. ProjectMapper.java
17. UserMapper.java
18. IUserService.java
19. UserServiceImpl.java
20. IProjectInvitationService.java
21. ProjectInvitationServiceImpl.java

---

## 💡 Tips

1. **Use Find & Replace**: In VS Code, use Ctrl+Shift+H to find/replace across files

   - Find: `Long (projectId|userId|id)`
   - Replace: `String $1`
   - With regex enabled

2. **Check One File at a Time**: Fix compilation errors in order:

   - Interfaces first
   - Implementations second
   - Controllers last

3. **Test Incrementally**: After fixing each file, run `mvn compile` to check progress

4. **Foreign Key Tables**: Don't forget tables that reference Project/User IDs:
   - project_user_roles
   - project_invitations
   - These use `project_id` and `user_id` as VARCHAR(4) foreign keys

---

## Need Help?

If you encounter issues:

1. Share the specific compilation error
2. Share the file name and line number
3. I can provide the exact fix for that specific location

Would you like me to continue fixing the remaining files? Or would you prefer to:

1. Fix them manually using this guide?
2. Have me fix specific files that are causing the most errors?
3. Take a break and continue later?

Let me know how you'd like to proceed!
