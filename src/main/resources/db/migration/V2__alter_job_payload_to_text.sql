ALTER TABLE job ALTER COLUMN payload TYPE TEXT USING (
    CASE WHEN payload IS NULL THEN NULL
         ELSE payload::text
    END
);
