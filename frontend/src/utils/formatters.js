/**
 * Format a number as a price string (USD)
 * @param {number} n
 * @returns {string}
 */
export const formatPrice = (n) => {
  if (n === null || n === undefined) return '$0';
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  }).format(n);
};

/**
 * Format a booking reference code for display
 * @param {string} ref
 * @returns {string}
 */
export const formatBookingRef = (ref) => {
  if (!ref) return 'N/A';
  const upper = String(ref).toUpperCase();
  // Group into chunks of 4
  return upper.match(/.{1,4}/g)?.join('-') ?? upper;
};

/**
 * Truncate a string to n characters, appending ellipsis if needed
 * @param {string} s
 * @param {number} n
 * @returns {string}
 */
export const truncateText = (s, n = 100) => {
  if (!s) return '';
  if (s.length <= n) return s;
  return s.slice(0, n).trimEnd() + '…';
};
