<div align="center">

# 📈 TradePulse
**A High-Performance, Real-Time Stock Trading & Portfolio Engine**

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white)](https://angular.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://postgresql.org)
[![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?&style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![TypeScript](https://img.shields.io/badge/TypeScript-007ACC?style=for-the-badge&logo=typescript&logoColor=white)](https://typescriptlang.org)

</div>

<p align="center">
  <b>TradePulse</b> is a robust, production-ready simulated stock trading engine featuring a high-concurrency order matching system, real-time data streaming via WebSockets, and a modern "Pro-Trading Terminal" UI.
</p>

---

## ✨ Key Features

- **⚡ Blazing Fast Matching Engine**: Implements in-memory data structures for Order Books (Bids & Asks) and utilizes multithreading to manage high-volume, concurrent user orders.
- **🔒 Transactional Integrity (ACID)**: Ensures absolute consistency during trade executions, preventing race conditions like double-spending or overselling.
- **📡 Real-Time Market Data**: Leverages **Spring WebSockets (STOMP/SockJS)** to broadcast live order book depth, recent trades, and ticker updates instantly to the frontend.
- **🛡️ Secure Authentication**: JWT-based stateless authentication ensures secure access to portfolios and order execution endpoints.
- **💻 Pro-Trading Terminal**: A slick, reactive Angular frontend utilizing **RxJS** to stream real-time data into charts, order books, and the live tape.
- **🚀 Advanced Caching**: Redis integration for lightning-fast caching and pub/sub message brokering.

## 🏗️ Architecture Stack

### Backend (`/trading-engine-backend`)
- **Java 17+** with **Spring Boot**
- **Spring Data JPA** & **Hibernate**
- **Spring Security** (JWT Authentication)
- **Spring WebSockets** (STOMP messages)
- **PostgreSQL** (Primary persistent data store)
- **Redis** (Caching layer and fast pub/sub operations)

### Frontend (`/trading-engine-frontend`)
- **Angular** (Component-based UI)
- **TypeScript** & **RxJS** (Reactive data streams)
- **Tailwind CSS** or Custom CSS (Sleek UI/UX)
- **SockJS & STOMP.js** (WebSocket clients)

---

## 🚀 Getting Started

Follow these steps to get a copy of the project up and running on your local machine.

### Prerequisites
- [Java Development Kit (JDK) 17+](https://adoptium.net/)
- [Node.js & npm](https://nodejs.org/en/)
- [PostgreSQL](https://www.postgresql.org/download/)
- [Redis](https://redis.io/download/)
- [Angular CLI](https://angular.io/cli) (`npm i -g @angular/cli`)

### Setup Instructions

#### 1. Database & Cache
Ensure that your local PostgreSQL server and Redis instances are running.
Create a PostgreSQL database for the application:
```sql
CREATE DATABASE tradepulse;
```

#### 2. Backend Setup
Navigate to the backend directory and run the Spring Boot app:
```bash
cd trading-engine-backend
# Update application.properties or application.yml with your DB/Redis credentials
./mvnw spring-boot:run
```

#### 3. Frontend Setup
Navigate to the frontend directory, install dependencies, and start the development server:
```bash
cd trading-engine-frontend
npm install
ng serve
```

#### 4. Access the Application
Open your browser and navigate to: [http://localhost:4200](http://localhost:4200)

---

## 🛠️ Development & Roadmap

TradePulse is actively being developed. Our current focus is testing and high-load simulations.

- ✅ **Phase 1-2**: Setup, DB Schema, Auth, setup Redis.
- ✅ **Phase 3**: Concurrent Order Matching System & ACID executions.
- ✅ **Phase 4**: Real-time WebSockets & Data broadcasting.
- ✅ **Phase 5**: Pro-Trading Terminal UI (Order form, live order book).
- ⬜ **Phase 6**: High-concurrency simulations, End-to-end tests, unit testing the matching engine.

## 🤝 Contributing

Contributions are welcome! If you're interested in algorithmic trading, high-frequency systems, or reactive frontends, feel free to open a PR or Issue.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

<div align="center">
  Made with ❤️ by the TradePulse Team.
</div>
