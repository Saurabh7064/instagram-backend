# 4. Databases & Data Modeling

[All concept categories](../index.md)

## Partitioning and key design module

The [11-part module](./partitioning-and-keys/README.md) expands sharding (84), partitioning (85), and replica context (86–90). It teaches range/hash partitioning, hot keys, consistent hashing, PostgreSQL partition keys, Cassandra primary/clustering keys, partition creation, request routing, and Instagram access-pattern choices. Artifacts are READY; comprehension remains TODO. The PostgreSQL exercise was run in isolation; Cassandra execution is not claimed.

## Existing overview

- [Partitioning, replication, and hot keys](./03-partitioning-replication-hot-keys.md) remains a stable entry URL and now directs detailed study to the module.

## Related interview practice

- [How do you prevent two concurrent requests from creating duplicate likes?](../../questions/short-questions-and-answers/21-concurrent-writes-and-database-constraints.md) — artifact `READY`, comprehension `TODO`.

## Topic roadmap

These topic numbers preserve the original index; listing a topic does not imply it is fully covered by an existing lesson.

71. SQL vs NoSQL
72. ACID properties
73. BASE properties
74. Database normalization
75. Database denormalization
76. Primary keys vs foreign keys
77. Indexes
78. Composite indexes
79. Covering indexes
80. B-tree vs B+ tree
81. Hash indexes
82. Query execution plans
83. N+1 query problem
84. Database sharding
85. Database partitioning
86. Database replication
87. Leader-follower replication
88. Multi-leader replication
89. Read replicas
90. Replication lag
91. Optimistic locking
92. Pessimistic locking
93. MVCC (Multi-Version Concurrency Control)
94. Transaction isolation levels
95. Deadlocks
96. Database connection pools
97. Materialized views
98. Change Data Capture (CDC)
99. CQRS
100. Event sourcing

Use the [concept template](../../_concept-template.md) for new lessons and record understanding in [learner progress](../../learner-progress.md).
