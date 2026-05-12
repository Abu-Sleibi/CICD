import axiosInstance from './axiosInstance';

export const createBooking = async (data) => {
  const response = await axiosInstance.post('/bookings', data);
  return response.data;
};

/** GET /bookings/me — bookings for the JWT-authenticated user */
export const getMyBookings = async ({ page = 0, size = 20 } = {}) => {
  const response = await axiosInstance.get('/bookings/me', {
    params: { page, size, sort: 'createdAt,desc' },
  });
  return response.data;
};

export const getGuestBookings = async (guestId, { page = 0, size = 20 } = {}) => {
  const response = await axiosInstance.get(`/bookings/guests/${guestId}`, { params: { page, size } });
  return response.data;
};

export const getBookingById = async (id) => {
  const response = await axiosInstance.get(`/bookings/${id}`);
  return response.data;
};

export const getBookingByReference = async (ref) => {
  const response = await axiosInstance.get(`/bookings/reference/${ref}`);
  return response.data;
};

/** PUT /bookings/{id}/cancel */
export const cancelBooking = async (id, reason) => {
  const response = await axiosInstance.put(`/bookings/${id}/cancel`, { reason });
  return response.data;
};

/** PUT /bookings/{id}/confirm */
export const confirmBooking = async (id) => {
  const response = await axiosInstance.put(`/bookings/${id}/confirm`);
  return response.data;
};

/** PUT /bookings/{id}/complete */
export const completeBooking = async (id) => {
  const response = await axiosInstance.put(`/bookings/${id}/complete`);
  return response.data;
};

/** PUT /bookings/{id}/no-show */
export const markNoShow = async (id) => {
  const response = await axiosInstance.put(`/bookings/${id}/no-show`);
  return response.data;
};

/** GET /bookings/hotels/{hotelId} — paginated hotel bookings (manager) */
export const getHotelBookings = async (hotelId, { page = 0, size = 20 } = {}) => {
  const response = await axiosInstance.get(`/bookings/hotels/${hotelId}`, {
    params: { page, size, sort: 'checkInDate,asc' },
  });
  return response.data;
};

/** GET /bookings/hotels/{hotelId}/status/{status} */
export const getHotelBookingsByStatus = async (hotelId, status, { page = 0, size = 20 } = {}) => {
  const response = await axiosInstance.get(`/bookings/hotels/${hotelId}/status/${status}`, {
    params: { page, size },
  });
  return response.data;
};

/** GET /bookings/hotel/{hotelId}/upcoming */
export const getUpcomingBookings = async (hotelId, { page = 0, size = 10 } = {}) => {
  const response = await axiosInstance.get(`/bookings/hotel/${hotelId}/upcoming`, {
    params: { page, size },
  });
  return response.data;
};

/** GET /bookings/hotels/{hotelId}/statistics */
export const getHotelStatistics = async (hotelId) => {
  const response = await axiosInstance.get(`/bookings/hotels/${hotelId}/statistics`);
  return response.data;
};

/** GET /bookings/search — admin/manager search with filters */
export const searchBookings = async (params = {}) => {
  const clean = Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== '' && v != null)
  );
  const response = await axiosInstance.get('/bookings/search', { params: clean });
  return response.data;
};
