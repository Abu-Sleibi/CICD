/**
 * Animated shimmer placeholder for loading states.
 * Pass className to control width/height/border-radius.
 */
function Skeleton({ className = '' }) {
  return (
    <div
      className={`shimmer rounded-lg ${className}`}
      aria-hidden="true"
    />
  );
}

export default Skeleton;
