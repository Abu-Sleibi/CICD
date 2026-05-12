import { Users, BedDouble } from 'lucide-react';
import { formatPrice } from '../../utils/formatters';
import Button from '../ui/Button';

function RoomTypeCard({ roomType, onCheckAvailability }) {
  if (!roomType) return null;

  const { name, description, pricePerNight, capacity, totalRooms, amenities } = roomType;

  return (
    <div className="bg-white border border-gray-100 rounded-2xl p-5 shadow-sm hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between gap-4">
        <div className="flex-1 min-w-0">
          {/* Room name */}
          <div className="flex items-center gap-2 mb-2">
            <BedDouble size={18} className="text-amber-500 flex-shrink-0" />
            <h3 className="font-semibold text-gray-900 text-base truncate">{name}</h3>
          </div>

          {/* Capacity */}
          <div className="flex items-center gap-1.5 text-sm text-gray-500 mb-3">
            <Users size={14} />
            <span>Up to {capacity} {capacity === 1 ? 'guest' : 'guests'}</span>
            {totalRooms && (
              <>
                <span className="text-gray-300">·</span>
                <span>{totalRooms} rooms</span>
              </>
            )}
          </div>

          {/* Description */}
          {description && (
            <p className="text-sm text-gray-500 mb-3 line-clamp-2">{description}</p>
          )}

          {/* Amenities */}
          {amenities && amenities.length > 0 && (
            <div className="flex flex-wrap gap-1">
              {amenities.slice(0, 4).map((a, i) => (
                <span
                  key={i}
                  className="px-2 py-0.5 bg-gray-100 text-gray-600 text-xs rounded-full"
                >
                  {a}
                </span>
              ))}
              {amenities.length > 4 && (
                <span className="px-2 py-0.5 bg-gray-100 text-gray-600 text-xs rounded-full">
                  +{amenities.length - 4}
                </span>
              )}
            </div>
          )}
        </div>

        {/* Price + CTA */}
        <div className="flex-shrink-0 flex flex-col items-end gap-3">
          <div className="text-right">
            <p className="text-2xl font-bold text-amber-500">{formatPrice(pricePerNight)}</p>
            <p className="text-xs text-gray-400">per night</p>
          </div>
          <Button
            variant="primary"
            size="sm"
            onClick={() => onCheckAvailability && onCheckAvailability(roomType)}
          >
            Check Availability
          </Button>
        </div>
      </div>
    </div>
  );
}

export default RoomTypeCard;
