import { Star } from 'lucide-react';

/**
 * Renders N filled amber stars out of 5 (always out of 5 total)
 * @param {{ rating: number, size?: number, className?: string }} props
 */
function StarRating({ rating = 0, size = 16, className = '' }) {
  const total = 5;
  const filled = Math.min(Math.max(Math.round(rating), 0), total);

  return (
    <div className={`flex items-center gap-0.5 ${className}`} aria-label={`${filled} out of ${total} stars`}>
      {Array.from({ length: total }).map((_, i) => (
        <Star
          key={i}
          size={size}
          className={i < filled ? 'text-amber-400 fill-amber-400' : 'text-gray-200 fill-gray-200'}
        />
      ))}
    </div>
  );
}

export default StarRating;
