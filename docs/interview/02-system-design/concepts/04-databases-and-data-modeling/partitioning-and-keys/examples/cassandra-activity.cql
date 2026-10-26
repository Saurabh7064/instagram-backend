-- Cassandra 5.x learning example; not an application migration.
-- Assumption: disposable single-node lab, actual datacenter name datacenter1.
-- Run only after reading lessons 7-10. CQL runtime execution is not claimed.
-- cqlsh -f cassandra-activity.cql
CREATE KEYSPACE activity_lessons
WITH replication = {'class': 'NetworkTopologyStrategy', 'datacenter1': 1};
CREATE TABLE activity_lessons.activity_by_user_day (
    user_id bigint,
    activity_date date,
    occurred_at timestamp,
    activity_id bigint,
    activity_type text,
    post_id bigint,
    PRIMARY KEY ((user_id, activity_date), occurred_at, activity_id)
) WITH CLUSTERING ORDER BY (occurred_at DESC, activity_id ASC);
INSERT INTO activity_lessons.activity_by_user_day
(user_id,activity_date,occurred_at,activity_id,activity_type)
VALUES (42,'2026-10-25','2026-10-25 09:00:00+0000',1001,'LOGIN');
INSERT INTO activity_lessons.activity_by_user_day
(user_id,activity_date,occurred_at,activity_id,activity_type,post_id)
VALUES (42,'2026-10-25','2026-10-25 09:05:00+0000',1002,'VIEW_POST',900);
INSERT INTO activity_lessons.activity_by_user_day
(user_id,activity_date,occurred_at,activity_id,activity_type,post_id)
VALUES (42,'2026-10-25','2026-10-25 09:05:00+0000',1003,'VIEW_POST',901);
INSERT INTO activity_lessons.activity_by_user_day
(user_id,activity_date,occurred_at,activity_id,activity_type,post_id)
VALUES (84,'2026-10-25','2026-10-25 09:08:00+0000',2001,'LIKE_POST',900);
INSERT INTO activity_lessons.activity_by_user_day
(user_id,activity_date,occurred_at,activity_id,activity_type,post_id)
VALUES (42,'2026-10-26','2026-10-26 00:00:00+0000',1004,'VIEW_POST',902);
-- Expected IDs: 1002,1003,1001; newest time first, then ID ascending.
SELECT activity_id,occurred_at,activity_type,post_id
FROM activity_lessons.activity_by_user_day
WHERE user_id=42 AND activity_date='2026-10-25';
-- Expected IDs: 1002,1003. This remains the same partition/replica set.
SELECT activity_id,occurred_at,activity_type
FROM activity_lessons.activity_by_user_day
WHERE user_id=42 AND activity_date='2026-10-25' LIMIT 2;
-- Actual token values are produced by the configured partitioner, not toy 35/65/80.
SELECT token(user_id,activity_date),user_id,activity_date,activity_id
FROM activity_lessons.activity_by_user_day
WHERE user_id=42 AND activity_date='2026-10-25';
-- Separate summary layout: demonstrates why type is not an event identifier.
CREATE TABLE activity_lessons.latest_activity_by_type (
    user_id bigint,
    activity_date date,
    activity_type text,
    occurred_at timestamp,
    activity_id bigint,
    post_id bigint,
    PRIMARY KEY ((user_id,activity_date),activity_type)
);
-- Explicit mutation timestamps make the replacement deterministic for the lab.
-- They are microseconds for write resolution, independent of occurred_at values.
INSERT INTO activity_lessons.latest_activity_by_type
(user_id,activity_date,activity_type,occurred_at,activity_id,post_id)
VALUES (42,'2026-10-25','VIEW_POST','2026-10-25 09:05:00+0000',1002,900)
USING TIMESTAMP 1000000;
INSERT INTO activity_lessons.latest_activity_by_type
(user_id,activity_date,activity_type,occurred_at,activity_id,post_id)
VALUES (42,'2026-10-25','VIEW_POST','2026-10-25 09:05:00+0000',1003,901)
USING TIMESTAMP 2000000;
-- Expected one row: VIEW_POST with activity_id=1003, post_id=901.
SELECT activity_type,activity_id,post_id
FROM activity_lessons.latest_activity_by_type
WHERE user_id=42 AND activity_date='2026-10-25';
-- No automatic DROP: the lab data is available for inspection.
-- This namespace must be disposable and unused; rerunning CREATE intentionally fails.
