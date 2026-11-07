CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE teams (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE memberships (
    user_id   BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    team_id   BIGINT      NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    role      VARCHAR(20) NOT NULL CHECK (role IN ('OWNER', 'MEMBER')),
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, team_id)
);

CREATE TABLE events (
    id            BIGSERIAL PRIMARY KEY,
    team_id       BIGINT       NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    title         VARCHAR(150) NOT NULL,
    type          VARCHAR(20)  NOT NULL CHECK (type IN ('TRAINING', 'GAME', 'OTHER')),
    location      VARCHAR(255),
    meet_at       TIMESTAMPTZ,
    starts_at     TIMESTAMPTZ  NOT NULL,
    ends_at       TIMESTAMPTZ,
    respond_until TIMESTAMPTZ,
    created_by    BIGINT       NOT NULL REFERENCES users(id)
);

CREATE TABLE attendances (
    event_id   BIGINT      NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    user_id    BIGINT      NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
    status     VARCHAR(20) NOT NULL CHECK (status IN ('ACCEPTED', 'DECLINED', 'MAYBE')),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (event_id, user_id)
);

CREATE INDEX idx_events_team_start ON events(team_id, starts_at);
CREATE INDEX idx_memberships_team ON memberships(team_id);
