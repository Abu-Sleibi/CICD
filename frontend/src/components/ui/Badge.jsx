const statusConfig = {
  CONFIRMED: {
    label: 'Confirmed',
    className: 'bg-green-100 text-green-700 border-green-200',
  },
  confirmed: {
    label: 'Confirmed',
    className: 'bg-green-100 text-green-700 border-green-200',
  },
  PENDING: {
    label: 'Pending',
    className: 'bg-yellow-100 text-yellow-700 border-yellow-200',
  },
  pending: {
    label: 'Pending',
    className: 'bg-yellow-100 text-yellow-700 border-yellow-200',
  },
  CANCELLED: {
    label: 'Cancelled',
    className: 'bg-red-100 text-red-700 border-red-200',
  },
  cancelled: {
    label: 'Cancelled',
    className: 'bg-red-100 text-red-700 border-red-200',
  },
  COMPLETED: {
    label: 'Completed',
    className: 'bg-blue-100 text-blue-700 border-blue-200',
  },
  completed: {
    label: 'Completed',
    className: 'bg-blue-100 text-blue-700 border-blue-200',
  },
  CHECKED_IN: {
    label: 'Checked In',
    className: 'bg-purple-100 text-purple-700 border-purple-200',
  },
  CHECKED_OUT: {
    label: 'Checked Out',
    className: 'bg-gray-100 text-gray-700 border-gray-200',
  },
};

/**
 * Status-based colored badge
 * @param {{ status: string, label?: string, className?: string }} props
 */
function Badge({ status, label, className = '' }) {
  const config = statusConfig[status] || {
    label: label || status || 'Unknown',
    className: 'bg-gray-100 text-gray-700 border-gray-200',
  };

  return (
    <span
      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border ${config.className} ${className}`}
    >
      {label || config.label}
    </span>
  );
}

export default Badge;
