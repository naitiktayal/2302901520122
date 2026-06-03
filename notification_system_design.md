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