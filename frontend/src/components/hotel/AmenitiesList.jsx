import {
  Wifi,
  Waves,
  Car,
  Dumbbell,
  UtensilsCrossed,
  Sparkles,
  Wind,
  Coffee,
  Tv,
  ShowerHead,
  PocketKnife,
  Globe,
  Clock,
  Baby,
  PawPrint,
  Briefcase,
  CheckCircle,
} from 'lucide-react';

const AMENITY_MAP = {
  wifi: { icon: Wifi, label: 'Free WiFi' },
  internet: { icon: Wifi, label: 'Internet' },
  pool: { icon: Waves, label: 'Swimming Pool' },
  'swimming pool': { icon: Waves, label: 'Swimming Pool' },
  parking: { icon: Car, label: 'Free Parking' },
  gym: { icon: Dumbbell, label: 'Fitness Center' },
  fitness: { icon: Dumbbell, label: 'Fitness Center' },
  restaurant: { icon: UtensilsCrossed, label: 'Restaurant' },
  dining: { icon: UtensilsCrossed, label: 'Dining' },
  spa: { icon: Sparkles, label: 'Spa' },
  'air conditioning': { icon: Wind, label: 'Air Conditioning' },
  ac: { icon: Wind, label: 'Air Conditioning' },
  'air-conditioning': { icon: Wind, label: 'Air Conditioning' },
  breakfast: { icon: Coffee, label: 'Breakfast' },
  tv: { icon: Tv, label: 'Flat-screen TV' },
  television: { icon: Tv, label: 'Television' },
  shower: { icon: ShowerHead, label: 'Hot Shower' },
  concierge: { icon: PocketKnife, label: 'Concierge' },
  tours: { icon: Globe, label: 'Tours' },
  '24/7': { icon: Clock, label: '24/7 Service' },
  'front desk': { icon: Clock, label: '24/7 Front Desk' },
  kids: { icon: Baby, label: "Kids' Club" },
  pets: { icon: PawPrint, label: 'Pet Friendly' },
  'pet friendly': { icon: PawPrint, label: 'Pet Friendly' },
  business: { icon: Briefcase, label: 'Business Center' },
};

function getAmenityInfo(amenity) {
  const key = amenity.toLowerCase();
  for (const [mapKey, val] of Object.entries(AMENITY_MAP)) {
    if (key.includes(mapKey)) return val;
  }
  return { icon: CheckCircle, label: amenity };
}

/**
 * Renders a list of amenity chips with Lucide icons
 * @param {{ amenities: string[], compact?: boolean }} props
 */
function AmenitiesList({ amenities = [], compact = false }) {
  if (!amenities || amenities.length === 0) {
    return <p className="text-sm text-gray-400">No amenities listed</p>;
  }

  return (
    <div className={`flex flex-wrap gap-2 ${compact ? '' : 'mt-1'}`}>
      {amenities.map((amenity, idx) => {
        const { icon: Icon, label } = getAmenityInfo(amenity);
        return (
          <div
            key={idx}
            className="flex items-center gap-1.5 px-3 py-1.5 bg-amber-50 text-amber-700 rounded-full text-sm font-medium border border-amber-100"
          >
            <Icon size={14} />
            <span>{label}</span>
          </div>
        );
      })}
    </div>
  );
}

export default AmenitiesList;
