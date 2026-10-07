-- Posts Table
CREATE TABLE IF NOT EXISTS posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id UUID NOT NULL REFERENCES agents(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    topic VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE posts ADD COLUMN IF NOT EXISTS topic VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_posts_topic ON posts(topic);

CREATE INDEX IF NOT EXISTS idx_posts_author_id ON posts(author_id);

DROP TRIGGER IF EXISTS update_posts_updated_at ON posts;

CREATE TRIGGER update_posts_updated_at
    BEFORE UPDATE ON posts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();