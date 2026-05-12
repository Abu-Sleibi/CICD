import axiosInstance from './axiosInstance';

/** GET /admin/hotels — all hotels across the platform (admin only) */
export const getAdminHotels = async ({ page = 0, size = 20 } = {}) => {
  const response = await axiosInstance.get('/admin/hotels', { params: { page, size } });
  return response.data;
};

/** GET /admin/statistics — platform-wide aggregate stats */
export const getAdminStatistics = async () => {
  const response = await axiosInstance.get('/admin/statistics');
  return response.data;
};

/** GET /admin/bookings — all bookings across the platform */
export const getAllBookings = async ({ page = 0, size = 20, status } = {}) => {
  const params = { page, size };
  if (status && status !== 'ALL') params.status = status;
  const response = await axiosInstance.get('/admin/bookings', { params });
  return response.data;
};

/** GET /admin/users — all registered users */
export const getAdminUsers = async ({ page = 0, size = 20 } = {}) => {
  const response = await axiosInstance.get('/admin/users', { params: { page, size } });
  return response.data;
};

/** PUT /admin/hotels/{id}/activate — make a hotel visible to guests */
export const activateHotel = async (id) => {
  const response = await axiosInstance.put(`/admin/hotels/${id}/activate`);
  return response.data;
};

/** PUT /admin/hotels/{id}/deactivate — hide a hotel from guests */
export const deactivateHotel = async (id) => {
  const response = await axiosInstance.put(`/admin/hotels/${id}/deactivate`);
  return response.data;
};

/** DELETE /admin/users/{id} — remove a user account */
export const deleteAdminUser = async (id) => {
  const response = await axiosInstance.delete(`/admin/users/${id}`);
  return response.data;
};
