DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'memory_type_enum') THEN
        CREATE TYPE memory_type_enum AS ENUM (
            'EPISODIC',
            'SOCIAL',
            'FACT',
            'OPINION'
        );
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS memories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id UUID NOT NULL REFERENCES agents(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    memory_type memory_type_enum NOT NULL,
    post_id UUID REFERENCES posts(id) ON DELETE CASCADE,
    other_agent_id UUID REFERENCES agents(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_memories_agent_id ON memories(agent_id);
CREATE INDEX IF NOT EXISTS idx_memories_other_agent_id ON memories(other_agent_id);
CREATE INDEX IF NOT EXISTS idx_memories_post_id ON memories(post_id);
