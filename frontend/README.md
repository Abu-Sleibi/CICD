# HotelHub Frontend

A production-quality React web application for Hotel Search & Booking Platform.

## Tech Stack

- **React 18** + Vite
- **React Router v6** for routing
- **TanStack Query v5** for server state management
- **React Hook Form** + **Zod** for form validation
- **Axios** for HTTP requests
- **Zustand** for client state management
- **Tailwind CSS v3** for styling
- **React Hot Toast** for notifications
- **date-fns** for date utilities
- **Lucide React** for icons

## Prerequisites

- Node.js 18+
- npm 9+
- Backend running at `http://localhost:8080`

## Getting Started

```bash
# Install dependencies
npm install

# Start development server
npm run dev
```

The app will be available at `http://localhost:5173`.

## Environment Variables

Copy `.env` and configure:

```env
VITE_API_URL=http://localhost:8080/api
```

## Features

- **Hotel Search** — Search by city, dates, guests, price range, star rating, amenities
- **Hotel Details** — Gallery, amenities, room types
- **Booking Flow** — Availability check → Checkout → Confirmation
- **My Bookings** — View and cancel bookings
- **Auth** — Login / Register with JWT
- **Manager Dashboard** — Hotel CRUD, room types, booking management

## Project Structure

```
src/
  api/          # Axios API clients
  components/   # Reusable UI components
  hooks/        # Custom React hooks
  pages/        # Route-level page components
  router/       # React Router configuration
  store/        # Zustand stores
  utils/        # Utility functions
```

## Build for Production

```bash
npm run build
npm run preview
```
