import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

/**
 * Redirects unauthenticated users to /login.
 * Preserves the original location so we can redirect back after login.
 */
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children;
};

export default ProtectedRoute;
