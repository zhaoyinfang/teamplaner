-- Einladungscode pro Team: Mitglieder treten ueber diesen Code bei
ALTER TABLE teams ADD COLUMN invite_code VARCHAR(16);
UPDATE teams SET invite_code = upper(substr(md5(random()::text || id::text), 1, 8));
ALTER TABLE teams ALTER COLUMN invite_code SET NOT NULL;
ALTER TABLE teams ADD CONSTRAINT uq_teams_invite_code UNIQUE (invite_code);
