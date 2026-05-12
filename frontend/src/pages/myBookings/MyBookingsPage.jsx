import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  Calendar,
  BedDouble,
  MapPin,
  Moon,
  Users,
  Receipt,
  ChevronDown,
  ChevronUp,
  XCircle,
  Search,
} from 'lucide-react';
import toast from 'react-hot-toast';
import { getMyBookings, cancelBooking } from '../../api/bookingsApi';
import { QUERY_KEYS } from '../../utils/queryKeys';
import { formatDate, formatCurrency, calculateNights } from '../../utils/dateUtils';
import BookingStatusBadge from '../../components/booking/BookingStatusBadge';
import Button from '../../components/ui/Button';
import Modal from '../../components/ui/Modal';
import Skeleton from '../../components/ui/Skeleton';
import EmptyState from '../../components/ui/EmptyState';

const STATUS_TABS = [
  { key: 'ALL', label: 'All' },
  { key: 'PENDING', label: 'Pending' },
  { key: 'CONFIRMED', label: 'Confirmed' },
  { key: 'COMPLETED', label: 'Completed' },
  { key: 'CANCELLED', label: 'Cancelled' },
];

const cancelSchema = z.object({
  reason: z.string().min(5, 'Please provide a reason (at least 5 characters)'),
});

// ─── Booking Card ─────────────────────────────────────────────────────────────

function BookingCard({ booking, onCancelClick }) {
  const [expanded, setExpanded] = useState(false);
  const nights = calculateNights(booking.checkInDate, booking.checkOutDate);
  const canCancel = booking.status === 'PENDING' || booking.status === 'CONFIRMED';

  return (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden transition-shadow hover:shadow-md">
      {/* Card header */}
      <div className="px-5 py-4 flex items-start justify-between gap-3">
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 flex-wrap">
            <h3 className="font-semibold text-gray-900 truncate">{booking.hotelName}</h3>
            <BookingStatusBadge status={booking.status} />
          </div>
          <div className="flex items-center gap-1 text-sm text-gray-500 mt-0.5">
            <Receipt size={12} />
            <span className="font-mono text-xs">{booking.bookingReference}</span>
          </div>
        </div>
        <div className="flex-shrink-0 text-right">
          <p className="text-lg font-bold text-amber-600">{formatCurrency(booking.totalPrice)}</p>
          <p className="text-xs text-gray-400">{nights} {nights === 1 ? 'night' : 'nights'}</p>
        </div>
      </div>

      {/* Quick info row */}
      <div className="px-5 pb-4 flex flex-wrap items-center gap-4 text-sm text-gray-600">
        <div className="flex items-center gap-1.5">
          <BedDouble size={14} className="text-amber-400" />
          <span>{booking.roomTypeName}</span>
          {booking.numberOfRooms > 1 && (
            <span className="text-gray-400">× {booking.numberOfRooms}</span>
          )}
        </div>
        <div className="flex items-center gap-1.5">
          <Calendar size={14} className="text-amber-400" />
          <span>{formatDate(booking.checkInDate)} → {formatDate(booking.checkOutDate)}</span>
        </div>
        <div className="flex items-center gap-1.5">
          <Users size={14} className="text-amber-400" />
          <span>{booking.numberOfGuests} {booking.numberOfGuests === 1 ? 'guest' : 'guests'}</span>
        </div>
      </div>

      {/* Expanded details */}
      {expanded && (
        <div className="px-5 pb-4 border-t border-gray-50 pt-4 space-y-3 text-sm">
          <div className="grid grid-cols-2 gap-3">
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide mb-0.5">Check-in</p>
              <p className="font-medium text-gray-900">{formatDate(booking.checkInDate)}</p>
            </div>
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide mb-0.5">Check-out</p>
              <p className="font-medium text-gray-900">{formatDate(booking.checkOutDate)}</p>
            </div>
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide mb-0.5">Total Paid</p>
              <p className="font-medium text-gray-900">{formatCurrency(booking.paidAmount ?? 0)}</p>
            </div>
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide mb-0.5">Balance Due</p>
              <p className="font-medium text-gray-900">
                {formatCurrency(Math.max(0, (booking.totalPrice ?? 0) - (booking.paidAmount ?? 0)))}
              </p>
            </div>
          </div>
          {booking.specialRequests && (
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide mb-0.5">Special Requests</p>
              <p className="text-gray-700 italic">{booking.specialRequests}</p>
            </div>
          )}
          {booking.cancellationReason && (
            <div className="bg-red-50 border border-red-100 rounded-lg p-3">
              <p className="text-xs text-red-400 uppercase tracking-wide mb-0.5">Cancellation Reason</p>
              <p className="text-red-700">{booking.cancellationReason}</p>
            </div>
          )}
        </div>
      )}

      {/* Footer actions */}
      <div className="px-5 py-3 bg-gray-50 border-t border-gray-100 flex items-center justify-between gap-2">
        <button
          onClick={() => setExpanded((p) => !p)}
          className="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-800 transition-colors"
        >
          {expanded ? <ChevronUp size={15} /> : <ChevronDown size={15} />}
          {expanded ? 'Less details' : 'More details'}
        </button>

        <div className="flex items-center gap-2">
          {canCancel && (
            <Button
              variant="danger"
              size="sm"
              leftIcon={<XCircle size={14} />}
              onClick={() => onCancelClick(booking)}
            >
              Cancel
            </Button>
          )}
        </div>
      </div>
    </div>
  );
}

// ─── Loading skeletons ────────────────────────────────────────────────────────

function BookingCardSkeleton() {
  return (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 space-y-3">
      <div className="flex justify-between">
        <Skeleton className="h-5 w-40" />
        <Skeleton className="h-5 w-20" />
      </div>
      <Skeleton className="h-4 w-32" />
      <div className="flex gap-4">
        <Skeleton className="h-4 w-28" />
        <Skeleton className="h-4 w-36" />
      </div>
    </div>
  );
}

// ─── Main Page ────────────────────────────────────────────────────────────────

function MyBookingsPage() {
  const queryClient = useQueryClient();
  const [activeTab, setActiveTab] = useState('ALL');
  const [cancelTarget, setCancelTarget] = useState(null);
  const [page, setPage] = useState(0);

  const { data, isLoading, isError } = useQuery({
    queryKey: QUERY_KEYS.MY_BOOKINGS(page),
    queryFn: () => getMyBookings({ page, size: 10 }),
    keepPreviousData: true,
  });

  const cancelMutation = useMutation({
    mutationFn: ({ id, reason }) => cancelBooking(id, reason),
    onSuccess: () => {
      toast.success('Booking cancelled successfully');
      queryClient.invalidateQueries({ queryKey: ['bookings', 'me'] });
      setCancelTarget(null);
    },
    onError: (err) => {
      toast.error(err.message || 'Could not cancel booking. Please try again.');
    },
  });

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({ resolver: zodResolver(cancelSchema) });

  const handleCancelClick = (booking) => {
    reset();
    setCancelTarget(booking);
  };

  const onCancelSubmit = ({ reason }) => {
    if (!cancelTarget) return;
    cancelMutation.mutate({ id: cancelTarget.id, reason });
  };

  const allBookings = data?.content ?? [];

  const filtered =
    activeTab === 'ALL'
      ? allBookings
      : allBookings.filter((b) => b.status === activeTab);

  const totalPages = data?.totalPages ?? 1;

  const tabCounts = STATUS_TABS.reduce((acc, tab) => {
    if (tab.key === 'ALL') {
      acc[tab.key] = allBookings.length;
    } else {
      acc[tab.key] = allBookings.filter((b) => b.status === tab.key).length;
    }
    return acc;
  }, {});

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-3xl mx-auto px-4 sm:px-6 py-10">
        {/* Page header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 flex items-center gap-2">
            <Receipt size={28} className="text-amber-500" />
            My Bookings
          </h1>
          <p className="text-gray-500 mt-1">Manage and track all your hotel reservations</p>
        </div>

        {/* Status tabs */}
        <div className="flex gap-1 bg-white rounded-xl border border-gray-200 p-1 mb-6 overflow-x-auto">
          {STATUS_TABS.map((tab) => (
            <button
              key={tab.key}
              onClick={() => setActiveTab(tab.key)}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-medium whitespace-nowrap transition-colors flex-shrink-0 ${
                activeTab === tab.key
                  ? 'bg-amber-500 text-white shadow-sm'
                  : 'text-gray-500 hover:text-gray-800 hover:bg-gray-50'
              }`}
            >
              {tab.label}
              {tabCounts[tab.key] > 0 && (
                <span
                  className={`text-xs rounded-full px-1.5 py-0.5 ${
                    activeTab === tab.key ? 'bg-amber-400 text-white' : 'bg-gray-100 text-gray-500'
                  }`}
                >
                  {tabCounts[tab.key]}
                </span>
              )}
            </button>
          ))}
        </div>

        {/* Content */}
        {isLoading ? (
          <div className="space-y-4">
            {[1, 2, 3].map((i) => (
              <BookingCardSkeleton key={i} />
            ))}
          </div>
        ) : isError ? (
          <div className="text-center py-16">
            <p className="text-red-500 mb-4">Failed to load bookings.</p>
            <Button variant="secondary" onClick={() => queryClient.invalidateQueries({ queryKey: ['bookings', 'me'] })}>
              Try Again
            </Button>
          </div>
        ) : filtered.length === 0 ? (
          <EmptyState
            icon={Search}
            title={activeTab === 'ALL' ? 'No bookings yet' : `No ${activeTab.toLowerCase()} bookings`}
            description={
              activeTab === 'ALL'
                ? 'Start exploring hotels and make your first booking!'
                : `You don't have any ${activeTab.toLowerCase()} bookings right now.`
            }
            action={
              activeTab === 'ALL' ? (
                <Link to="/search">
                  <Button variant="primary">Browse Hotels</Button>
                </Link>
              ) : (
                <Button variant="ghost" onClick={() => setActiveTab('ALL')}>
                  View All Bookings
                </Button>
              )
            }
          />
        ) : (
          <div className="space-y-4">
            {filtered.map((booking) => (
              <BookingCard
                key={booking.id}
                booking={booking}
                onCancelClick={handleCancelClick}
              />
            ))}
          </div>
        )}

        {/* Pagination */}
        {!isLoading && totalPages > 1 && (
          <div className="flex items-center justify-center gap-3 mt-8">
            <Button
              variant="secondary"
              size="sm"
              disabled={page === 0}
              onClick={() => setPage((p) => p - 1)}
            >
              Previous
            </Button>
            <span className="text-sm text-gray-500">
              Page {page + 1} of {totalPages}
            </span>
            <Button
              variant="secondary"
              size="sm"
              disabled={page + 1 >= totalPages}
              onClick={() => setPage((p) => p + 1)}
            >
              Next
            </Button>
          </div>
        )}
      </div>

      {/* Cancel Modal */}
      <Modal
        isOpen={!!cancelTarget}
        onClose={() => { setCancelTarget(null); reset(); }}
        title="Cancel Booking"
        size="sm"
        footer={
          <>
            <Button
              variant="ghost"
              onClick={() => { setCancelTarget(null); reset(); }}
              disabled={cancelMutation.isPending}
            >
              Keep Booking
            </Button>
            <Button
              variant="danger"
              loading={cancelMutation.isPending}
              onClick={handleSubmit(onCancelSubmit)}
              leftIcon={<XCircle size={16} />}
            >
              Cancel Booking
            </Button>
          </>
        }
      >
        {cancelTarget && (
          <div className="space-y-4">
            {/* Booking summary in modal */}
            <div className="bg-gray-50 rounded-xl p-4 text-sm space-y-1">
              <p className="font-semibold text-gray-900">{cancelTarget.hotelName}</p>
              <p className="text-gray-500 font-mono text-xs">{cancelTarget.bookingReference}</p>
              <div className="flex items-center gap-1.5 text-gray-600 mt-2">
                <Calendar size={13} className="text-amber-400" />
                <span>{formatDate(cancelTarget.checkInDate)} → {formatDate(cancelTarget.checkOutDate)}</span>
              </div>
            </div>

            <div className="bg-amber-50 border border-amber-100 rounded-xl p-3 text-xs text-amber-700">
              Cancellations within 2 days of check-in cannot be processed. This action cannot be undone.
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">
                Reason for cancellation <span className="text-red-500">*</span>
              </label>
              <textarea
                rows={3}
                placeholder="Please tell us why you're cancelling..."
                className={`w-full px-3 py-2.5 border rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 resize-none ${
                  errors.reason ? 'border-red-400' : 'border-gray-300'
                }`}
                {...register('reason')}
              />
              {errors.reason && (
                <p className="text-xs text-red-500 mt-0.5">{errors.reason.message}</p>
              )}
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}

export default MyBookingsPage;
