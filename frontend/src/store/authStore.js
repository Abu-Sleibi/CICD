import { create } from 'zustand';

export const useAuthStore = create((set) => ({
  user: null,
  token: null,
  isAuthenticated: false,

  /**
   * Called on app mount — restores auth state from localStorage
   */
  rehydrate: () => {
    try {
      const token = localStorage.getItem('token');
      const userStr = localStorage.getItem('user');
      if (token && userStr) {
        const user = JSON.parse(userStr);
        set({ user, token, isAuthenticated: true });
      }
    } catch {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    }
  },

  /**
   * Called on successful login
   * @param {object} user
   * @param {string} token
   */
  login: (user, token) => {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
    set({ user, token, isAuthenticated: true });
  },

  /**
   * Called on logout or 401
   */
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    set({ user: null, token: null, isAuthenticated: false });
  },
}));
