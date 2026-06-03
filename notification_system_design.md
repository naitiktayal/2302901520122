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


# Stage 4

## Problem Statement

Currently, notifications are fetched from the database on every page load for every student.

With:

* 50,000 students
* 5,000,000+ notifications

this approach generates a large number of repeated database queries, causing high database load, increased response time, and poor user experience.

---

## Proposed Solutions

### 1. Caching Layer (Recommended)

Introduce a cache such as Redis between the application and the database.

### Working

1. User requests notifications.
2. Application checks Redis cache.
3. If data exists, return cached data.
4. If data does not exist, fetch from PostgreSQL and store in Redis.

### Benefits

* Very fast response time.
* Reduces database load significantly.
* Handles large traffic efficiently.

### Tradeoffs

* Additional infrastructure required.
* Cache invalidation must be handled carefully.
* Slight increase in system complexity.

---

## 2. Real-Time Notifications Using WebSocket

Instead of fetching notifications on every page refresh, establish a persistent WebSocket connection.

### Working

1. User logs in.
2. WebSocket connection is established.
3. Server pushes new notifications instantly.
4. Frontend updates automatically.

### Benefits

* Real-time updates.
* Better user experience.
* Reduces repeated API calls.

### Tradeoffs

* More complex implementation.
* Requires connection management.
* Higher memory usage for active connections.

---

## 3. Pagination

Do not load all notifications at once.

### Example

```http
GET /api/notifications?page=0&size=20
```

Only the latest 20 notifications are returned.

### Benefits

* Smaller query result sets.
* Faster API response.
* Lower memory usage.

### Tradeoffs

* Additional API logic.
* Users may need multiple requests for older notifications.

---

## 4. Database Index Optimization

Create indexes on frequently searched columns.

```sql
CREATE INDEX idx_notifications_student_read_created
ON notifications(studentID, isRead, createdAt DESC);
```

### Benefits

* Faster searching.
* Faster sorting.

### Tradeoffs

* Additional storage usage.
* Slower write operations.

---

## 5. Notification Count API

Instead of loading all notifications on every page load, first load only the unread count.

### Example

```http
GET /api/notifications/unread-count
```

Response:

```json
{
  "unreadCount": 5
}
```

Load full notifications only when the user opens the notification panel.

### Benefits

* Reduces unnecessary database queries.
* Faster page loading.

### Tradeoffs

* Additional API endpoint required.

---

## 6. Background Processing with Message Queue

Use Kafka or RabbitMQ for notification delivery.

### Working

1. Event occurs.
2. Notification is pushed to queue.
3. Worker processes notification.
4. Notification is stored and delivered.

### Benefits

* Highly scalable.
* Handles traffic spikes.

### Tradeoffs

* More infrastructure.
* Increased operational complexity.

---

## Recommended Architecture

For best performance:

1. PostgreSQL as primary storage.
2. Redis for caching.
3. WebSocket for real-time delivery.
4. Pagination for notification history.
5. Proper indexing.
6. Kafka/RabbitMQ for large-scale processing.

---

## Conclusion

The current approach of fetching notifications from the database on every page load does not scale well. A combination of Redis caching, WebSocket-based real-time delivery, pagination, indexing, and asynchronous processing provides a scalable and high-performance notification system while maintaining a good user experience.


# Stage 5

## Problems in Current Implementation

Given Pseudocode:

```text id="p1"
function notify_all(student_ids, message):
    for student_id in student_ids:
        send_email(student_id, message)
        save_to_db(student_id, message)
        push_to_app(student_id, message)
```

### Shortcomings

1. Sequential Processing

The system processes one student at a time.

For 50,000 students, this will be extremely slow.

---

2. No Fault Tolerance

If `send_email()` fails for a student, the process may continue without recording the failure properly.

---

3. No Retry Mechanism

Failed emails are simply lost.

The 200 students whose email failed will never receive the notification.

---

4. Tight Coupling

Email sending, database storage, and app notification are executed together.

Failure in one step can affect the entire flow.

---

5. Poor Scalability

The application server handles all work directly.

Large traffic spikes can overwhelm the system.

---

## What Happens When Email Fails For 200 Students?

With the current design:

* Notification may already be saved for some students.
* Some students may receive in-app notifications.
* 200 students will miss email notifications.
* System has no way to automatically retry.

This creates inconsistent data and poor reliability.

---

## Recommended Design

Use:

* PostgreSQL
* Message Queue (Kafka/RabbitMQ)
* Worker Services
* Retry Mechanism
* WebSocket

### Architecture Flow

1. HR clicks Notify All.
2. Notification is stored in database.
3. Notification jobs are published to queue.
4. Multiple workers process jobs in parallel.
5. Email service sends emails.
6. WebSocket pushes in-app notifications.
7. Failed jobs are retried automatically.

---

## Revised Pseudocode

```text id="p2"
function notify_all(student_ids, message):

    notification_id = create_notification_batch(message)

    for student_id in student_ids:

        save_to_db(
            student_id,
            notification_id,
            message,
            status = "PENDING"
        )

        publish_to_queue(
            student_id,
            notification_id,
            message
        )
```

### Worker Service

```text id="p3"
function process_notification(job):

    try:

        send_email(job.student_id, job.message)

        push_to_app(job.student_id, job.message)

        update_status(
            job.notification_id,
            job.student_id,
            "SUCCESS"
        )

    except Exception:

        retry(job)

        if retry_limit_exceeded:
            update_status(
                job.notification_id,
                job.student_id,
                "FAILED"
            )
```

---

## Should Saving to DB and Sending Email Happen Together?

No.

They should be separated.

### Reason

Saving notifications to the database is the source of truth.

Once stored successfully:

* Notification history is preserved.
* User can view it later.
* Retry mechanisms can work.
* Failures can be tracked.

Email delivery is an external operation and may fail due to:

* SMTP issues
* Network problems
* Rate limits
* Third-party outages

Therefore email sending should happen asynchronously after successful database storage.

---

## Benefits of the New Design

### Reliability

Failed emails can be retried automatically.

### Scalability

Multiple workers can process thousands of notifications simultaneously.

### Faster Response

HR receives immediate confirmation without waiting for 50,000 emails.

### Fault Isolation

Email failures do not affect database storage.

### Monitoring

Notification status can be tracked as:

```text id="p4"
PENDING
SUCCESS
FAILED
```

---

## Conclusion

The original implementation is slow, tightly coupled, and unreliable. A queue-based asynchronous architecture using PostgreSQL, Kafka/RabbitMQ, worker services, retries, and WebSocket notifications provides high performance, fault tolerance, scalability, and reliable delivery for 50,000 students during placement season.

# Stage 6

## Priority Inbox

Priority Order:

1. Placement
2. Result
3. Event

If two notifications have the same type, the more recent notification gets higher priority.

### Approach

- Assign weight to each notification type.
- Sort notifications by:
    1. Weight (descending)
    2. Timestamp (descending)
- Display top 10 notifications.

### Complexity

Sorting: O(N log N)

### Efficient Maintenance

For continuous incoming notifications, a Min Heap (Priority Queue) of size 10 can be maintained.

Complexity:
- Insert: O(log 10)
- Retrieval: O(1)

This is more efficient than sorting the entire dataset repeatedly.

### Files Submitted

- PriorityInbox.java
- output_screenshot.png
- notification_system_design.md