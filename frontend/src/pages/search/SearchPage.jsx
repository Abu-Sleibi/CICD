import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useLocation } from 'react-router-dom';
import { Search, ChevronLeft, ChevronRight } from 'lucide-react';
import { searchHotels } from '../../api/hotelsApi';
import { QUERY_KEYS } from '../../utils/queryKeys';
import { useSearchParams } from '../../hooks/useSearchParams';
import SearchFilters from './SearchFilters';
import SearchResultCard from './SearchResultCard';
import Skeleton from '../../components/ui/Skeleton';
import EmptyState from '../../components/ui/EmptyState';

const PAGE_SIZE = 9;

function SkeletonCard() {
  return (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden flex flex-col sm:flex-row">
      <Skeleton className="sm:w-56 lg:w-64 flex-shrink-0 h-48 sm:h-auto rounded-none" />
      <div className="flex-1 p-5 space-y-3">
        <Skeleton className="h-5 w-3/4" />
        <Skeleton className="h-4 w-1/2" />
        <Skeleton className="h-3 w-full" />
        <Skeleton className="h-3 w-5/6" />
        <div className="flex gap-2 mt-2">
          <Skeleton className="h-5 w-16 rounded-full" />
          <Skeleton className="h-5 w-16 rounded-full" />
          <Skeleton className="h-5 w-12 rounded-full" />
        </div>
        <div className="flex justify-between items-center pt-3">
          <Skeleton className="h-6 w-24" />
          <Skeleton className="h-9 w-28 rounded-xl" />
        </div>
      </div>
    </div>
  );
}

function SearchPage() {
  const { params, setParams } = useSearchParams();
  const [page, setPage] = useState(Number(params.page) || 1);

  // Sync page from URL params
  useEffect(() => {
    setPage(Number(params.page) || 1);
  }, [params.page]);

  const queryParams = {
    city: params.city || undefined,
    checkIn: params.checkIn || undefined,
    checkOut: params.checkOut || undefined,
    guests: params.guests || undefined,
    minPrice: params.minPrice || undefined,
    maxPrice: params.maxPrice || undefined,
    rating: params.rating || undefined,
    amenities: params.amenities || undefined,
    page: page - 1,
    size: PAGE_SIZE,
  };

  const { data, isLoading, isError, error } = useQuery({
    queryKey: QUERY_KEYS.HOTEL_SEARCH(queryParams),
    queryFn: () => searchHotels(queryParams),
    placeholderData: (prev) => prev,
  });

  // Handle both array response and paginated object response
  const hotels = Array.isArray(data) ? data : data?.content ?? data?.hotels ?? [];
  const totalPages = data?.totalPages ?? Math.ceil((data?.totalElements ?? hotels.length) / PAGE_SIZE);
  const totalElements = data?.totalElements ?? hotels.length;

  const handleFilterApply = (filters) => {
    setParams({ ...filters, page: null });
    setPage(1);
  };

  const goToPage = (p) => {
    setParams({ page: p });
    setPage(p);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const location = useLocation();
  const searchTitle = params.city ? `Hotels in "${params.city}"` : 'All Hotels';

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-100 py-6">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">{searchTitle}</h1>
              {!isLoading && (
                <p className="text-sm text-gray-500 mt-0.5">
                  {isError ? 'Error loading results' : `${totalElements} hotels found`}
                </p>
              )}
            </div>
            {/* Active param chips */}
            <div className="flex flex-wrap gap-2">
              {params.checkIn && (
                <span className="px-3 py-1 bg-amber-50 text-amber-700 text-xs font-medium rounded-full border border-amber-100">
                  Check-in: {params.checkIn}
                </span>
              )}
              {params.checkOut && (
                <span className="px-3 py-1 bg-amber-50 text-amber-700 text-xs font-medium rounded-full border border-amber-100">
                  Check-out: {params.checkOut}
                </span>
              )}
              {params.guests && (
                <span className="px-3 py-1 bg-amber-50 text-amber-700 text-xs font-medium rounded-full border border-amber-100">
                  Guests: {params.guests}
                </span>
              )}
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex gap-7">
          {/* Sidebar filters (desktop) */}
          <div className="w-64 flex-shrink-0">
            <SearchFilters
              initialFilters={params}
              onApply={handleFilterApply}
            />
          </div>

          {/* Results */}
          <div className="flex-1 min-w-0">
            {/* Mobile filters */}
            <div className="lg:hidden mb-4">
              <SearchFilters
                initialFilters={params}
                onApply={handleFilterApply}
              />
            </div>

            {isLoading ? (
              <div className="space-y-4">
                {Array.from({ length: 6 }).map((_, i) => (
                  <SkeletonCard key={i} />
                ))}
              </div>
            ) : isError ? (
              <div className="bg-white rounded-2xl p-8 text-center">
                <p className="text-red-500 font-medium mb-2">Failed to load hotels</p>
                <p className="text-sm text-gray-500">{error?.message}</p>
              </div>
            ) : hotels.length === 0 ? (
              <EmptyState
                icon={Search}
                title="No hotels found"
                description="Try adjusting your search filters or searching for a different destination."
                actionLabel="Clear Filters"
                onAction={() => handleFilterApply({})}
              />
            ) : (
              <>
                <div className="space-y-4">
                  {hotels.map((hotel, index) => (
                    <div
                      key={hotel.id}
                      className="animate-fade-in-up"
                      style={{ animationDelay: `${index * 80}ms` }}
                    >
                      <SearchResultCard hotel={hotel} />
                    </div>
                  ))}
                </div>

                {/* Pagination */}
                {totalPages > 1 && (
                  <div className="flex items-center justify-center gap-2 mt-8">
                    <button
                      onClick={() => goToPage(page - 1)}
                      disabled={page <= 1}
                      className="p-2 rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                    >
                      <ChevronLeft size={18} />
                    </button>

                    {Array.from({ length: totalPages }, (_, i) => i + 1)
                      .filter((p) => p === 1 || p === totalPages || Math.abs(p - page) <= 1)
                      .reduce((acc, p, idx, arr) => {
                        if (idx > 0 && arr[idx - 1] !== p - 1) {
                          acc.push('...');
                        }
                        acc.push(p);
                        return acc;
                      }, [])
                      .map((item, i) =>
                        item === '...' ? (
                          <span key={`dots-${i}`} className="px-2 text-gray-400">
                            ...
                          </span>
                        ) : (
                          <button
                            key={item}
                            onClick={() => goToPage(item)}
                            className={`w-9 h-9 rounded-lg text-sm font-medium transition-colors ${
                              item === page
                                ? 'bg-amber-500 text-white'
                                : 'border border-gray-200 text-gray-600 hover:bg-gray-50'
                            }`}
                          >
                            {item}
                          </button>
                        )
                      )}

                    <button
                      onClick={() => goToPage(page + 1)}
                      disabled={page >= totalPages}
                      className="p-2 rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
                    >
                      <ChevronRight size={18} />
                    </button>
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default SearchPage;
