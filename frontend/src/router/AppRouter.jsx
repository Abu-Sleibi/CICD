import { BrowserRouter, Routes, Route, NavLink, useNavigate } from 'react-router-dom';
import Layout from '../components/layout/Layout';
import ProtectedRoute from './ProtectedRoute';
import ManagerRoute from './ManagerRoute';
import { useAuth } from '../hooks/useAuth';
import {
  LayoutDashboard,
  Hotel,
  CalendarCheck,
  LogOut,
  Menu,
  X,
} from 'lucide-react';
import { useState } from 'react';
import toast from 'react-hot-toast';

// Pages
import HomePage from '../pages/home/HomePage';
import SearchPage from '../pages/search/SearchPage';
import HotelDetailPage from '../pages/hotel/HotelDetailPage';
import AvailabilityPage from '../pages/booking/AvailabilityPage';
import CheckoutPage from '../pages/booking/CheckoutPage';
import ConfirmationPage from '../pages/booking/ConfirmationPage';
import MyBookingsPage from '../pages/myBookings/MyBookingsPage';
import LoginPage from '../pages/auth/LoginPage';
import RegisterPage from '../pages/auth/RegisterPage';
import DashboardPage from '../pages/manager/DashboardPage';
import HotelsManagementPage from '../pages/manager/HotelsManagementPage';
import HotelFormPage from '../pages/manager/HotelFormPage';
import RoomTypesPage from '../pages/manager/RoomTypesPage';
import ManagerBookingsPage from '../pages/manager/ManagerBookingsPage';

// ─── Manager sidebar layout ────────────────────────────────────────────────

const managerNavLinks = [
  { to: '/manager/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/manager/hotels', label: 'Hotels', icon: Hotel },
  { to: '/manager/bookings', label: 'Bookings', icon: CalendarCheck },
];

function ManagerLayout({ children }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleLogout = () => {
    logout();
    toast.success('Logged out successfully');
    navigate('/');
  };

  return (
    <div className="min-h-screen bg-gray-100 flex">
      {/* Mobile overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-20 bg-black/50 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`fixed inset-y-0 left-0 z-30 w-64 bg-gray-900 text-white flex flex-col transform transition-transform duration-300 lg:static lg:translate-x-0 ${
          sidebarOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        {/* Logo */}
        <div className="flex items-center justify-between h-16 px-6 border-b border-gray-700">
          <span style={{ fontFamily: "'Playfair Display', Georgia, serif", fontWeight: 700, letterSpacing: '0.15em', color: '#C9A84C', fontSize: '1.15rem' }}>VELOUR</span>
          <button
            onClick={() => setSidebarOpen(false)}
            className="lg:hidden text-gray-400 hover:text-white"
          >
            <X size={20} />
          </button>
        </div>

        {/* Manager info */}
        <div className="px-6 py-4 border-b border-gray-700">
          <p className="text-xs text-gray-400 uppercase tracking-wider mb-1">Signed in as</p>
          <p className="font-medium text-white truncate">{user?.fullName || user?.username || 'Manager'}</p>
          <p className="text-xs text-gray-400 truncate">{user?.email}</p>
        </div>

        {/* Nav links */}
        <nav className="flex-1 px-3 py-4 space-y-1">
          {managerNavLinks.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-amber-500 text-white'
                    : 'text-gray-300 hover:bg-gray-800 hover:text-white'
                }`
              }
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>

        {/* Logout */}
        <div className="px-3 py-4 border-t border-gray-700">
          <button
            onClick={handleLogout}
            className="flex items-center gap-3 w-full px-3 py-2.5 rounded-lg text-sm font-medium text-gray-300 hover:bg-red-600 hover:text-white transition-colors"
          >
            <LogOut size={18} />
            Logout
          </button>
        </div>
      </aside>

      {/* Main content */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Top bar (mobile) */}
        <header className="lg:hidden h-16 bg-white border-b border-gray-200 flex items-center px-4 gap-3">
          <button
            onClick={() => setSidebarOpen(true)}
            className="text-gray-600 hover:text-gray-900"
          >
            <Menu size={22} />
          </button>
          <span style={{ fontFamily: "'Playfair Display', Georgia, serif", fontWeight: 700, letterSpacing: '0.12em', color: '#C9A84C', fontSize: '1.1rem' }}>VELOUR Manager</span>
        </header>

        <main className="flex-1 p-6 overflow-auto">{children}</main>
      </div>
    </div>
  );
}

// ─── Router ────────────────────────────────────────────────────────────────

function AppRouter() {
  return (
    <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <Routes>
        {/* Public routes wrapped in standard Layout */}
        <Route
          path="/"
          element={
            <Layout>
              <HomePage />
            </Layout>
          }
        />
        <Route
          path="/search"
          element={
            <Layout>
              <SearchPage />
            </Layout>
          }
        />
        <Route
          path="/hotels/:id"
          element={
            <Layout>
              <HotelDetailPage />
            </Layout>
          }
        />
        <Route
          path="/hotels/:hotelId/availability"
          element={
            <Layout>
              <AvailabilityPage />
            </Layout>
          }
        />

        {/* Auth routes */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Protected guest routes */}
        <Route
          path="/checkout"
          element={
            <ProtectedRoute>
              <Layout>
                <CheckoutPage />
              </Layout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/booking/confirmation/:ref"
          element={
            <ProtectedRoute>
              <Layout>
                <ConfirmationPage />
              </Layout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-bookings"
          element={
            <ProtectedRoute>
              <Layout>
                <MyBookingsPage />
              </Layout>
            </ProtectedRoute>
          }
        />

        {/* Manager routes */}
        <Route
          path="/manager/dashboard"
          element={
            <ManagerRoute>
              <ManagerLayout>
                <DashboardPage />
              </ManagerLayout>
            </ManagerRoute>
          }
        />
        <Route
          path="/manager/hotels"
          element={
            <ManagerRoute>
              <ManagerLayout>
                <HotelsManagementPage />
              </ManagerLayout>
            </ManagerRoute>
          }
        />
        <Route
          path="/manager/hotels/new"
          element={
            <ManagerRoute>
              <ManagerLayout>
                <HotelFormPage />
              </ManagerLayout>
            </ManagerRoute>
          }
        />
        <Route
          path="/manager/hotels/:id/edit"
          element={
            <ManagerRoute>
              <ManagerLayout>
                <HotelFormPage />
              </ManagerLayout>
            </ManagerRoute>
          }
        />
        <Route
          path="/manager/hotels/:hotelId/rooms"
          element={
            <ManagerRoute>
              <ManagerLayout>
                <RoomTypesPage />
              </ManagerLayout>
            </ManagerRoute>
          }
        />
        <Route
          path="/manager/bookings"
          element={
            <ManagerRoute>
              <ManagerLayout>
                <ManagerBookingsPage />
              </ManagerLayout>
            </ManagerRoute>
          }
        />

        {/* 404 fallback */}
        <Route
          path="*"
          element={
            <Layout>
              <div className="min-h-[60vh] flex flex-col items-center justify-center text-center px-4">
                <h1 className="text-6xl font-bold text-amber-500 mb-4">404</h1>
                <h2 className="text-2xl font-semibold text-gray-900 mb-2">Page Not Found</h2>
                <p className="text-gray-500 mb-8">
                  The page you're looking for doesn't exist or has been moved.
                </p>
                <a
                  href="/"
                  className="inline-flex items-center px-6 py-3 bg-amber-500 text-white rounded-lg font-medium hover:bg-amber-600 transition-colors"
                >
                  Go Home
                </a>
              </div>
            </Layout>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}

export default AppRouter;
