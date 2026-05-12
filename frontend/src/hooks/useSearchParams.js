import { useSearchParams as useRouterSearchParams } from 'react-router-dom';
import { useCallback } from 'react';

/**
 * Convenience hook that reads and writes URL search params.
 * Returns { params, setParam, setParams }
 */
export const useSearchParams = () => {
  const [searchParams, setSearchParams] = useRouterSearchParams();

  /**
   * Get an object of all current params
   */
  const params = Object.fromEntries(searchParams.entries());

  /**
   * Set a single param (preserves all others)
   * @param {string} key
   * @param {string|number|null} value  — null removes the param
   */
  const setParam = useCallback(
    (key, value) => {
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev);
        if (value === null || value === undefined || value === '') {
          next.delete(key);
        } else {
          next.set(key, String(value));
        }
        return next;
      });
    },
    [setSearchParams]
  );

  /**
   * Set multiple params at once (preserves params not mentioned)
   * @param {Record<string, string|number|null>} updates
   */
  const setParams = useCallback(
    (updates) => {
      setSearchParams((prev) => {
        const next = new URLSearchParams(prev);
        Object.entries(updates).forEach(([key, value]) => {
          if (value === null || value === undefined || value === '') {
            next.delete(key);
          } else {
            next.set(key, String(value));
          }
        });
        return next;
      });
    },
    [setSearchParams]
  );

  return { params, setParam, setParams };
};
