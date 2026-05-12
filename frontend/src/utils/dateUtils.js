import {
  format,
  differenceInCalendarDays,
  isFuture,
  parseISO,
  isValid,
} from 'date-fns';

/**
 * Format a date to a readable string
 * @param {Date|string} date
 * @param {string} formatStr
 * @returns {string}
 */
export const formatDate = (date, formatStr = 'MMM dd, yyyy') => {
  if (!date) return '';
  try {
    const d = typeof date === 'string' ? parseISO(date) : date;
    if (!isValid(d)) return '';
    return format(d, formatStr);
  } catch {
    return '';
  }
};

/**
 * Format a date range to a readable string
 * @param {Date|string} start
 * @param {Date|string} end
 * @returns {string}
 */
export const formatDateRange = (start, end) => {
  const s = formatDate(start, 'MMM dd');
  const e = formatDate(end, 'MMM dd, yyyy');
  return `${s} – ${e}`;
};

/**
 * Calculate number of nights between two dates
 * @param {Date|string} start
 * @param {Date|string} end
 * @returns {number}
 */
export const calculateNights = (start, end) => {
  if (!start || !end) return 0;
  try {
    const s = typeof start === 'string' ? parseISO(start) : start;
    const e = typeof end === 'string' ? parseISO(end) : end;
    const diff = differenceInCalendarDays(e, s);
    return Math.max(0, diff);
  } catch {
    return 0;
  }
};

/**
 * Check if a date is in the future
 * @param {Date|string} date
 * @returns {boolean}
 */
export const isDateInFuture = (date) => {
  if (!date) return false;
  try {
    const d = typeof date === 'string' ? parseISO(date) : date;
    return isFuture(d);
  } catch {
    return false;
  }
};

/**
 * Format a number as USD currency
 * @param {number} amount
 * @returns {string}
 */
export const formatCurrency = (amount) => {
  if (amount === null || amount === undefined) return '$0.00';
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 2,
  }).format(amount);
};
