import axiosInstance from './axiosInstance';

/**
 * Login with email + password
 * @param {{ email: string, password: string }} credentials
 * @returns {Promise<{ accessToken, refreshToken, id, username, email, fullName, roles, hotelId }>}
 */
export const login = async (credentials) => {
  const response = await axiosInstance.post('/auth/login', credentials);
  return response.data;
};

/**
 * Register a new user account
 * @param {{ fullName: string, email: string, password: string, username?: string }} data
 * @returns {Promise<any>}
 */
export const register = async (data) => {
  const response = await axiosInstance.post('/auth/register', data);
  return response.data;
};

/**
 * Get the currently authenticated user's profile
 * @returns {Promise<any>}
 */
export const getMe = async () => {
  const response = await axiosInstance.get('/auth/me');
  return response.data;
};
