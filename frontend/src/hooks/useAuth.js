import { useAuthStore } from '../store/authStore';

const MANAGER_ROLES = ['ROLE_HOTEL_MANAGER', 'HOTEL_MANAGER'];
const ADMIN_ROLES = ['ROLE_ADMIN', 'ADMIN'];

/**
 * Convenience hook that reads from authStore and exposes
 * derived booleans for role-based access control.
 */
export const useAuth = () => {
  const user = useAuthStore((state) => state.user);
  const token = useAuthStore((state) => state.token);
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const loginAction = useAuthStore((state) => state.login);
  const logoutAction = useAuthStore((state) => state.logout);

  const roles = user?.roles ?? [];

  const isAdmin = roles.some((r) => ADMIN_ROLES.includes(r));
  const isManager = roles.some((r) => MANAGER_ROLES.includes(r)) || isAdmin;

  return {
    user,
    token,
    isAuthenticated,
    isManager,
    isAdmin,
    login: loginAction,
    logout: logoutAction,
  };
};
