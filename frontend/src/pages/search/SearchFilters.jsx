import { useState, useEffect } from 'react';
import { SlidersHorizontal, X } from 'lucide-react';
import Button from '../../components/ui/Button';

const STAR_OPTIONS = [5, 4, 3, 2, 1];
const AMENITY_OPTIONS = [
  'WiFi',
  'Pool',
  'Parking',
  'Gym',
  'Restaurant',
  'Spa',
  'Air Conditioning',
  'Breakfast',
  'Pet Friendly',
  'Business Center',
];

function SearchFilters({ initialFilters = {}, onApply }) {
  const [minPrice, setMinPrice] = useState(initialFilters.minPrice || '');
  const [maxPrice, setMaxPrice] = useState(initialFilters.maxPrice || '');
  const [selectedStars, setSelectedStars] = useState(
    initialFilters.rating ? [initialFilters.rating] : []
  );
  const [selectedAmenities, setSelectedAmenities] = useState(
    initialFilters.amenities
      ? String(initialFilters.amenities)
          .split(',')
          .filter(Boolean)
      : []
  );
  const [mobileOpen, setMobileOpen] = useState(false);

  useEffect(() => {
    setMinPrice(initialFilters.minPrice || '');
    setMaxPrice(initialFilters.maxPrice || '');
    setSelectedStars(initialFilters.rating ? [String(initialFilters.rating)] : []);
    setSelectedAmenities(
      initialFilters.amenities
        ? String(initialFilters.amenities).split(',').filter(Boolean)
        : []
    );
  }, [JSON.stringify(initialFilters)]);

  const toggleStar = (star) => {
    const s = String(star);
    setSelectedStars((prev) =>
      prev.includes(s) ? prev.filter((x) => x !== s) : [...prev, s]
    );
  };

  const toggleAmenity = (amenity) => {
    setSelectedAmenities((prev) =>
      prev.includes(amenity)
        ? prev.filter((a) => a !== amenity)
        : [...prev, amenity]
    );
  };

  const handleApply = () => {
    onApply({
      minPrice: minPrice || null,
      maxPrice: maxPrice || null,
      rating: selectedStars.length === 1 ? selectedStars[0] : null,
      amenities: selectedAmenities.length > 0 ? selectedAmenities.join(',') : null,
    });
    setMobileOpen(false);
  };

  const handleReset = () => {
    setMinPrice('');
    setMaxPrice('');
    setSelectedStars([]);
    setSelectedAmenities([]);
    onApply({ minPrice: null, maxPrice: null, rating: null, amenities: null });
    setMobileOpen(false);
  };

  const hasFilters =
    minPrice || maxPrice || selectedStars.length > 0 || selectedAmenities.length > 0;

  const FiltersBody = (
    <div className="space-y-6">
      {/* Price range */}
      <div>
        <h4 className="text-sm font-semibold text-gray-800 mb-3">Price per Night</h4>
        <div className="flex items-center gap-2">
          <div className="relative flex-1">
            <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-sm">$</span>
            <input
              type="number"
              value={minPrice}
              onChange={(e) => setMinPrice(e.target.value)}
              placeholder="Min"
              min={0}
              className="w-full pl-6 pr-2 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-amber-500"
            />
          </div>
          <span className="text-gray-400">–</span>
          <div className="relative flex-1">
            <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-sm">$</span>
            <input
              type="number"
              value={maxPrice}
              onChange={(e) => setMaxPrice(e.target.value)}
              placeholder="Max"
              min={0}
              className="w-full pl-6 pr-2 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-amber-500"
            />
          </div>
        </div>
      </div>

      {/* Star rating */}
      <div>
        <h4 className="text-sm font-semibold text-gray-800 mb-3">Star Rating</h4>
        <div className="space-y-2">
          {STAR_OPTIONS.map((star) => (
            <label key={star} className="flex items-center gap-2.5 cursor-pointer">
              <input
                type="checkbox"
                checked={selectedStars.includes(String(star))}
                onChange={() => toggleStar(star)}
                className="w-4 h-4 rounded border-gray-300 text-amber-500 focus:ring-amber-500"
              />
              <span className="text-sm text-gray-700 flex items-center gap-1">
                {'★'.repeat(star)}
                <span className="text-gray-400">({star} stars)</span>
              </span>
            </label>
          ))}
        </div>
      </div>

      {/* Amenities */}
      <div>
        <h4 className="text-sm font-semibold text-gray-800 mb-3">Amenities</h4>
        <div className="space-y-2">
          {AMENITY_OPTIONS.map((amenity) => (
            <label key={amenity} className="flex items-center gap-2.5 cursor-pointer">
              <input
                type="checkbox"
                checked={selectedAmenities.includes(amenity)}
                onChange={() => toggleAmenity(amenity)}
                className="w-4 h-4 rounded border-gray-300 text-amber-500 focus:ring-amber-500"
              />
              <span className="text-sm text-gray-700">{amenity}</span>
            </label>
          ))}
        </div>
      </div>

      {/* Actions */}
      <div className="flex gap-2 pt-2">
        <Button variant="primary" className="flex-1" onClick={handleApply}>
          Apply Filters
        </Button>
        {hasFilters && (
          <Button variant="ghost" onClick={handleReset}>
            <X size={16} />
          </Button>
        )}
      </div>
    </div>
  );

  return (
    <>
      {/* Mobile filter toggle */}
      <div className="lg:hidden mb-4">
        <Button
          variant="secondary"
          onClick={() => setMobileOpen((o) => !o)}
          leftIcon={<SlidersHorizontal size={16} />}
        >
          Filters {hasFilters && `(active)`}
        </Button>
        {mobileOpen && (
          <div className="mt-3 bg-white border border-gray-100 rounded-2xl p-4 shadow-sm">
            {FiltersBody}
          </div>
        )}
      </div>

      {/* Desktop sidebar filters */}
      <div className="hidden lg:block bg-white border border-gray-100 rounded-2xl p-5 shadow-sm sticky top-24">
        <div className="flex items-center justify-between mb-5">
          <h3 className="font-semibold text-gray-900 flex items-center gap-2">
            <SlidersHorizontal size={18} className="text-amber-500" />
            Filters
          </h3>
          {hasFilters && (
            <button
              onClick={handleReset}
              className="text-xs text-red-500 hover:text-red-700 font-medium"
            >
              Reset all
            </button>
          )}
        </div>
        {FiltersBody}
      </div>
    </>
  );
}

export default SearchFilters;
