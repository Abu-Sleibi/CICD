# 🏨 Hotel Booking System - SWER313 Course Project

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)
![JWT](https://img.shields.io/badge/JWT-0.11.5-red)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI%203.0-green)

---

## 📋 Project Overview

A comprehensive hotel booking backend system built as a **modular monolith** for the SWER313 course.

The system enables:
- Guests to search hotels, check availability, create bookings, and make payments
- Admins and managers to manage hotels, rooms, pricing, and bookings

---

## 📁 Architecture: Modular Monolith

The system is divided into clean modules inside one project:

| Module | Responsibility |
|--------|----------------|
| **Auth** | Authentication & Authorization (JWT) |
| **Catalog** | Hotels & Room Types |
| **Availability** | Availability & Pricing |
| **Booking** | Booking Management |
| **Payment** | Payment Processing (Mock) |
| **Notification** | Email Notifications |

---

## 🏗️ Project Structure

```bash
src
├── main
│   ├── java/com/example/hotelproject
│   │
│   │   ├── auth
│   │   │   ├── controller
│   │   │   ├── dto
│   │   │   ├── entity
│   │   │   ├── exception
│   │   │   ├── mapper
│   │   │   ├── repository
│   │   │   ├── security
│   │   │   └── service
│   │
│   │   ├── availability
│   │   │   ├── controller
│   │   │   ├── dto
│   │   │   ├── entity
│   │   │   ├── exception
│   │   │   ├── mapper
│   │   │   ├── repository
│   │   │   └── service
│   │
│   │   ├── booking
│   │   │   ├── controller
│   │   │   ├── dto
│   │   │   ├── entity
│   │   │   ├── exception
│   │   │   ├── mapper
│   │   │   ├── repository
│   │   │   ├── security
│   │   │   └── service
│   │
│   │   ├── catalog
│   │   │   ├── hotel
│   │   │   └── room
│   │
│   │   ├── notification
│   │   │   ├── controller
│   │   │   ├── dto
│   │   │   ├── entity
│   │   │   ├── exception
│   │   │   ├── mapper
│   │   │   ├── repository
│   │   │   └── service
│   │
│   │   ├── payment
│   │   │   ├── controller
│   │   │   ├── dto
│   │   │   ├── entity
│   │   │   ├── exception
│   │   │   ├── mapper
│   │   │   ├── repository
│   │   │   └── service
│   │
│   │   ├── config
│   │   ├── enums
│   │   ├── ApiError.java
│   │   ├── GlobalExceptionHandler.java
│   │   ├── HotelProjectApplication.java
│   │   └── PageResponseDto.java
│
│   └── resources
│       └── application.properties
│
└── test
    └── java/com/example/hotelproject
        ├── auth
        ├── availability
        ├── booking
        ├── catalog
        ├── notification
        └── payment
```

---

## ✨ Core Features

### 🔐 Authentication
- JWT login & registration
- Refresh tokens
- Role-based access (ADMIN, MANAGER, GUEST)

### 🏨 Hotel Catalog
- CRUD Hotels
- CRUD Room Types
- Pagination & filtering

### 📅 Availability & Pricing
- Check room availability
- Prevent double booking
- Pricing rules (weekday/weekend)

### 📖 Booking
- Create booking
- Confirm / cancel booking
- Booking search

### 💳 Payment (Mock)
- Payment intent
- Simulated processing (85% success)
- Refunds

### 🔔 Notifications
- Email notifications
- Booking confirmation & cancellation

---

## 🔗 API Endpoints

| Module | Base URL |
|--------|---------|
| Auth | `/api/v1/auth` |
| Hotels | `/api/v1/hotels` |
| Availability | `/api/v1/availability` |
| Booking | `/api/v1/bookings` |
| Payment | `/api/v1/payments` |
| Notification | `/api/v1/notifications` |

---

## 🛠️ Technology Stack

- Java 17
- Spring Boot 3
- Spring Security + JWT
- Spring Data JPA
- MySQL / H2
- Swagger (OpenAPI)
- JUnit + Mockito
- Maven

---

## 🚀 Getting Started

### 1. Clone
```bash
git clone https://github.com/your-repo/hotel-booking-system.git
cd hotel-booking-system
```

### 2. Database
```sql
CREATE DATABASE hotel_booking_db;
```

### 3. Configure
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hotel_booking_db
spring.datasource.username=root
spring.datasource.password=
```

### 4. Run
```bash
mvn clean install
mvn spring-boot:run
```

---
## 📚 API Documentation

- **Base URL:** http://localhost:8080/api/v1
- **Swagger UI:** http://localhost:8080/swagger-ui.html
---

## 🧪 Testing

```bash
mvn test
```

Includes:
- Unit Tests
- Integration Tests
- Mockito

---

## 🔐 Security

- JWT Authentication
- Refresh Tokens
- BCrypt Password Encoding
- Role-based Authorization

---

## 📈 Future Enhancements

- Microservices (Docker + Kubernetes)
- Real Payment Integration (Stripe)
- Redis Caching
- Message Queues
- AI-based pricing

---

## 👨‍💻 Contributors

- George Sleibi
- Francis Lolas

---

## 📄 License

Educational project for SWER313 course.

---

⭐ George ⭐

## 📅 Assignment Due Date

[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/CU6l4amx)

---