-- Migration Script: Change ID columns from BIGINT to VARCHAR(4)
-- WARNING: This will delete all existing data. Backup your database first!

-- Step 1: Drop foreign key constraints
ALTER TABLE project_user_roles DROP FOREIGN KEY IF EXISTS fk_project_user_roles_project;
ALTER TABLE project_user_roles DROP FOREIGN KEY IF EXISTS fk_project_user_roles_user;
ALTER TABLE project_invitations DROP FOREIGN KEY IF EXISTS fk_project_invitations_project;
ALTER TABLE project_invitations DROP FOREIGN KEY IF EXISTS fk_project_invitations_invited_by;

-- Step 2: Drop existing tables to recreate with new ID types
DROP TABLE IF EXISTS project_user_roles;
DROP TABLE IF EXISTS project_invitations;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS users;

-- Step 3: Recreate users table with VARCHAR(4) ID
CREATE TABLE users (
    id VARCHAR(4) PRIMARY KEY,
    cognito_sub VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    full_name VARCHAR(150),
    global_role VARCHAR(50) DEFAULT 'APP_USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Step 4: Recreate projects table with VARCHAR(4) ID
CREATE TABLE projects (
    id VARCHAR(4) PRIMARY KEY,
    project_name VARCHAR(150) NOT NULL UNIQUE,
    description VARCHAR(500),
    client_name VARCHAR(150),
    client_email VARCHAR(150),
    client_phone VARCHAR(150),
    icon_url VARCHAR(255),
    price DOUBLE,
    artifact_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Step 5: Recreate project_user_roles table with VARCHAR(4) foreign keys
CREATE TABLE project_user_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id VARCHAR(4) NOT NULL,
    user_id VARCHAR(4) NOT NULL,
    role VARCHAR(50) NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_project_user (project_id, user_id)
);

-- Step 6: Recreate project_invitations table with VARCHAR(4) foreign keys
CREATE TABLE project_invitations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id VARCHAR(4) NOT NULL,
    email VARCHAR(150) NOT NULL,
    role VARCHAR(50) NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    invited_by_id VARCHAR(4) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (invited_by_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Step 7: Create indexes for better performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_cognito_sub ON users(cognito_sub);
CREATE INDEX idx_projects_name ON projects(project_name);
CREATE INDEX idx_project_user_roles_project ON project_user_roles(project_id);
CREATE INDEX idx_project_user_roles_user ON project_user_roles(user_id);
CREATE INDEX idx_invitations_project ON project_invitations(project_id);
CREATE INDEX idx_invitations_email ON project_invitations(email);
CREATE INDEX idx_invitations_token ON project_invitations(token);

-- Migration complete!
-- Note: All data has been cleared. New users and projects will get 4-character IDs (e.g., UA12, PB45)
