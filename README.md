## Core Features

- **User Management**: 
  - Register new users with a username, password, and initial balance.
  - Retrieve user details by ID or username.
  - Check user balance with caching for improved performance.

- **Transaction Processing**:
  - Initiate money transfers between users.
  - Process transactions asynchronously using Kafka.
  - Update transaction status with real-time feedback.

- **Security**:
  - JWT-based authentication for secure access.
  - Password encryption using Spring Security.

- **Caching**:
  - Redis integration for caching transaction statuses and user balances.
  - Configurable cache expiration for optimal performance.

- **Event-Driven Architecture**:
  - Kafka integration for handling transaction events.
  - Asynchronous processing to ensure scalability and responsiveness.
