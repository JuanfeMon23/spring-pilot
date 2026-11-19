-- Notes module: table, constraints, indexes and updated_at trigger.

CREATE TABLE notes (
    id         uuid         NOT NULL DEFAULT uuidv7(),
    title      varchar(120) NOT NULL,
    body       varchar(2000),
    is_active  boolean      NOT NULL DEFAULT true,
    created_at timestamptz  NOT NULL DEFAULT now(),
    updated_at timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT pk_notes PRIMARY KEY (id),
    CONSTRAINT ck_notes_title_length CHECK (char_length(btrim(title)) >= 1)
);

-- Title unique among active notes, case-insensitive (backs the 409 note_title_taken).
CREATE UNIQUE INDEX uq_notes_title ON notes (lower(title)) WHERE is_active;

-- Listing order: newest first.
CREATE INDEX ix_notes_created_at ON notes (created_at DESC);

-- Shared trigger function: reusable by any table with an updated_at column.
-- clock_timestamp() (not now()) so successive updates in one transaction still advance it.
CREATE FUNCTION set_updated_at() RETURNS trigger
    LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = clock_timestamp();
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_notes_set_updated_at
    BEFORE UPDATE ON notes
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
