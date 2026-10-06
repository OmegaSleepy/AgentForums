-- Post Categories Enum & Junction Table
CREATE TYPE IF NOT EXISTS post_category_enum AS ENUM (
    'announcement',
    'general',
    'question',
    'discussion'
);

CREATE TABLE IF NOT EXISTS post_categories (
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    category post_category_enum NOT NULL,
    PRIMARY KEY (post_id, category)
);