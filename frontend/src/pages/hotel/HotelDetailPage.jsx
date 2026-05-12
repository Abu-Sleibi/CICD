import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { ArrowLeft, MapPin, Phone, Mail, Globe, BedDouble } from 'lucide-react';
import { getHotelById } from '../../api/hotelsApi';
import { getRoomTypesByHotel } from '../../api/roomTypesApi';
import { QUERY_KEYS } from '../../utils/queryKeys';
import HotelGallery from '../../components/hotel/HotelGallery';
import AmenitiesList from '../../components/hotel/AmenitiesList';
import RoomTypeCard from '../../components/hotel/RoomTypeCard';
import StarRating from '../../components/ui/StarRating';
import Skeleton from '../../components/ui/Skeleton';
import EmptyState from '../../components/ui/EmptyState';

function HotelDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const {
    data: hotel,
    isLoading: hotelLoading,
    isError: hotelError,
    error: hotelErr,
  } = useQuery({
    queryKey: QUERY_KEYS.HOTEL_DETAIL(id),
    queryFn: () => getHotelById(id),
    enabled: !!id,
  });

  const { data: roomTypes = [], isLoading: roomsLoading } = useQuery({
    queryKey: QUERY_KEYS.ROOM_TYPES(id),
    queryFn: () => getRoomTypesByHotel(id),
    enabled: !!id,
  });

  const handleCheckAvailability = (roomType) => {
    navigate(`/hotels/${id}/availability?roomTypeId=${roomType.id}`);
  };

  if (hotelLoading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6">
        <Skeleton className="h-72 sm:h-96 rounded-2xl" />
        <Skeleton className="h-8 w-1/2" />
        <Skeleton className="h-4 w-1/3" />
        <Skeleton className="h-24 w-full" />
      </div>
    );
  }

  if (hotelError) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16 text-center">
        <p className="text-red-500 font-medium text-lg mb-2">Hotel not found</p>
        <p className="text-gray-500 mb-6">{hotelErr?.message}</p>
        <button
          onClick={() => navigate(-1)}
          className="inline-flex items-center gap-2 px-4 py-2 bg-amber-500 text-white rounded-lg hover:bg-amber-600"
        >
          <ArrowLeft size={16} />
          Go Back
        </button>
      </div>
    );
  }

  if (!hotel) return null;

  const {
    name,
    city,
    country,
    address,
    starRating,
    description,
    amenities,
    images,
    imageUrl,
    phone,
    email,
    website,
  } = hotel;

  const galleryImages = images?.length > 0
    ? images
    : imageUrl
    ? [imageUrl]
    : [];

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Back button */}
        <button
          onClick={() => navigate(-1)}
          className="inline-flex items-center gap-2 text-sm text-gray-500 hover:text-gray-800 mb-6 transition-colors"
        >
          <ArrowLeft size={16} />
          Back to results
        </button>

        {/* Gallery */}
        <div className="mb-8">
          <HotelGallery images={galleryImages} hotelName={name} />
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main info */}
          <div className="lg:col-span-2 space-y-6">
            {/* Hotel name & location */}
            <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
              <div className="flex items-start justify-between gap-4 mb-3">
                <h1 className="text-2xl sm:text-3xl font-bold text-gray-900">{name}</h1>
                {starRating && <StarRating rating={starRating} />}
              </div>

              <div className="flex items-center gap-1.5 text-gray-500 mb-4">
                <MapPin size={16} className="flex-shrink-0" />
                <span className="text-sm">
                  {[address, city, country].filter(Boolean).join(', ')}
                </span>
              </div>

              {description && (
                <p className="text-gray-600 leading-relaxed text-sm sm:text-base">{description}</p>
              )}
            </div>

            {/* Amenities */}
            {amenities && amenities.length > 0 && (
              <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Amenities</h2>
                <AmenitiesList amenities={amenities} />
              </div>
            )}

            {/* Room types */}
            <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">Available Room Types</h2>
              {roomsLoading ? (
                <div className="space-y-3">
                  {[1, 2].map((i) => (
                    <Skeleton key={i} className="h-32" />
                  ))}
                </div>
              ) : roomTypes.length === 0 ? (
                <EmptyState
                  icon={BedDouble}
                  title="No room types available"
                  description="This hotel hasn't listed any room types yet."
                />
              ) : (
                <div className="space-y-4">
                  {roomTypes.map((rt, index) => (
                    <div
                      key={rt.id}
                      className="animate-fade-in-up"
                      style={{ animationDelay: `${index * 100}ms` }}
                    >
                      <RoomTypeCard
                        roomType={rt}
                        onCheckAvailability={handleCheckAvailability}
                      />
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Sidebar */}
          <div className="space-y-4">
            {/* Contact info */}
            <div className="bg-white rounded-2xl p-5 shadow-sm border border-gray-100">
              <h3 className="font-semibold text-gray-900 mb-4">Contact</h3>
              <div className="space-y-3">
                {phone && (
                  <div className="flex items-center gap-2 text-sm text-gray-600">
                    <Phone size={15} className="text-amber-500 flex-shrink-0" />
                    <a href={`tel:${phone}`} className="hover:text-amber-600 no-underline">
                      {phone}
                    </a>
                  </div>
                )}
                {email && (
                  <div className="flex items-center gap-2 text-sm text-gray-600">
                    <Mail size={15} className="text-amber-500 flex-shrink-0" />
                    <a href={`mailto:${email}`} className="hover:text-amber-600 no-underline">
                      {email}
                    </a>
                  </div>
                )}
                {website && (
                  <div className="flex items-center gap-2 text-sm text-gray-600">
                    <Globe size={15} className="text-amber-500 flex-shrink-0" />
                    <a
                      href={website}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="hover:text-amber-600 no-underline truncate"
                    >
                      {website.replace(/^https?:\/\//, '')}
                    </a>
                  </div>
                )}
                {!phone && !email && !website && (
                  <p className="text-sm text-gray-400">No contact details provided</p>
                )}
              </div>
            </div>

            {/* Quick book CTA */}
            <div className="bg-gradient-to-br from-amber-500 to-orange-500 rounded-2xl p-5 text-white">
              <h3 className="font-semibold text-lg mb-1">Ready to book?</h3>
              <p className="text-sm text-amber-100 mb-4">
                Select a room type above and check availability for your dates.
              </p>
              <Link
                to={`/hotels/${id}/availability`}
                className="block w-full text-center bg-white text-amber-600 font-semibold py-2.5 rounded-xl hover:bg-amber-50 transition-colors no-underline"
              >
                Check Availability
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default HotelDetailPage;
