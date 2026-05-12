import { Navigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { useAuth } from '../hooks/useAuth';
import { useEffect, useRef } from 'react';

/**
 * Redirects to / with a toast if the user is not a HOTEL_MANAGER or ADMIN.
 * Also redirects to /login if not authenticated.
 */
const ManagerRoute = ({ children }) => {
  const { isAuthenticated, isManager } = useAuth();
  const hasToasted = useRef(false);

  useEffect(() => {
    if (isAuthenticated && !isManager && !hasToasted.current) {
      hasToasted.current = true;
      toast.error('Access denied. Manager or Admin role required.');
    }
  }, [isAuthenticated, isManager]);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!isManager) {
    return <Navigate to="/" replace />;
  }

  return children;
};

export default ManagerRoute;
