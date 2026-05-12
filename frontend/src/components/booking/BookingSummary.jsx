import { MapPin, Calendar, Users, BedDouble, Moon } from 'lucide-react';
import { formatDate, calculateNights, formatCurrency } from '../../utils/dateUtils';

function BookingSummary({ booking }) {
  if (!booking) return null;

  const {
    hotelName,
    roomTypeName,
    checkInDate,
    checkOutDate,
    numberOfRooms = 1,
    numberOfGuests = 1,
    pricePerNight,
    totalPrice,
    city,
    country,
  } = booking;

  const nights = calculateNights(checkInDate, checkOutDate);
  const roomNightPrice = pricePerNight ? pricePerNight * numberOfRooms * nights : null;
  const taxes = totalPrice ? totalPrice * 0.1 : null;

  return (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
      {/* Header */}
      <div className="bg-gradient-to-r from-amber-500 to-orange-500 px-5 py-4">
        <h3 className="font-semibold text-white text-base">Booking Summary</h3>
      </div>

      <div className="p-5 space-y-4">
        {/* Hotel info */}
        {hotelName && (
          <div>
            <p className="font-semibold text-gray-900 text-base">{hotelName}</p>
            {(city || country) && (
              <div className="flex items-center gap-1 text-sm text-gray-500 mt-0.5">
                <MapPin size={13} />
                <span>{[city, country].filter(Boolean).join(', ')}</span>
              </div>
            )}
          </div>
        )}

        <hr className="border-gray-100" />

        {/* Room */}
        {roomTypeName && (
          <div className="flex items-center gap-2 text-sm">
            <BedDouble size={16} className="text-amber-500 flex-shrink-0" />
            <div>
              <span className="text-gray-500">Room: </span>
              <span className="font-medium text-gray-900">{roomTypeName}</span>
              {numberOfRooms > 1 && (
                <span className="text-gray-500"> × {numberOfRooms}</span>
              )}
            </div>
          </div>
        )}

        {/* Dates */}
        {checkInDate && (
          <div className="flex items-start gap-2 text-sm">
            <Calendar size={16} className="text-amber-500 flex-shrink-0 mt-0.5" />
            <div>
              <div>
                <span className="text-gray-500">Check-in: </span>
                <span className="font-medium text-gray-900">{formatDate(checkInDate)}</span>
              </div>
              <div>
                <span className="text-gray-500">Check-out: </span>
                <span className="font-medium text-gray-900">{formatDate(checkOutDate)}</span>
              </div>
            </div>
          </div>
        )}

        {/* Duration */}
        {nights > 0 && (
          <div className="flex items-center gap-2 text-sm">
            <Moon size={16} className="text-amber-500 flex-shrink-0" />
            <span className="text-gray-700">
              <span className="font-medium">{nights}</span> {nights === 1 ? 'night' : 'nights'}
            </span>
          </div>
        )}

        {/* Guests */}
        {numberOfGuests > 0 && (
          <div className="flex items-center gap-2 text-sm">
            <Users size={16} className="text-amber-500 flex-shrink-0" />
            <span className="text-gray-700">
              <span className="font-medium">{numberOfGuests}</span> {numberOfGuests === 1 ? 'guest' : 'guests'}
            </span>
          </div>
        )}

        <hr className="border-gray-100" />

        {/* Price breakdown */}
        <div className="space-y-2 text-sm">
          {pricePerNight && nights > 0 && (
            <div className="flex justify-between text-gray-600">
              <span>{formatCurrency(pricePerNight)} × {nights} nights{numberOfRooms > 1 ? ` × ${numberOfRooms} rooms` : ''}</span>
              <span>{formatCurrency(roomNightPrice)}</span>
            </div>
          )}
          {taxes && (
            <div className="flex justify-between text-gray-500">
              <span>Taxes & fees (10%)</span>
              <span>{formatCurrency(taxes)}</span>
            </div>
          )}
          {totalPrice && (
            <div className="flex justify-between font-bold text-gray-900 text-base pt-2 border-t border-gray-100">
              <span>Total</span>
              <span className="text-amber-600">{formatCurrency(totalPrice)}</span>
            </div>
          )}
        </div>

        {/* Free cancellation note */}
        <div className="bg-green-50 border border-green-100 rounded-lg px-3 py-2 text-xs text-green-700">
          Free cancellation available
        </div>
      </div>
    </div>
  );
}

export default BookingSummary;
