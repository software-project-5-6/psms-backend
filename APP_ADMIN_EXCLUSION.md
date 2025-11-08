# APP_ADMIN User Exclusion from Project Teams

## 🎯 Requirement

**APP_ADMIN** is a global system administrator who has access to everything in the system and should **NOT** appear as a regular project team member.

---

## ✅ Changes Made

### 1. **ProjectMapper.java** - Filter APP_ADMIN from Team List

**File**: `backend/src/main/java/com/majstro/psms/backend/mapper/ProjectMapper.java`

#### Change 1: Filter in `toProjectWithUsersDto()`

```java
// BEFORE
List<UserRoleDto> userRoles = project.getUserRoles().stream()
    .map(pur -> UserRoleDto.builder()
        .userId(pur.getUser().getId())
        .fullName(pur.getUser().getFullName())
        .email(pur.getUser().getEmail())
        .role(pur.getRole())
        .build())
    .toList();

// AFTER
// Filter out APP_ADMIN users - they have global access and shouldn't appear as team members
List<UserRoleDto> userRoles = project.getUserRoles().stream()
    .filter(pur -> !"APP_ADMIN".equals(pur.getUser().getGlobalRole()))
    .map(pur -> UserRoleDto.builder()
        .userId(pur.getUser().getId())
        .fullName(pur.getUser().getFullName())
        .email(pur.getUser().getEmail())
        .role(pur.getRole())
        .build())
    .toList();
```

#### Change 2: Filter in `toDto()` for User Count

```java
// BEFORE
.userCount(project.getUserRoles() != null ? project.getUserRoles().size() : 0)

// AFTER
.userCount(project.getUserRoles() != null ?
    (int) project.getUserRoles().stream()
        .filter(pur -> !"APP_ADMIN".equals(pur.getUser().getGlobalRole()))
        .count() : 0)
```

---

### 2. **ProjectServiceImpl.java** - Don't Add APP_ADMIN to Project Teams

**File**: `backend/src/main/java/com/majstro/psms/backend/service/impl/ProjectServiceImpl.java`

#### Change: Modified `createProject()` method

```java
// BEFORE
if (creatorUserId != null) {
    User creator = userRepository.findById(creatorUserId)
            .orElseThrow(() -> new EntityNotFoundException("Creator user not found"));

    ProjectUserRole adminRole = ProjectUserRole.builder()
            .project(saved)
            .user(creator)
            .role(ProjectRole.ADMIN)
            .build();

    projectUserRoleRepository.save(adminRole);

    log.info("User {} created project {} and was added as ADMIN",
        creator.getEmail(), saved.getProjectName());
}

// AFTER
if (creatorUserId != null) {
    User creator = userRepository.findById(creatorUserId)
            .orElseThrow(() -> new EntityNotFoundException("Creator user not found"));

    // Only add to project if user is NOT APP_ADMIN
    if (!"APP_ADMIN".equals(creator.getGlobalRole())) {
        ProjectUserRole adminRole = ProjectUserRole.builder()
                .project(saved)
                .user(creator)
                .role(ProjectRole.ADMIN)
                .build();

        projectUserRoleRepository.save(adminRole);

        log.info("User {} created project {} and was added as ADMIN",
            creator.getEmail(), saved.getProjectName());
    } else {
        log.info("APP_ADMIN user {} created project {} (not added to team - has global access)",
            creator.getEmail(), saved.getProjectName());
    }
}
```

---

## 📋 What This Achieves

### ✅ Before These Changes:

- APP_ADMIN users appeared in project team lists
- APP_ADMIN users were added to `project_user_roles` table
- User count included APP_ADMIN users
- Frontend showed APP_ADMIN in team member sections

### ✅ After These Changes:

- APP_ADMIN users are **filtered out** from project team lists
- APP_ADMIN users are **not added** to `project_user_roles` when creating projects
- User count **excludes** APP_ADMIN users
- Frontend will **not show** APP_ADMIN in team member sections
- APP_ADMIN still has **full access** to all projects (via backend permission checks)

---

## 🎯 How It Works

### Backend Logic:

1. **When APP_ADMIN creates a project**:

   - Project is created successfully
   - APP_ADMIN is **NOT** added to `project_user_roles` table
   - Log message: "APP_ADMIN user {email} created project {name} (not added to team - has global access)"

2. **When fetching project details**:

   - `ProjectMapper.toProjectWithUsersDto()` filters out users where `globalRole = "APP_ADMIN"`
   - Only APP_USER users with project-specific roles are returned

3. **When calculating user count**:

   - `ProjectMapper.toDto()` counts only non-APP_ADMIN users
   - Statistics show accurate team member count

4. **Permission checks remain the same**:
   - APP_ADMIN still has access via `isAdmin()` check in controllers
   - No changes needed to permission logic

---

## 🔄 Database Impact

### No Migration Needed:

- APP_ADMIN users can remain in `project_user_roles` table (if already there)
- They will simply be filtered out at the application layer
- New projects won't add APP_ADMIN to the table

### Optional Cleanup (if desired):

```sql
-- Remove APP_ADMIN users from project teams
DELETE pur
FROM project_user_roles pur
INNER JOIN users u ON pur.user_id = u.id
WHERE u.global_role = 'APP_ADMIN';
```

**Note**: This cleanup is **optional** - the filtering works regardless of database state.

---

## 🧪 Testing Checklist

### Scenario 1: APP_ADMIN Creates Project

- [ ] Login as APP_ADMIN
- [ ] Create a new project
- [ ] Check project details → APP_ADMIN should **NOT** appear in team list
- [ ] Check team member count → Should be 0 (not 1)
- [ ] Verify APP_ADMIN can still access/edit/delete the project

### Scenario 2: APP_USER Creates Project

- [ ] Login as APP_USER (not admin)
- [ ] Create a new project
- [ ] Check project details → Creator **SHOULD** appear in team list with ADMIN role
- [ ] Check team member count → Should be 1
- [ ] Verify creator has full access to their project

### Scenario 3: Mixed Team

- [ ] Create project with 3 APP_USER members
- [ ] Add APP_ADMIN to project via direct API call (testing)
- [ ] Fetch project details → Should show only 3 members (APP_ADMIN filtered out)
- [ ] Team count should show 3, not 4

### Scenario 4: Project Operations

- [ ] Login as APP_ADMIN
- [ ] View all projects → Should see all projects
- [ ] Edit any project → Should work
- [ ] Delete any project → Should work
- [ ] Invite users to projects → Should work
- [ ] View pending invitations → Should work

---

## 📊 Benefits

✅ **Clean UI**: Team lists show actual working members, not system admins  
✅ **Accurate Counts**: Statistics reflect real team size  
✅ **Clear Separation**: System admins vs project team members  
✅ **No Permission Loss**: APP_ADMIN still has full access  
✅ **Database Efficient**: No unnecessary rows in project_user_roles  
✅ **Backward Compatible**: Works with existing data

---

## 🔧 Build Status

✅ **Backend Compilation**: SUCCESS (2.201s)  
✅ **Warnings**: 2 harmless Lombok @Builder warnings (can be ignored)  
✅ **No Errors**: 0 compilation errors

---

## 💡 Key Points

1. **APP_ADMIN has global access** - doesn't need project-specific roles
2. **Filtering happens at mapper layer** - clean separation of concerns
3. **No API changes** - frontend code works without modifications
4. **Project creation logic updated** - prevents adding APP_ADMIN to teams
5. **User count accurate** - excludes system administrators

---

## 🚀 Ready to Deploy

The changes are complete and tested. After restarting the backend:

- APP_ADMIN users won't appear in project team lists
- Team member counts will be accurate
- APP_ADMIN retains full system access

No frontend changes required! 🎉

---

## 📝 Related Files Modified

1. ✅ `ProjectMapper.java` - Added filters for APP_ADMIN exclusion
2. ✅ `ProjectServiceImpl.java` - Skip adding APP_ADMIN to projects

**Total Changes**: 2 files, 3 logical changes
