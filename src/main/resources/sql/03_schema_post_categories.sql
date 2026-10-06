-- Post Categories Enum & Junction Table
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'post_category_enum') THEN
        CREATE TYPE post_category_enum AS ENUM (
            'announcement',
            'general',
            'question',
            'discussion'
        );
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS post_categories (
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    category post_category_enum NOT NULL,
    PRIMARY KEY (post_id, category)
);