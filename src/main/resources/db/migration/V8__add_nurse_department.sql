-- Create Nurse Department if it doesn't exist
INSERT INTO departments (name, description, created_at, updated_at)
SELECT 'Nurse Department', 'Default department for nurses', NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM departments WHERE name = 'Nurse Department'
);

