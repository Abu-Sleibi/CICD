export const QUERY_KEYS = {
  // Auth
  ME: ['auth', 'me'],

  // Hotels
  HOTELS: ['hotels'],
  HOTEL_SEARCH: (params) => ['hotels', 'search', params],
  HOTEL_DETAIL: (id) => ['hotels', id],
  MANAGER_HOTELS: ['hotels', 'manager'],

  // Room Types
  ROOM_TYPES: (hotelId) => ['roomTypes', hotelId],

  // Bookings
  MY_BOOKINGS: (page) => ['bookings', 'me', page],
  GUEST_BOOKINGS: (guestId) => ['bookings', 'guest', guestId],
  BOOKING_DETAIL: (id) => ['bookings', id],
  BOOKING_REFERENCE: (ref) => ['bookings', 'reference', ref],
  HOTEL_BOOKINGS: (hotelId) => ['bookings', 'hotel', hotelId],
  HOTEL_BOOKINGS_STATUS: (hotelId, status) => ['bookings', 'hotel', hotelId, status],
  UPCOMING_BOOKINGS: (hotelId) => ['bookings', 'hotel', hotelId, 'upcoming'],
  HOTEL_STATS: (hotelId) => ['bookings', 'hotel', hotelId, 'stats'],

  // Availability
  AVAILABILITY: (data) => ['availability', 'check', data],
  PRICE: (data) => ['availability', 'price', data],

  // Reviews
  HOTEL_REVIEWS: (hotelId) => ['reviews', 'hotel', hotelId],
  HOTEL_RATING: (hotelId) => ['reviews', 'rating', hotelId],
};
