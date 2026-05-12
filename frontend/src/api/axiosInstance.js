import axios from 'axios';
import toast from 'react-hot-toast';

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
});

// Request interceptor — attach Bearer token from localStorage
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor — handle 401, extract error messages
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;

    if (status === 401) {
      // Clear storage
      localStorage.removeItem('token');
      localStorage.removeItem('user');

      // Import store dynamically to avoid circular deps
      import('../store/authStore').then(({ useAuthStore }) => {
        useAuthStore.getState().logout();
      });

      toast.error('Session expired. Please log in again.');
      window.location.href = '/login';
      return Promise.reject(error);
    }

    // Extract meaningful error message
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.response?.data ||
      error.message ||
      'An unexpected error occurred';

    const extractedError = new Error(
      typeof message === 'string' ? message : JSON.stringify(message)
    );
    extractedError.status = status;
    extractedError.originalError = error;

    return Promise.reject(extractedError);
  }
);

export default axiosInstance;
