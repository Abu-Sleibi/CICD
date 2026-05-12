import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MapPin, Calendar, Users, Search } from 'lucide-react';
import { format, addDays } from 'date-fns';

function HeroSection() {
  const navigate = useNavigate();
  const today = format(new Date(), 'yyyy-MM-dd');
  const tomorrow = format(addDays(new Date(), 1), 'yyyy-MM-dd');

  const [city, setCity] = useState('');
  const [checkIn, setCheckIn] = useState(today);
  const [checkOut, setCheckOut] = useState(tomorrow);
  const [guests, setGuests] = useState(2);

  const handleSearch = (e) => {
    e.preventDefault();
    const params = new URLSearchParams();
    if (city) params.set('city', city);
    if (checkIn) params.set('checkIn', checkIn);
    if (checkOut) params.set('checkOut', checkOut);
    if (guests) params.set('guests', String(guests));
    navigate(`/search?${params.toString()}`);
  };

  return (
    <section className="relative min-h-[85vh] flex items-center justify-center overflow-hidden">
      {/* Background */}
      <div className="absolute inset-0 bg-gradient-to-br from-gray-900 via-gray-800 to-amber-900">
        <img
          src="https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?w=1920&q=80"
          alt="Luxury hotel"
          className="absolute inset-0 w-full h-full object-cover opacity-30 mix-blend-luminosity"
          loading="eager"
        />
      </div>

      {/* Overlay gradient */}
      <div className="absolute inset-0 bg-gradient-to-t from-gray-900/80 via-transparent to-transparent" />

      {/* Content */}
      <div className="relative z-10 w-full max-w-5xl mx-auto px-4 sm:px-6 text-center">
        {/* Badge */}
        <div className="inline-flex items-center gap-2 bg-amber-500/20 border border-amber-500/30 text-amber-400 text-sm font-medium px-4 py-1.5 rounded-full mb-6 animate-fade-in">
          <span className="w-2 h-2 rounded-full bg-amber-400 animate-pulse" />
          Over 10,000+ hotels worldwide
        </div>

        {/* Brand name */}
        <div className="mb-4 animate-slide-up" style={{ animationDelay: '80ms' }}>
          <div style={{ fontFamily: "'Playfair Display', Georgia, serif", fontWeight: 700, letterSpacing: '0.2em', color: '#C9A84C', fontSize: 'clamp(2.5rem, 8vw, 4.5rem)', lineHeight: 1 }}>
            VELOUR
          </div>
          <p style={{ fontStyle: 'italic', letterSpacing: '0.08em', color: '#d1d5db', fontSize: '1rem', marginTop: '6px' }}>
            Redefine Your Stay
          </p>
        </div>

        {/* Headline */}
        <h1
          className="text-3xl sm:text-4xl md:text-5xl font-extrabold text-white leading-tight mb-5 animate-slide-up"
          style={{ animationDelay: '180ms' }}
        >
          Find Your{' '}
          <span className="text-transparent bg-clip-text bg-gradient-to-r from-amber-400 to-orange-400">
            Perfect Stay
          </span>
        </h1>

        {/* Subtext */}
        <p
          className="text-lg sm:text-xl text-gray-300 max-w-2xl mx-auto mb-10 animate-slide-up"
          style={{ animationDelay: '200ms' }}
        >
          Discover thousands of hotels across the globe. Compare prices, read reviews, and book in minutes.
        </p>

        {/* Search card */}
        <div
          className="bg-white rounded-2xl shadow-2xl p-5 sm:p-6 max-w-4xl mx-auto animate-fade-in-up"
          style={{ animationDelay: '300ms' }}
        >
          <form onSubmit={handleSearch}>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 mb-4">
              {/* City */}
              <div className="relative">
                <label className="block text-xs font-medium text-gray-500 mb-1 text-left">
                  Destination
                </label>
                <div className="relative">
                  <MapPin size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" />
                  <input
                    type="text"
                    value={city}
                    onChange={(e) => setCity(e.target.value)}
                    placeholder="Where to?"
                    className="w-full pl-9 pr-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500"
                  />
                </div>
              </div>

              {/* Check-in */}
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1 text-left">
                  Check-in
                </label>
                <div className="relative">
                  <Calendar size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" />
                  <input
                    type="date"
                    value={checkIn}
                    min={today}
                    onChange={(e) => setCheckIn(e.target.value)}
                    className="w-full pl-9 pr-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500"
                  />
                </div>
              </div>

              {/* Check-out */}
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1 text-left">
                  Check-out
                </label>
                <div className="relative">
                  <Calendar size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" />
                  <input
                    type="date"
                    value={checkOut}
                    min={checkIn || today}
                    onChange={(e) => setCheckOut(e.target.value)}
                    className="w-full pl-9 pr-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500"
                  />
                </div>
              </div>

              {/* Guests */}
              <div>
                <label className="block text-xs font-medium text-gray-500 mb-1 text-left">
                  Guests
                </label>
                <div className="relative">
                  <Users size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" />
                  <input
                    type="number"
                    value={guests}
                    min={1}
                    max={20}
                    onChange={(e) => setGuests(Number(e.target.value))}
                    className="w-full pl-9 pr-3 py-2.5 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500"
                  />
                </div>
              </div>
            </div>

            {/* Search button */}
            <button
              type="submit"
              className="w-full flex items-center justify-center gap-2 bg-amber-500 hover:bg-amber-600 active:bg-amber-700 text-white font-semibold py-3 px-6 rounded-xl transition-colors text-base"
            >
              <Search size={18} />
              Search Hotels
            </button>
          </form>
        </div>

        {/* Trust badges */}
        <div className="flex flex-wrap justify-center gap-6 mt-8 text-gray-400 text-sm">
          <div className="flex items-center gap-2">
            <span className="text-green-400">✓</span>
            No booking fees
          </div>
          <div className="flex items-center gap-2">
            <span className="text-green-400">✓</span>
            Free cancellation on most hotels
          </div>
          <div className="flex items-center gap-2">
            <span className="text-green-400">✓</span>
            Best price guarantee
          </div>
        </div>
      </div>
    </section>
  );
}

export default HeroSection;
