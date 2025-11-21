-- Increase avatar_url column size to TEXT to support base64 encoded images
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' 
        AND column_name = 'avatar_url' 
        AND data_type = 'character varying'
    ) THEN
        ALTER TABLE users ALTER COLUMN avatar_url TYPE TEXT;
    END IF;
END $$;
