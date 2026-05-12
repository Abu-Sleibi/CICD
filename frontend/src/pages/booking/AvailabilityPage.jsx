import { useState } from 'react';
import { useParams, useNavigate, useSearchParams, Link } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { format, addDays } from 'date-fns';
import { Calendar, Users, ArrowLeft, CheckCircle, BedDouble, Lock } from 'lucide-react';
import toast from 'react-hot-toast';
import { getHotelById } from '../../api/hotelsApi';
import { getRoomTypesByHotel } from '../../api/roomTypesApi';
import { checkAvailability, calculatePrice } from '../../api/availabilityApi';
import { QUERY_KEYS } from '../../utils/queryKeys';
import { formatCurrency, calculateNights } from '../../utils/dateUtils';
import { formatPrice } from '../../utils/formatters';
import { useAuth } from '../../hooks/useAuth';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Skeleton from '../../components/ui/Skeleton';

const today = format(new Date(), 'yyyy-MM-dd');
const tomorrow = format(addDays(new Date(), 1), 'yyyy-MM-dd');

const schema = z
  .object({
    checkInDate: z.string().min(1, 'Check-in date is required'),
    checkOutDate: z.string().min(1, 'Check-out date is required'),
    numberOfRooms: z.coerce.number().min(1, 'At least 1 room'),
    numberOfGuests: z.coerce.number().min(1, 'At least 1 guest'),
  })
  .refine((d) => d.checkOutDate > d.checkInDate, {
    message: 'Check-out must be after check-in',
    path: ['checkOutDate'],
  });

function LoginPromptModal({ onClose }) {
  const navigate = useNavigate();
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center px-4">
      <div className="absolute inset-0 bg-black/50 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-white rounded-2xl shadow-2xl p-8 max-w-sm w-full text-center z-10 animate-fade-in-up">
        <div className="inline-flex items-center justify-center w-14 h-14 rounded-full bg-amber-100 mb-4">
          <Lock size={24} className="text-amber-500" />
        </div>
        <h2 className="text-xl font-bold text-gray-900 mb-2">Sign in to continue</h2>
        <p className="text-gray-500 text-sm mb-6">
          Please log in or create a free account to check availability and book your stay.
        </p>
        <div className="flex flex-col gap-3">
          <Button variant="primary" className="w-full" onClick={() => navigate('/login')}>
            Sign In
          </Button>
          <Button variant="secondary" className="w-full" onClick={() => navigate('/register')}>
            Create Account
          </Button>
        </div>
        <button
          onClick={onClose}
          className="mt-4 text-sm text-gray-400 hover:text-gray-600 transition-colors"
        >
          Maybe later
        </button>
      </div>
    </div>
  );
}

function AvailabilityPage() {
  const { hotelId } = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const preselectedRoomTypeId = searchParams.get('roomTypeId');
  const { isAuthenticated } = useAuth();

  const [selectedRoomType, setSelectedRoomType] = useState(null);
  const [availabilityResult, setAvailabilityResult] = useState(null);
  const [priceResult, setPriceResult] = useState(null);
  const [showLoginPrompt, setShowLoginPrompt] = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues: {
      checkInDate: today,
      checkOutDate: tomorrow,
      numberOfRooms: 1,
      numberOfGuests: 2,
    },
  });

  const checkIn = watch('checkInDate');
  const checkOut = watch('checkOutDate');
  const numRooms = watch('numberOfRooms');

  const { data: hotel, isLoading: hotelLoading } = useQuery({
    queryKey: QUERY_KEYS.HOTEL_DETAIL(hotelId),
    queryFn: () => getHotelById(hotelId),
    enabled: !!hotelId,
  });

  const { data: roomTypes = [], isLoading: roomsLoading } = useQuery({
    queryKey: QUERY_KEYS.ROOM_TYPES(hotelId),
    queryFn: () => getRoomTypesByHotel(hotelId),
    enabled: !!hotelId,
    onSuccess: (data) => {
      if (preselectedRoomTypeId) {
        const found = data.find((rt) => String(rt.id) === String(preselectedRoomTypeId));
        if (found) setSelectedRoomType(found);
      }
    },
  });

  const availMutation = useMutation({
    mutationFn: checkAvailability,
    onSuccess: (data) => {
      setAvailabilityResult(data);
    },
    onError: (err) => {
      toast.error(err.message || 'Failed to check availability');
    },
  });

  const priceMutation = useMutation({
    mutationFn: calculatePrice,
    onSuccess: (data) => {
      setPriceResult(data);
    },
  });

  const onSubmit = (values) => {
    if (!isAuthenticated) {
      setShowLoginPrompt(true);
      return;
    }

    if (!selectedRoomType) {
      toast.error('Please select a room type');
      return;
    }

    const availPayload = {
      hotelId: Number(hotelId),
      roomTypeId: selectedRoomType.id,
      checkInDate: values.checkInDate,
      checkOutDate: values.checkOutDate,
      guests: values.numberOfGuests,
      roomsRequested: values.numberOfRooms,
    };

    const pricePayload = {
      roomTypeId: selectedRoomType.id,
      checkInDate: values.checkInDate,
      checkOutDate: values.checkOutDate,
      numberOfRooms: values.numberOfRooms,
    };

    setAvailabilityResult(null);
    setPriceResult(null);
    availMutation.mutate(availPayload);
    priceMutation.mutate(pricePayload);
  };

  const handleBookNow = (formValues) => {
    const nights = calculateNights(checkIn, checkOut);
    const total = priceResult?.totalPrice || selectedRoomType.pricePerNight * numRooms * nights;

    const bookingData = {
      hotelId,
      hotelName: hotel?.name,
      city: hotel?.city,
      country: hotel?.country,
      roomTypeId: selectedRoomType.id,
      roomTypeName: selectedRoomType.name,
      pricePerNight: selectedRoomType.pricePerNight,
      checkInDate: checkIn,
      checkOutDate: checkOut,
      numberOfRooms: numRooms,
      numberOfGuests: watch('numberOfGuests'),
      totalPrice: total,
    };

    sessionStorage.setItem('checkoutData', JSON.stringify(bookingData));
    navigate('/checkout');
  };

  const nights = calculateNights(checkIn, checkOut);

  return (
    <div className="min-h-screen bg-gray-50">
      {showLoginPrompt && <LoginPromptModal onClose={() => setShowLoginPrompt(false)} />}

      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <button
          onClick={() => navigate(-1)}
          className="inline-flex items-center gap-2 text-sm text-gray-500 hover:text-gray-800 mb-6 transition-colors"
        >
          <ArrowLeft size={16} />
          Back to hotel
        </button>

        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">
            {hotelLoading ? (
              <Skeleton className="h-7 w-64 inline-block" />
            ) : (
              hotel?.name || 'Check Availability'
            )}
          </h1>
          <p className="text-gray-500 mt-1">Select a room type and dates to check availability</p>

          {!isAuthenticated && (
            <div className="mt-3 inline-flex items-center gap-2 bg-amber-50 border border-amber-200 text-amber-700 text-sm px-4 py-2 rounded-xl">
              <Lock size={14} />
              <span>
                <Link to="/login" className="font-semibold hover:text-amber-800">Sign in</Link>
                {' '}or{' '}
                <Link to="/register" className="font-semibold hover:text-amber-800">register</Link>
                {' '}to check availability and book
              </span>
            </div>
          )}
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Left: Room type selector + form */}
          <div className="space-y-5">
            {/* Room type selection */}
            <div className="bg-white rounded-2xl p-5 shadow-sm border border-gray-100">
              <h2 className="font-semibold text-gray-900 mb-4">Select Room Type</h2>
              {roomsLoading ? (
                <div className="space-y-3">
                  {[1, 2].map((i) => <Skeleton key={i} className="h-20" />)}
                </div>
              ) : (
                <div className="space-y-3">
                  {roomTypes.map((rt) => (
                    <button
                      key={rt.id}
                      type="button"
                      onClick={() => {
                        setSelectedRoomType(rt);
                        setAvailabilityResult(null);
                        setPriceResult(null);
                      }}
                      className={`w-full text-left p-4 rounded-xl border-2 transition-colors ${
                        selectedRoomType?.id === rt.id
                          ? 'border-amber-500 bg-amber-50'
                          : 'border-gray-200 hover:border-amber-300 hover:bg-amber-50/50'
                      }`}
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <BedDouble size={16} className="text-amber-500" />
                          <span className="font-medium text-gray-900 text-sm">{rt.name}</span>
                        </div>
                        <span className="font-bold text-amber-600 text-sm">
                          {formatPrice(rt.pricePerNight)}/night
                        </span>
                      </div>
                      {rt.description && (
                        <p className="text-xs text-gray-500 mt-1 ml-6 line-clamp-1">{rt.description}</p>
                      )}
                    </button>
                  ))}
                </div>
              )}
            </div>

            {/* Availability form */}
            <form
              onSubmit={handleSubmit(onSubmit)}
              className="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 space-y-4"
            >
              <h2 className="font-semibold text-gray-900">Your Dates</h2>

              <Input
                label="Check-in Date"
                type="date"
                icon={Calendar}
                min={today}
                {...register('checkInDate')}
                error={errors.checkInDate?.message}
              />

              <Input
                label="Check-out Date"
                type="date"
                icon={Calendar}
                min={checkIn || today}
                {...register('checkOutDate')}
                error={errors.checkOutDate?.message}
              />

              <div className="grid grid-cols-2 gap-3">
                <Input
                  label="Rooms"
                  type="number"
                  min={1}
                  max={10}
                  {...register('numberOfRooms')}
                  error={errors.numberOfRooms?.message}
                />
                <Input
                  label="Guests"
                  type="number"
                  icon={Users}
                  min={1}
                  max={20}
                  {...register('numberOfGuests')}
                  error={errors.numberOfGuests?.message}
                />
              </div>

              <Button
                type="submit"
                variant="primary"
                className="w-full"
                loading={availMutation.isPending}
              >
                {isAuthenticated ? 'Check Availability' : 'Check Availability'}
              </Button>
            </form>
          </div>

          {/* Right: Result */}
          <div className="space-y-5">
            {selectedRoomType && (
              <div className="bg-white rounded-2xl p-5 shadow-sm border border-gray-100">
                <h2 className="font-semibold text-gray-900 mb-3">Selected Room</h2>
                <div className="flex items-center gap-2 text-gray-700">
                  <BedDouble size={18} className="text-amber-500" />
                  <span className="font-medium">{selectedRoomType.name}</span>
                </div>
                <p className="text-sm text-gray-500 mt-1">
                  {selectedRoomType.description}
                </p>
                <div className="mt-3 flex items-center justify-between text-sm">
                  <span className="text-gray-500">Price per night</span>
                  <span className="font-semibold text-amber-600">
                    {formatPrice(selectedRoomType.pricePerNight)}
                  </span>
                </div>
                {nights > 0 && numRooms > 0 && (
                  <div className="flex items-center justify-between text-sm mt-1">
                    <span className="text-gray-500">Estimated total ({nights}n × {numRooms} rm)</span>
                    <span className="font-semibold text-gray-900">
                      {formatCurrency(selectedRoomType.pricePerNight * nights * numRooms)}
                    </span>
                  </div>
                )}
              </div>
            )}

            {/* Availability result */}
            {availMutation.isPending && (
              <div className="bg-white rounded-2xl p-5 shadow-sm border border-gray-100">
                <Skeleton className="h-5 w-3/4 mb-3" />
                <Skeleton className="h-4 w-1/2" />
              </div>
            )}

            {availabilityResult && (
              <div
                className={`rounded-2xl p-5 border ${
                  availabilityResult.available !== false
                    ? 'bg-green-50 border-green-200'
                    : 'bg-red-50 border-red-200'
                }`}
              >
                <div className="flex items-center gap-2 mb-2">
                  <CheckCircle
                    size={20}
                    className={
                      availabilityResult.available !== false ? 'text-green-500' : 'text-red-500'
                    }
                  />
                  <h3
                    className={`font-semibold ${
                      availabilityResult.available !== false ? 'text-green-700' : 'text-red-700'
                    }`}
                  >
                    {availabilityResult.available !== false
                      ? 'Rooms Available!'
                      : 'Not Available'}
                  </h3>
                </div>

                {availabilityResult.available !== false ? (
                  <>
                    {availabilityResult.availableRooms !== undefined && (
                      <p className="text-sm text-green-600 mb-3">
                        {availabilityResult.availableRooms} room(s) available for your dates.
                      </p>
                    )}
                    {priceResult && (
                      <div className="bg-white rounded-xl p-4 mb-4 space-y-2 text-sm">
                        <div className="flex justify-between text-gray-600">
                          <span>Price per night</span>
                          <span>{formatCurrency(priceResult.basePricePerNight || priceResult.finalPricePerNight || selectedRoomType?.pricePerNight)}</span>
                        </div>
                        <div className="flex justify-between text-gray-600">
                          <span>{nights} night(s) × {numRooms} room(s)</span>
                          <span>{formatCurrency(priceResult.subtotal || (priceResult.totalPrice * 0.9))}</span>
                        </div>
                        <div className="flex justify-between font-bold text-gray-900 pt-2 border-t border-gray-100 text-base">
                          <span>Total</span>
                          <span className="text-amber-600">{formatCurrency(priceResult.totalPrice)}</span>
                        </div>
                      </div>
                    )}
                    <Button
                      variant="primary"
                      className="w-full"
                      onClick={handleSubmit(handleBookNow)}
                    >
                      Book Now
                    </Button>
                  </>
                ) : (
                  <p className="text-sm text-red-600">
                    {availabilityResult.message ||
                      'No rooms available for your selected dates. Please try different dates.'}
                  </p>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default AvailabilityPage;
