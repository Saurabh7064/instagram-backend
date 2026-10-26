-- Isolated learning lab. Run with psql -v ON_ERROR_STOP=1 -f this_file.
-- All schema/data changes are rolled back; no application tables are used.
BEGIN;
CREATE SCHEMA partitioning_lesson;
SET LOCAL search_path = partitioning_lesson, pg_catalog;
SET LOCAL TIME ZONE 'UTC';
CREATE TABLE user_activity (
    user_id bigint NOT NULL,
    activity_date date NOT NULL,
    occurred_at timestamptz NOT NULL,
    activity_id bigint NOT NULL,
    activity_type text NOT NULL,
    post_id bigint,
    PRIMARY KEY (activity_date, user_id, activity_id),
    CHECK (activity_date = (occurred_at AT TIME ZONE 'UTC')::date)
) PARTITION BY RANGE (activity_date);
CREATE TABLE user_activity_2026_10_25
    PARTITION OF user_activity
    FOR VALUES FROM ('2026-10-25') TO ('2026-10-26');
INSERT INTO user_activity VALUES
(42,'2026-10-25','2026-10-25 09:00:00+00',1001,'LOGIN',NULL),
(42,'2026-10-25','2026-10-25 09:05:00+00',1002,'VIEW_POST',900),
(42,'2026-10-25','2026-10-25 09:05:00+00',1003,'VIEW_POST',901),
(84,'2026-10-25','2026-10-25 09:08:00+00',2001,'LIKE_POST',900);
-- Controlled failure: Oct26 has no child yet. Catch only the expected error.
DO $$
BEGIN
    BEGIN
        INSERT INTO user_activity VALUES
        (42,'2026-10-26','2026-10-26 00:00:00+00',1004,'VIEW_POST',902);
        RAISE EXCEPTION 'Expected a missing-partition failure';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'EXPECTED: no matching October 26 partition';
    END;
END $$;
CREATE TABLE user_activity_2026_10_26
    PARTITION OF user_activity
    FOR VALUES FROM ('2026-10-26') TO ('2026-10-27');
INSERT INTO user_activity VALUES
(42,'2026-10-26','2026-10-26 00:00:00+00',1004,'VIEW_POST',902);
CREATE INDEX user_activity_user_time_idx
    ON user_activity (user_id, occurred_at DESC, activity_id ASC);
SELECT tableoid::regclass AS physical_child, count(*) AS rows
FROM user_activity GROUP BY tableoid ORDER BY tableoid::regclass::text;
SELECT activity_id, activity_type, occurred_at
FROM user_activity
WHERE activity_date=DATE '2026-10-25' AND user_id=42
ORDER BY occurred_at DESC, activity_id ASC;
EXPLAIN (COSTS OFF)
SELECT * FROM user_activity
WHERE activity_date=DATE '2026-10-25' AND user_id=42;
DO $$
DECLARE ordered_ids bigint[];
BEGIN
    IF (SELECT count(*) FROM user_activity) <> 5 THEN
        RAISE EXCEPTION 'Expected 5 total activities';
    END IF;
    IF (SELECT count(*) FROM user_activity_2026_10_25) <> 4 THEN
        RAISE EXCEPTION 'Expected 4 rows in October 25 child';
    END IF;
    IF (SELECT count(*) FROM user_activity_2026_10_26) <> 1 THEN
        RAISE EXCEPTION 'Expected 1 row in October 26 child';
    END IF;
    SELECT array_agg(activity_id ORDER BY occurred_at DESC, activity_id ASC)
      INTO ordered_ids FROM user_activity
      WHERE user_id=42 AND activity_date=DATE '2026-10-25';
    IF ordered_ids IS DISTINCT FROM ARRAY[1002,1003,1001]::bigint[] THEN
        RAISE EXCEPTION 'Unexpected ordering: %', ordered_ids;
    END IF;
    IF (SELECT tableoid FROM user_activity WHERE activity_id=1004)
        <> 'user_activity_2026_10_26'::regclass THEN
        RAISE EXCEPTION 'Boundary row routed to wrong child';
    END IF;
    RAISE NOTICE 'PASS: row counts, exact boundary placement, and tie ordering';
END $$;
ROLLBACK;
DO $$
BEGIN
    IF EXISTS (SELECT FROM pg_namespace WHERE nspname='partitioning_lesson') THEN
        RAISE EXCEPTION 'Learning schema survived rollback';
    END IF;
    RAISE NOTICE 'PASS: schema and data removed by rollback';
END $$;
