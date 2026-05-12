import Badge from '../ui/Badge';

/**
 * Delegates to Badge with booking status mapping.
 * Handles both string statuses and uppercased enum values.
 */
function BookingStatusBadge({ status, className = '' }) {
  return <Badge status={status} className={className} />;
}

export default BookingStatusBadge;
