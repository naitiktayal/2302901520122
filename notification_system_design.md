# Stage 1

## Notification System Design

### Core Actions Supported

1. Create Notification
2. Get User Notifications
3. Get Notification By ID
4. Mark Notification As Read
5. Mark All Notifications As Read
6. Delete Notification
7. Real-Time Notification Delivery

---

## 1. Create Notification

### Endpoint

POST /api/v1/notifications

### Headers

```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer <token>"
}
```

### Request

```json
{
  "userId": 101,
  "title": "New Message",
  "message": "You have received a new message.",
  "type": "INFO"
}
```

### Response

```json
{
  "notificationId": 1001,
  "status": "CREATED",
  "timestamp": "2026-06-03T10:00:00Z"
}
```

---

## 2. Get User Notifications

### Endpoint

GET /api/v1/users/{userId}/notifications

### Headers

```json
{
  "Authorization": "Bearer <token>"
}
```

### Response

```json
[
  {
    "notificationId": 1001,
    "title": "New Message",
    "message": "You have received a new message.",
    "isRead": false,
    "createdAt": "2026-06-03T10:00:00Z"
  }
]
```

---

## 3. Get Notification By ID

### Endpoint

GET /api/v1/notifications/{notificationId}

### Response

```json
{
  "notificationId": 1001,
  "title": "New Message",
  "message": "You have received a new message.",
  "isRead": false
}
```

---

## 4. Mark Notification As Read

### Endpoint

PUT /api/v1/notifications/{notificationId}/read

### Response

```json
{
  "notificationId": 1001,
  "status": "READ"
}
```

---

## 5. Mark All Notifications As Read

### Endpoint

PUT /api/v1/users/{userId}/notifications/read-all

### Response

```json
{
  "status": "SUCCESS",
  "message": "All notifications marked as read"
}
```

---

## 6. Delete Notification

### Endpoint

DELETE /api/v1/notifications/{notificationId}

### Response

```json
{
  "status": "SUCCESS",
  "message": "Notification deleted"
}
```

---

## Real-Time Notification Mechanism

Technology Used: WebSocket

### WebSocket Endpoint

/ws/notifications

### Event Payload

```json
{
  "notificationId": 1001,
  "title": "New Message",
  "message": "You have received a new message.",
  "type": "INFO"
}
```

### Flow

1. User logs in.
2. Frontend connects to WebSocket.
3. Server pushes notifications instantly.
4. Notification appears without page refresh.

### Benefits

- Real-time updates
- Low latency
- Better user experience
- Scalable architecture

# Stage 2

## Database Choice

For the Notification System, I suggest using a **Relational Database (PostgreSQL)**.

### Reasons for Choosing PostgreSQL

1. Notifications have a structured format.
2. Data consistency is important.
3. Supports indexing for fast retrieval.
4. Handles large amounts of data efficiently.
5. Supports transactions and ACID properties.
6. Easy integration with REST APIs and backend frameworks.

---

## Database Schema

### Table: users

| Column Name | Data Type    | Description |
| ----------- | ------------ | ----------- |
| user_id     | BIGINT       | Primary Key |
| username    | VARCHAR(100) | Username    |
| email       | VARCHAR(255) | User Email  |

### Table: notifications

| Column Name     | Data Type    | Description          |
| --------------- | ------------ | -------------------- |
| notification_id | BIGINT       | Primary Key          |
| user_id         | BIGINT       | Foreign Key          |
| title           | VARCHAR(255) | Notification Title   |
| message         | TEXT         | Notification Content |
| type            | VARCHAR(50)  | INFO, WARNING, ALERT |
| is_read         | BOOLEAN      | Read Status          |
| created_at      | TIMESTAMP    | Creation Time        |

---

## Problems as Data Volume Increases

### 1. Slow Query Performance

When millions of notifications are stored, fetching data may become slower.

**Solution:**

* Create indexes on user_id and created_at.
* Use query optimization.

### 2. Large Storage Consumption

Notification records continuously increase.

**Solution:**

* Archive old notifications.
* Delete expired notifications.

### 3. High Read Traffic

Many users may request notifications simultaneously.

**Solution:**

* Use caching.
* Load balancing.

### 4. Scalability Issues

Single database server may become overloaded.

**Solution:**

* Database replication.
* Horizontal scaling.
* Database partitioning (sharding).

---

## SQL Queries

### Create Notification

```sql
INSERT INTO notifications
(user_id, title, message, type, is_read, created_at)
VALUES
(101, 'New Message', 'You received a new message', 'INFO', FALSE, NOW());
```

### Get All Notifications For User

```sql
SELECT *
FROM notifications
WHERE user_id = 101
ORDER BY created_at DESC;
```

### Get Notification By ID

```sql
SELECT *
FROM notifications
WHERE notification_id = 1001;
```

### Mark Notification As Read

```sql
UPDATE notifications
SET is_read = TRUE
WHERE notification_id = 1001;
```

### Mark All Notifications As Read

```sql
UPDATE notifications
SET is_read = TRUE
WHERE user_id = 101;
```

### Delete Notification

```sql
DELETE FROM notifications
WHERE notification_id = 1001;
```

---

## Real-Time Notification Storage Support

For real-time notifications:

1. Notification is stored in PostgreSQL.
2. Backend sends event through WebSocket.
3. Connected users receive notification instantly.
4. Notification remains available for future retrieval through REST APIs.

This approach provides both reliability and real-time delivery.

# Stage 3

## Query Analysis

Given Query:

```sql
SELECT * FROM notifications
WHERE studentID = 1042 AND isRead = false
ORDER BY createdAt DESC;
```

### Is the Query Accurate?

Yes, the query is logically correct.

It retrieves all unread notifications of a specific student (studentID = 1042) and sorts them by creation time in descending order so that the latest notifications appear first.

---

## Why is the Query Slow?

Current Database Size:

* Students: 50,000
* Notifications: 5,000,000

Without proper indexing, the database may perform a full table scan.

Steps performed:

1. Scan millions of rows.
2. Filter rows where studentID = 1042.
3. Filter rows where isRead = false.
4. Sort the matching rows by createdAt DESC.

This results in high I/O cost and increased execution time.

---

## Recommended Improvement

Create a composite index:

```sql
CREATE INDEX idx_notifications_student_read_created
ON notifications(studentID, isRead, createdAt DESC);
```

### Benefits

* Directly locates notifications for a specific student.
* Filters unread notifications efficiently.
* Uses the index ordering for sorting.
* Avoids expensive table scans.

---

## Computation Cost

### Without Index

Approximate Cost:

```text
O(N)
```

Where N = 5,000,000 rows.

The database may inspect a large portion of the table.

### With Composite Index

Approximate Cost:

```text
O(log N + K)
```

Where:

* N = total notifications
* K = matching unread notifications

Performance improves significantly because only relevant index entries are scanned.

---

## Should We Add Indexes on Every Column?

No.

Adding indexes on every column is generally not a good practice.

### Problems

1. Increased storage usage.
2. Slower INSERT operations.
3. Slower UPDATE operations.
4. Slower DELETE operations.
5. Unused indexes waste resources.

### Best Practice

Create indexes only on:

* Frequently filtered columns
* Frequently sorted columns
* Join columns
* High-value search columns

Indexes should be based on query patterns rather than adding them everywhere.

---

## Query to Find Students Who Received Placement Notifications in Last 7 Days

### Using JOIN

```sql
SELECT DISTINCT s.studentID,
                s.name
FROM students s
JOIN notifications n
ON s.studentID = n.studentID
WHERE n.notificationType = 'Placement'
AND n.createdAt >= NOW() - INTERVAL '7 days';
```

### Alternative Query

```sql
SELECT DISTINCT studentID
FROM notifications
WHERE notificationType = 'Placement'
AND createdAt >= NOW() - INTERVAL '7 days';
```

---

## Recommended Index for Placement Query

```sql
CREATE INDEX idx_notifications_type_date
ON notifications(notificationType, createdAt);
```

This index improves filtering of placement notifications and date-based searches.

---

## Conclusion

The original query is correct but becomes slow at large scale due to scanning and sorting millions of records. A composite index on (studentID, isRead, createdAt DESC) greatly improves performance. Adding indexes on every column is not recommended because it increases storage and slows write operations. Proper indexing based on query usage is the most effective solution.
