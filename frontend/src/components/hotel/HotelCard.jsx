import { Link } from 'react-router-dom';
import { MapPin } from 'lucide-react';
import StarRating from '../ui/StarRating';
import { formatPrice } from '../../utils/formatters';

const PLACEHOLDER_IMAGE = 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=600&q=80';

function HotelCard({ hotel }) {
  if (!hotel) return null;

  const {
    id,
    name,
    city,
    country,
    starRating,
    minPrice,
    imageUrl,
    images,
    description,
  } = hotel;

  const imgSrc = imageUrl || images?.[0] || PLACEHOLDER_IMAGE;

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-shadow group">
      {/* Image */}
      <div className="relative h-48 overflow-hidden bg-gray-100">
        <img
          src={imgSrc}
          alt={name}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
          onError={(e) => {
            e.target.src = PLACEHOLDER_IMAGE;
          }}
        />
        {starRating && (
          <div className="absolute top-3 left-3 bg-white/90 backdrop-blur-sm rounded-lg px-2 py-1">
            <StarRating rating={starRating} size={12} />
          </div>
        )}
      </div>

      {/* Content */}
      <div className="p-4">
        <h3 className="font-semibold text-gray-900 text-base mb-1 line-clamp-1">{name}</h3>
        <div className="flex items-center gap-1 text-gray-500 text-sm mb-3">
          <MapPin size={13} className="flex-shrink-0" />
          <span className="line-clamp-1">
            {[city, country].filter(Boolean).join(', ')}
          </span>
        </div>

        {description && (
          <p className="text-xs text-gray-400 mb-3 line-clamp-2">{description}</p>
        )}

        <div className="flex items-end justify-between">
          <div>
            {minPrice ? (
              <>
                <span className="text-xs text-gray-400">From</span>
                <p className="text-lg font-bold text-amber-500">
                  {formatPrice(minPrice)}
                  <span className="text-xs text-gray-400 font-normal">/night</span>
                </p>
              </>
            ) : (
              <p className="text-sm text-gray-400">Price on request</p>
            )}
          </div>

          <Link
            to={`/hotels/${id}`}
            className="inline-flex items-center px-4 py-2 bg-amber-500 hover:bg-amber-600 text-white text-sm font-medium rounded-lg transition-colors no-underline"
          >
            View Details
          </Link>
        </div>
      </div>
    </div>
  );
}

export default HotelCard;
