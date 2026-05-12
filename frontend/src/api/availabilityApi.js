import axiosInstance from './axiosInstance';

/**
 * Check room availability for given dates
 * @param {{ roomTypeId: string|number, checkInDate: string, checkOutDate: string, numberOfRooms: number }} data
 */
export const checkAvailability = async (data) => {
  const response = await axiosInstance.post('/availability/check', data);
  return response.data;
};

/**
 * Calculate total price for a booking
 * @param {{ roomTypeId: string|number, checkInDate: string, checkOutDate: string, numberOfRooms: number }} data
 */
export const calculatePrice = async (data) => {
  const response = await axiosInstance.post('/availability/calculate-price', data);
  return response.data;
};
