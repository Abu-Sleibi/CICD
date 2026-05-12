import { useState } from 'react';
import { X, ChevronLeft, ChevronRight } from 'lucide-react';

const PLACEHOLDER = 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&q=80';
const PLACEHOLDER_SMALL = 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?w=400&q=80';

function HotelGallery({ images = [], hotelName = 'Hotel' }) {
  const [lightboxIdx, setLightboxIdx] = useState(null);

  // Normalize images to an array of strings
  const allImages = images.length > 0 ? images : [PLACEHOLDER, PLACEHOLDER_SMALL];

  const main = allImages[0];
  const thumbs = allImages.slice(1, 5);

  const openLightbox = (idx) => setLightboxIdx(idx);
  const closeLightbox = () => setLightboxIdx(null);

  const prevImage = () =>
    setLightboxIdx((i) => (i === 0 ? allImages.length - 1 : i - 1));
  const nextImage = () =>
    setLightboxIdx((i) => (i === allImages.length - 1 ? 0 : i + 1));

  return (
    <>
      {/* Grid */}
      <div className="grid grid-cols-4 grid-rows-2 gap-2 h-72 sm:h-96 rounded-2xl overflow-hidden">
        {/* Main large image (left, spans 2 rows) */}
        <div
          className="col-span-2 row-span-2 cursor-pointer overflow-hidden bg-gray-100"
          onClick={() => openLightbox(0)}
        >
          <img
            src={main}
            alt={`${hotelName} – main`}
            className="w-full h-full object-cover hover:scale-105 transition-transform duration-300"
            onError={(e) => { e.target.src = PLACEHOLDER; }}
          />
        </div>

        {/* Up to 4 smaller images on the right */}
        {Array.from({ length: 4 }).map((_, i) => {
          const src = thumbs[i] || null;
          return (
            <div
              key={i}
              className="cursor-pointer overflow-hidden bg-gray-100 relative"
              onClick={() => src && openLightbox(i + 1)}
            >
              {src ? (
                <>
                  <img
                    src={src}
                    alt={`${hotelName} ${i + 2}`}
                    className="w-full h-full object-cover hover:scale-105 transition-transform duration-300"
                    onError={(e) => { e.target.src = PLACEHOLDER_SMALL; }}
                  />
                  {i === 3 && allImages.length > 5 && (
                    <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
                      <span className="text-white font-semibold text-lg">
                        +{allImages.length - 5}
                      </span>
                    </div>
                  )}
                </>
              ) : (
                <div className="w-full h-full bg-gray-200" />
              )}
            </div>
          );
        })}
      </div>

      {/* Lightbox */}
      {lightboxIdx !== null && (
        <div className="fixed inset-0 z-50 bg-black/90 flex items-center justify-center p-4">
          <button
            onClick={closeLightbox}
            className="absolute top-4 right-4 p-2 rounded-full bg-white/10 hover:bg-white/20 text-white transition-colors"
          >
            <X size={24} />
          </button>

          <button
            onClick={prevImage}
            className="absolute left-4 p-2 rounded-full bg-white/10 hover:bg-white/20 text-white transition-colors"
          >
            <ChevronLeft size={28} />
          </button>

          <img
            src={allImages[lightboxIdx]}
            alt={`${hotelName} ${lightboxIdx + 1}`}
            className="max-h-[80vh] max-w-full object-contain rounded-xl"
            onError={(e) => { e.target.src = PLACEHOLDER; }}
          />

          <button
            onClick={nextImage}
            className="absolute right-4 p-2 rounded-full bg-white/10 hover:bg-white/20 text-white transition-colors"
          >
            <ChevronRight size={28} />
          </button>

          <div className="absolute bottom-4 text-white text-sm">
            {lightboxIdx + 1} / {allImages.length}
          </div>
        </div>
      )}
    </>
  );
}

export default HotelGallery;
