import axiosInstance from './axiosInstance';

export const getRoomTypesByHotel = async (hotelId) => {
  const response = await axiosInstance.get(`/hotels/${hotelId}/room-types`);
  return response.data;
};

export const createRoomType = async (data) => {
  const { pricePerNight, imageUrl, ...rest } = data;
  const response = await axiosInstance.post('/hotels/room-types', {
    ...rest,
    basePrice: pricePerNight,
  });
  return response.data;
};

export const updateRoomType = async (id, data) => {
  const { pricePerNight, imageUrl, ...rest } = data;
  const response = await axiosInstance.put(`/hotels/room-types/${id}`, {
    ...rest,
    basePrice: pricePerNight,
  });
  return response.data;
};

export const deleteRoomType = async (id) => {
  await axiosInstance.delete(`/hotels/room-types/${id}`);
};
