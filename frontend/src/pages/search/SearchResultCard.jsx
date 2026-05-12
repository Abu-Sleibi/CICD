import { Link } from 'react-router-dom';
import { MapPin, Users } from 'lucide-react';
import StarRating from '../../components/ui/StarRating';
import { formatPrice } from '../../utils/formatters';

const PLACEHOLDER = 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=600&q=80';

function SearchResultCard({ hotel }) {
  if (!hotel) return null;

  const {
    id,
    name,
    city,
    country,
    address,
    starRating,
    minPrice,
    imageUrl,
    images,
    description,
    amenities,
    reviewScore,
    reviewCount,
  } = hotel;

  const imgSrc = imageUrl || images?.[0] || PLACEHOLDER;

  return (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm hover:shadow-md transition-shadow overflow-hidden flex flex-col sm:flex-row">
      {/* Image */}
      <div className="sm:w-56 lg:w-64 flex-shrink-0 h-48 sm:h-auto overflow-hidden bg-gray-100">
        <img
          src={imgSrc}
          alt={name}
          className="w-full h-full object-cover hover:scale-105 transition-transform duration-300"
          onError={(e) => { e.target.src = PLACEHOLDER; }}
        />
      </div>

      {/* Content */}
      <div className="flex flex-col flex-1 p-5">
        <div className="flex items-start justify-between gap-3 mb-2">
          <div className="flex-1 min-w-0">
            <h3 className="font-semibold text-gray-900 text-lg mb-1 line-clamp-1">{name}</h3>
            <div className="flex items-center gap-1.5 text-sm text-gray-500 mb-2">
              <MapPin size={14} className="flex-shrink-0" />
              <span className="line-clamp-1">
                {[address, city, country].filter(Boolean).join(', ')}
              </span>
            </div>
          </div>

          {/* Review score */}
          {reviewScore && (
            <div className="flex-shrink-0 flex flex-col items-end">
              <div className="bg-amber-500 text-white text-sm font-bold px-2.5 py-1 rounded-lg">
                {Number(reviewScore).toFixed(1)}
              </div>
              {reviewCount && (
                <span className="text-xs text-gray-400 mt-1">{reviewCount} reviews</span>
              )}
            </div>
          )}
        </div>

        {/* Star rating */}
        {starRating && (
          <StarRating rating={starRating} size={14} className="mb-2" />
        )}

        {/* Description */}
        {description && (
          <p className="text-sm text-gray-500 mb-3 line-clamp-2">{description}</p>
        )}

        {/* Amenities chips */}
        {amenities && amenities.length > 0 && (
          <div className="flex flex-wrap gap-1 mb-3">
            {amenities.slice(0, 4).map((a, i) => (
              <span
                key={i}
                className="px-2 py-0.5 bg-gray-100 text-gray-600 text-xs rounded-full"
              >
                {a}
              </span>
            ))}
            {amenities.length > 4 && (
              <span className="px-2 py-0.5 bg-gray-100 text-gray-500 text-xs rounded-full">
                +{amenities.length - 4} more
              </span>
            )}
          </div>
        )}

        {/* Price + CTA */}
        <div className="mt-auto flex items-center justify-between pt-3 border-t border-gray-50">
          <div>
            {minPrice ? (
              <>
                <span className="text-xs text-gray-400">From</span>
                <p className="text-xl font-bold text-amber-500">
                  {formatPrice(minPrice)}
                  <span className="text-xs text-gray-400 font-normal ml-1">/ night</span>
                </p>
              </>
            ) : (
              <p className="text-sm text-gray-400">Price on request</p>
            )}
          </div>

          <Link
            to={`/hotels/${id}`}
            className="inline-flex items-center gap-1.5 px-5 py-2.5 bg-amber-500 hover:bg-amber-600 text-white text-sm font-medium rounded-xl transition-colors no-underline"
          >
            View Hotel
          </Link>
        </div>
      </div>
    </div>
  );
}

export default SearchResultCard;
