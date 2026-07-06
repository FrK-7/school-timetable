-- Migration: rename YEAR column to CLASS_YEAR if it exists (H2 syntax)
-- These statements will fail silently if columns are already migrated (continue-on-error=true)

ALTER TABLE SCHOOL_CLASS RENAME COLUMN "YEAR" TO CLASS_YEAR;
ALTER TABLE SUBJECT_HOURS_PER_YEAR RENAME COLUMN "YEAR" TO CLASS_YEAR;
