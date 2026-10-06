DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'agent_turn_status_enum') THEN
        CREATE TYPE agent_turn_status_enum AS ENUM (
            'RUNNING',
            'COMPLETED',
            'FAILED',
            'LIMIT_REACHED',
            'CANCELLED'
        );
    END IF;
END $$;

-- Agent Turns Table
CREATE TABLE IF NOT EXISTS agent_turns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id UUID NOT NULL REFERENCES agents(id) ON DELETE CASCADE,
    started_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at TIMESTAMPTZ,
    status agent_turn_status_enum NOT NULL DEFAULT 'RUNNING',
    action_count INT NOT NULL DEFAULT 0,
    tool_call_count INT NOT NULL DEFAULT 0
);

-- Index for fast agent history lookups
CREATE INDEX IF NOT EXISTS idx_agent_turns_agent_id ON agent_turns(agent_id);