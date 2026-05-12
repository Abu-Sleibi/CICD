import axiosInstance from './axiosInstance';

/**
 * Search hotels with optional filters
 * @param {{ city?, checkIn?, checkOut?, guests?, minPrice?, maxPrice?, rating?, amenities?, page?, size? }} params
 */
export const searchHotels = async (params = {}) => {
  const cleanParams = Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== '' && v !== null && v !== undefined)
  );
  const response = await axiosInstance.get('/hotels/search', { params: cleanParams });
  return response.data;
};

/**
 * Get a hotel by ID
 * @param {string|number} id
 */
export const getHotelById = async (id) => {
  const response = await axiosInstance.get(`/hotels/${id}`);
  return response.data;
};

/**
 * Create a new hotel (manager/admin only)
 * @param {object} data
 */
export const createHotel = async (data) => {
  const response = await axiosInstance.post('/hotels', data);
  return response.data;
};

/**
 * Update an existing hotel (manager/admin only)
 * @param {string|number} id
 * @param {object} data
 */
export const updateHotel = async (id, data) => {
  const response = await axiosInstance.put(`/hotels/${id}`, data);
  return response.data;
};

/**
 * Delete a hotel (manager/admin only)
 * @param {string|number} id
 */
export const deleteHotel = async (id) => {
  const response = await axiosInstance.delete(`/hotels/${id}`);
  return response.data;
};

/**
 * Get hotels managed by the currently authenticated manager
 */
export const getManagerHotels = async () => {
  const response = await axiosInstance.get('/hotels/manager');
  return response.data;
};
