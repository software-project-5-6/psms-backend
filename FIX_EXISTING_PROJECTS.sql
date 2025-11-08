-- Fix for existing projects: Add creators as ADMIN members
-- This script helps you manually add yourself as ADMIN to existing projects

-- Step 1: Check which projects have no members
SELECT p.id, p.project_name, COUNT(pur.id) as member_count
FROM projects p
LEFT JOIN project_user_roles pur ON p.id = pur.project_id
GROUP BY p.id, p.project_name
HAVING COUNT(pur.id) = 0;

-- Step 2: Find your user ID
-- Replace 'your-email@example.com' with your actual email
SELECT id, email, full_name FROM users WHERE email = 'your-email@example.com';

-- Step 3: Add yourself as ADMIN to a specific project
-- Replace @YOUR_USER_ID with your user ID from Step 2
-- Replace @PROJECT_ID with the project ID from Step 1
-- Example: INSERT INTO project_user_roles (project_id, user_id, role) VALUES (1, 5, 'ADMIN');

INSERT INTO project_user_roles (project_id, user_id, role)
VALUES (@PROJECT_ID, @YOUR_USER_ID, 'ADMIN');

-- Step 4: Add yourself as ADMIN to ALL projects that have no members
-- WARNING: Only use this if you're the only user and created all projects
-- Replace @YOUR_USER_ID with your actual user ID

INSERT INTO project_user_roles (project_id, user_id, role)
SELECT p.id, @YOUR_USER_ID, 'ADMIN'
FROM projects p
LEFT JOIN project_user_roles pur ON p.id = pur.project_id
WHERE pur.id IS NULL;

-- Step 5: Verify the changes
SELECT
    p.project_name,
    u.email,
    pur.role
FROM projects p
JOIN project_user_roles pur ON p.id = pur.project_id
JOIN users u ON pur.user_id = u.id
ORDER BY p.project_name, pur.role;

