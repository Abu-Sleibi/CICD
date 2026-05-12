import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { Menu, X, LogOut, User, CalendarCheck, LayoutDashboard } from 'lucide-react';
import toast from 'react-hot-toast';

const publicLinks = [
  { to: '/', label: 'Home' },
  { to: '/search', label: 'Hotels' },
];

function Navbar() {
  const { isAuthenticated, user, isManager, logout } = useAuth();
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const handleLogout = () => {
    logout();
    toast.success('Logged out successfully');
    navigate('/');
    setMenuOpen(false);
    setDropdownOpen(false);
  };

  const initials = user?.fullName
    ? user.fullName
        .split(' ')
        .map((n) => n[0])
        .join('')
        .toUpperCase()
        .slice(0, 2)
    : user?.username?.[0]?.toUpperCase() ?? 'U';

  return (
    <header className="sticky top-0 z-50 bg-white/80 backdrop-blur-md border-b border-gray-100 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="flex flex-col items-start no-underline leading-none">
            <span style={{ fontFamily: "'Playfair Display', Georgia, serif", fontWeight: 700, letterSpacing: '0.15em', color: '#C9A84C', fontSize: '1.25rem' }}>
              VELOUR
            </span>
            <span style={{ fontFamily: 'inherit', fontStyle: 'italic', fontSize: '0.6rem', letterSpacing: '0.05em', color: '#9ca3af' }}>
              Redefine Your Stay
            </span>
          </Link>

          {/* Desktop nav links */}
          <nav className="hidden md:flex items-center gap-6">
            {publicLinks.map(({ to, label }) => (
              <NavLink
                key={to}
                to={to}
                end={to === '/'}
                className={({ isActive }) =>
                  `text-sm font-medium transition-colors no-underline ${
                    isActive ? 'text-amber-500' : 'text-gray-600 hover:text-gray-900'
                  }`
                }
              >
                {label}
              </NavLink>
            ))}
            {isManager && (
              <NavLink
                to="/manager/dashboard"
                className={({ isActive }) =>
                  `text-sm font-medium transition-colors no-underline ${
                    isActive ? 'text-amber-500' : 'text-gray-600 hover:text-gray-900'
                  }`
                }
              >
                Manager
              </NavLink>
            )}
          </nav>

          {/* Desktop auth buttons */}
          <div className="hidden md:flex items-center gap-3">
            {isAuthenticated ? (
              <div className="relative">
                <button
                  onClick={() => setDropdownOpen((o) => !o)}
                  className="flex items-center gap-2 px-3 py-1.5 rounded-lg hover:bg-gray-100 transition-colors"
                >
                  <div className="w-8 h-8 bg-amber-500 rounded-full flex items-center justify-center text-white text-sm font-semibold">
                    {initials}
                  </div>
                  <span className="text-sm font-medium text-gray-700 max-w-[120px] truncate">
                    {user?.fullName || user?.username}
                  </span>
                </button>

                {dropdownOpen && (
                  <>
                    <div
                      className="fixed inset-0 z-10"
                      onClick={() => setDropdownOpen(false)}
                    />
                    <div className="absolute right-0 top-full mt-2 w-52 bg-white border border-gray-100 rounded-xl shadow-lg z-20 overflow-hidden">
                      <div className="px-4 py-3 border-b border-gray-100">
                        <p className="text-sm font-semibold text-gray-900 truncate">
                          {user?.fullName || user?.username}
                        </p>
                        <p className="text-xs text-gray-500 truncate">{user?.email}</p>
                      </div>
                      <div className="py-1">
                        <Link
                          to="/my-bookings"
                          onClick={() => setDropdownOpen(false)}
                          className="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 no-underline"
                        >
                          <CalendarCheck size={16} />
                          My Bookings
                        </Link>
                        {isManager && (
                          <Link
                            to="/manager/dashboard"
                            onClick={() => setDropdownOpen(false)}
                            className="flex items-center gap-3 px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 no-underline"
                          >
                            <LayoutDashboard size={16} />
                            Manager Dashboard
                          </Link>
                        )}
                        <button
                          onClick={handleLogout}
                          className="flex items-center gap-3 w-full px-4 py-2 text-sm text-red-600 hover:bg-red-50"
                        >
                          <LogOut size={16} />
                          Logout
                        </button>
                      </div>
                    </div>
                  </>
                )}
              </div>
            ) : (
              <>
                <Link
                  to="/login"
                  className="text-sm font-medium text-gray-600 hover:text-gray-900 px-3 py-1.5 rounded-lg hover:bg-gray-100 transition-colors no-underline"
                >
                  Login
                </Link>
                <Link
                  to="/register"
                  className="text-sm font-medium text-white bg-amber-500 hover:bg-amber-600 px-4 py-1.5 rounded-lg transition-colors no-underline"
                >
                  Sign Up
                </Link>
              </>
            )}
          </div>

          {/* Mobile hamburger */}
          <button
            onClick={() => setMenuOpen((o) => !o)}
            className="md:hidden p-2 rounded-lg text-gray-600 hover:text-gray-900 hover:bg-gray-100 transition-colors"
          >
            {menuOpen ? <X size={22} /> : <Menu size={22} />}
          </button>
        </div>
      </div>

      {/* Mobile menu */}
      {menuOpen && (
        <div className="md:hidden border-t border-gray-100 bg-white shadow-lg">
          <div className="px-4 py-3 space-y-1">
            {publicLinks.map(({ to, label }) => (
              <NavLink
                key={to}
                to={to}
                end={to === '/'}
                onClick={() => setMenuOpen(false)}
                className={({ isActive }) =>
                  `block px-3 py-2.5 rounded-lg text-sm font-medium no-underline ${
                    isActive ? 'bg-amber-50 text-amber-600' : 'text-gray-700 hover:bg-gray-50'
                  }`
                }
              >
                {label}
              </NavLink>
            ))}
            {isManager && (
              <NavLink
                to="/manager/dashboard"
                onClick={() => setMenuOpen(false)}
                className={({ isActive }) =>
                  `block px-3 py-2.5 rounded-lg text-sm font-medium no-underline ${
                    isActive ? 'bg-amber-50 text-amber-600' : 'text-gray-700 hover:bg-gray-50'
                  }`
                }
              >
                Manager Dashboard
              </NavLink>
            )}
          </div>

          <div className="border-t border-gray-100 px-4 py-3 space-y-1">
            {isAuthenticated ? (
              <>
                <div className="flex items-center gap-3 px-3 py-2">
                  <div className="w-9 h-9 bg-amber-500 rounded-full flex items-center justify-center text-white font-semibold text-sm">
                    {initials}
                  </div>
                  <div>
                    <p className="text-sm font-semibold text-gray-900">{user?.fullName || user?.username}</p>
                    <p className="text-xs text-gray-500">{user?.email}</p>
                  </div>
                </div>
                <Link
                  to="/my-bookings"
                  onClick={() => setMenuOpen(false)}
                  className="flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 no-underline"
                >
                  <CalendarCheck size={16} />
                  My Bookings
                </Link>
                <button
                  onClick={handleLogout}
                  className="flex items-center gap-3 w-full px-3 py-2.5 rounded-lg text-sm font-medium text-red-600 hover:bg-red-50"
                >
                  <LogOut size={16} />
                  Logout
                </button>
              </>
            ) : (
              <div className="flex flex-col gap-2">
                <Link
                  to="/login"
                  onClick={() => setMenuOpen(false)}
                  className="block px-3 py-2.5 rounded-lg text-sm font-medium text-center text-gray-700 border border-gray-200 hover:bg-gray-50 no-underline"
                >
                  Login
                </Link>
                <Link
                  to="/register"
                  onClick={() => setMenuOpen(false)}
                  className="block px-3 py-2.5 rounded-lg text-sm font-medium text-center text-white bg-amber-500 hover:bg-amber-600 no-underline"
                >
                  Sign Up
                </Link>
              </div>
            )}
          </div>
        </div>
      )}
    </header>
  );
}

export default Navbar;
