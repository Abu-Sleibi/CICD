import { Link, useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
  CheckCircle,
  Calendar,
  BedDouble,
  MapPin,
  Users,
  Moon,
  Receipt,
  ArrowRight,
  Home,
} from 'lucide-react';
import { getBookingByReference } from '../../api/bookingsApi';
import { QUERY_KEYS } from '../../utils/queryKeys';
import { formatDate, formatCurrency, calculateNights } from '../../utils/dateUtils';
import BookingStatusBadge from '../../components/booking/BookingStatusBadge';
import Skeleton from '../../components/ui/Skeleton';
import Button from '../../components/ui/Button';

function ConfirmationPage() {
  const { ref } = useParams();

  const { data: booking, isLoading, isError } = useQuery({
    queryKey: QUERY_KEYS.BOOKING_REFERENCE(ref),
    queryFn: () => getBookingByReference(ref),
    enabled: !!ref,
    retry: 1,
  });

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
        <div className="w-full max-w-lg space-y-4">
          <Skeleton className="h-16 w-16 rounded-full mx-auto" />
          <Skeleton className="h-8 w-64 mx-auto" />
          <Skeleton className="h-4 w-48 mx-auto" />
          <Skeleton className="h-64 rounded-2xl" />
        </div>
      </div>
    );
  }

  if (isError || !booking) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
        <div className="text-center max-w-md">
          <div className="text-6xl mb-4">🔍</div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">Booking Not Found</h2>
          <p className="text-gray-500 mb-6">
            We couldn't find a booking with reference <strong>{ref}</strong>.
          </p>
          <Link to="/my-bookings">
            <Button variant="primary">View My Bookings</Button>
          </Link>
        </div>
      </div>
    );
  }

  const nights = calculateNights(booking.checkInDate, booking.checkOutDate);

  return (
    <div className="min-h-screen bg-gradient-to-b from-green-50 to-gray-50">
      <div className="max-w-2xl mx-auto px-4 sm:px-6 py-12">

        {/* Success header */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-20 h-20 rounded-full bg-green-100 mb-4">
            <CheckCircle size={44} className="text-green-500" />
          </div>
          <h1 className="text-3xl font-bold text-gray-900 mb-1">Booking Confirmed!</h1>
          <p className="text-gray-500">
            Your reservation is confirmed. A confirmation email has been sent.
          </p>
        </div>

        {/* Reference pill */}
        <div className="flex justify-center mb-6">
          <div className="inline-flex items-center gap-2 bg-white border-2 border-green-200 rounded-full px-5 py-2 shadow-sm">
            <Receipt size={16} className="text-green-600" />
            <span className="text-sm text-gray-500 font-medium">Booking Reference</span>
            <span className="font-bold text-gray-900 tracking-wider">{booking.bookingReference}</span>
          </div>
        </div>

        {/* Details card */}
        <div className="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden mb-6">
          {/* Hotel name header */}
          <div className="bg-gradient-to-r from-amber-500 to-orange-500 px-6 py-5">
            <h2 className="text-xl font-bold text-white">{booking.hotelName}</h2>
            <div className="flex items-center gap-1.5 text-amber-100 text-sm mt-1">
              <MapPin size={13} />
              <span>{booking.hotelName}</span>
            </div>
          </div>

          <div className="p-6 space-y-4">
            {/* Room */}
            <div className="flex items-center gap-3">
              <div className="flex-shrink-0 w-9 h-9 rounded-xl bg-amber-50 flex items-center justify-center">
                <BedDouble size={18} className="text-amber-500" />
              </div>
              <div>
                <p className="text-xs text-gray-400 uppercase tracking-wide">Room Type</p>
                <p className="font-semibold text-gray-900">
                  {booking.roomTypeName}
                  {booking.numberOfRooms > 1 && (
                    <span className="font-normal text-gray-500"> × {booking.numberOfRooms}</span>
                  )}
                </p>
              </div>
            </div>

            {/* Dates */}
            <div className="flex items-center gap-3">
              <div className="flex-shrink-0 w-9 h-9 rounded-xl bg-amber-50 flex items-center justify-center">
                <Calendar size={18} className="text-amber-500" />
              </div>
              <div className="flex-1">
                <p className="text-xs text-gray-400 uppercase tracking-wide">Dates</p>
                <div className="flex items-center gap-3 mt-0.5">
                  <div>
                    <p className="text-xs text-gray-400">Check-in</p>
                    <p className="font-semibold text-gray-900">{formatDate(booking.checkInDate)}</p>
                  </div>
                  <ArrowRight size={14} className="text-gray-300 flex-shrink-0" />
                  <div>
                    <p className="text-xs text-gray-400">Check-out</p>
                    <p className="font-semibold text-gray-900">{formatDate(booking.checkOutDate)}</p>
                  </div>
                </div>
              </div>
            </div>

            {/* Duration + guests */}
            <div className="flex items-center gap-6">
              <div className="flex items-center gap-2 text-sm text-gray-600">
                <Moon size={15} className="text-amber-400" />
                <span><strong>{nights}</strong> {nights === 1 ? 'night' : 'nights'}</span>
              </div>
              <div className="flex items-center gap-2 text-sm text-gray-600">
                <Users size={15} className="text-amber-400" />
                <span><strong>{booking.numberOfGuests}</strong> {booking.numberOfGuests === 1 ? 'guest' : 'guests'}</span>
              </div>
            </div>

            <hr className="border-gray-100" />

            {/* Price + status */}
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs text-gray-400 uppercase tracking-wide">Total Price</p>
                <p className="text-2xl font-bold text-amber-600">
                  {formatCurrency(booking.totalPrice)}
                </p>
              </div>
              <BookingStatusBadge status={booking.status} />
            </div>
          </div>
        </div>

        {/* Info note */}
        <div className="bg-blue-50 border border-blue-100 rounded-2xl px-5 py-4 mb-8 text-sm text-blue-700">
          <strong>What's next?</strong> Your booking is pending payment confirmation. Once confirmed,
          you'll receive a full confirmation email. You can view or cancel your booking anytime from
          My Bookings.
        </div>

        {/* Actions */}
        <div className="flex flex-col sm:flex-row gap-3">
          <Link to="/my-bookings" className="flex-1">
            <Button variant="primary" size="lg" className="w-full" leftIcon={<Receipt size={18} />}>
              View My Bookings
            </Button>
          </Link>
          <Link to="/" className="flex-1">
            <Button variant="secondary" size="lg" className="w-full" leftIcon={<Home size={18} />}>
              Back to Home
            </Button>
          </Link>
        </div>
      </div>
    </div>
  );
}

export default ConfirmationPage;
