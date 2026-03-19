# Real-Time Trading Engine - Implementation Plan

## Goal
Build a high-performance, real-time stock trading and portfolio engine. It must handle live data streams, enforce strict transactional integrity (ACID properties) to prevent race conditions (e.g., multiple users buying the last share), and leverage multithreading and reactive programming patterns. 

## Technology Stack
- **Backend:** Java Spring Boot, Spring WebSockets, Spring Data JPA
- **Database:** PostgreSQL (persistent storage), Redis (in-memory caching & Pub-Sub)
- **Frontend:** Angular, RxJS (reactive data streams), Vanilla CSS / Custom UI Framework
- **Concurrency Strategy:** Java `ConcurrentHashMap`, ReentrantLocks, Transactional Service boundaries

---

## User Review Required
> [!IMPORTANT]
> The Matching Engine requires strict concurrency controls. Please review the "Order Matching Engine Design" section to ensure the strategy of using Read/Write Locks and DB-level row locks fits the expected scale of the application before we proceed to execution.

---

## Proposed System Architecture

### 1. Database Layer (PostgreSQL)
We will create structured tables to maintain absolute truth for balances and assets:
- `users`: ID, Username, Email, PasswordHash, AvailableCash
- `stocks`: Ticker, Name, CurrentPrice, TotalShares
- `portfolios`: UserID, Ticker, Quantity, AveragePrice
- `orders`: OrderID, UserID, Ticker, Type (BUY/SELL), Price, Quantity, Status (OPEN, FILLED, CANCELLED), Timestamp
- `trades`: TradeID, BuyOrderID, SellOrderID, Ticker, Price, Quantity, Timestamp

### 2. Caching & Pub/Sub (Redis)
- **Fast Order Books:** Keep active order bids/asks in standard Redis Sorted Sets for rapid insertion and retrieval.
- **Message Broker:** Use Redis Pub/Sub to pass trade execution events from the Match Engine to the WebSocket handler horizontally to broadcast to clients.

### 3. Backend Implementation (Spring Boot)

#### REST Controllers
- `/api/auth`: Login and Registration
- `/api/portfolio`: Fetch user balances and holdings
- `/api/orders`: Submit new limit/market orders

#### The Order Matching Engine (Core)
The matching engine will run asynchronously from the REST execution thread. 
- Order requests are placed on a concurrent queue.
- Dedicated consumer threads will pick up limit orders and attempt a match.
- **Handling Race Conditions:** 
  - We will use `@Transactional` methods combined with database row locking (e.g., `SELECT ... FOR UPDATE` on user balances and portfolio holdings).
  - Price-Time priority matching algorithms will ensure fair execution.

#### Real-Time WebSockets
- Use `@EnableWebSocketMessageBroker`.
- **Broadcast Endpoints:**
  - `/topic/ticker`: Streams live stock prices to all users.
  - `/topic/orderbook/{ticker}`: Streams the topmost 10 entries of the Bids and Asks.
- **Private Endpoints:**
  - `/queue/trades/{userId}`: Pushes private notifications when a specific user's order is filled.

---

## Frontend Integration (Angular)

### Core Services
- **`AuthService`**: Handles JWT tokens, user guards, and session state.
- **`TradeService`**: Performs standard HTTP REST calls for fetching historical data or submitting an order.
- **`MarketStreamService` (RxJS)**: 
  - Connects to Spring WebSockets via `RxStomp`.
  - Exposes `Observables` for live ticker tapes (`ticker$`), order books (`book$`), and private alerts (`notifications$`). Components will simply `async` pipe these variables into the HTML.

### UI Components
- **`HomeComponent`**: The landing marketing page (Already implemented).
- **`TradeComponent`**: The main Pro-Terminal. It will feature:
  - Dynamic Grid layout using Glassmorphism themes.
  - Scrolling ticker tape at the top.
  - Live Order Book component reacting to stream updates.
  - Interactive "Place Order" ticket form that validates user inputs.

---

## Verification Plan

### Automated Tests
- **Unit Tests (`JUnit` & `Mockito`)**: Ensure the Matching logic always respects Price-Time conditions. We will simulate multi-threaded submissions of BUY and SELL orders simultaneously to assert that exactly the correct number of trades are matched and no "phantom" shares are generated.
- **Repository Tests (`@DataJpaTest`)**: Verify pessimistic locking across transactions works efficiently without deadlocks.

### Manual Verification
1. Start up PostgreSQL, Redis, Frontend, and Backend.
2. Register two different users in incognito windows.
3. Open the main Trading view.
4. Have User A place a Limit Sell for 100 shares. Verify it appears on User B's live Order Book instantly via WebSockets without refreshing.
5. Have User B place a Market Buy. Verify both users' portfolios correctly transition assets and the socket sends a "Trade Executed" notification.
