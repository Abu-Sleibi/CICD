import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  CalendarCheck,
  ChevronDown,
  ChevronUp,
  CheckCircle,
  XCircle,
  Clock,
  UserX,
  Receipt,
  BedDouble,
  Users,
  Calendar,
  Moon,
  Search,
  Filter,
} from 'lucide-react';
import toast from 'react-hot-toast';
import {
  getHotelBookings,
  searchBookings,
  confirmBooking,
  cancelBooking,
  completeBooking,
  markNoShow,
} from '../../api/bookingsApi';
import { getManagerHotels } from '../../api/hotelsApi';
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
  { key: 'NO_SHOW', label: 'No-Show' },
];

const cancelSchema = z.object({
  reason: z.string().min(5, 'Please provide a reason (at least 5 characters)'),
});

// ─── Booking Row Card ─────────────────────────────────────────────────────────

function BookingCard({ booking, onAction }) {
  const [expanded, setExpanded] = useState(false);
  const nights = calculateNights(booking.checkInDate, booking.checkOutDate);
  const { status } = booking;

  const canConfirm = status === 'PENDING';
  const canCancel = status === 'PENDING' || status === 'CONFIRMED';
  const canComplete = status === 'CONFIRMED';
  const canNoShow = status === 'CONFIRMED';

  return (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden transition-shadow hover:shadow-md">
      {/* Header */}
      <div className="px-5 py-4 flex items-start justify-between gap-3">
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 flex-wrap">
            <h3 className="font-semibold text-gray-900 truncate">
              {booking.guestName || booking.guestEmail || 'Guest'}
            </h3>
            <BookingStatusBadge status={status} />
          </div>
          <div className="flex items-center gap-3 text-xs text-gray-500 mt-0.5 flex-wrap">
            <span className="font-mono">{booking.bookingReference}</span>
            {booking.hotelName && (
              <span className="text-gray-400">· {booking.hotelName}</span>
            )}
          </div>
        </div>
        <div className="flex-shrink-0 text-right">
          <p className="text-lg font-bold text-amber-600">{formatCurrency(booking.totalPrice)}</p>
          <p className="text-xs text-gray-400">{nights} {nights === 1 ? 'night' : 'nights'}</p>
        </div>
      </div>

      {/* Quick info */}
      <div className="px-5 pb-4 flex flex-wrap gap-4 text-sm text-gray-600">
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
              <p className="text-xs text-gray-400 uppercase tracking-wide mb-0.5">Guest Email</p>
              <p className="text-gray-900">{booking.guestEmail || '—'}</p>
            </div>
            <div>
              <p className="text-xs text-gray-400 uppercase tracking-wide mb-0.5">Total Paid</p>
              <p className="text-gray-900">{formatCurrency(booking.paidAmount ?? 0)}</p>
            </div>
            {booking.specialRequests && (
              <div className="col-span-2">
                <p className="text-xs text-gray-400 uppercase tracking-wide mb-0.5">Special Requests</p>
                <p className="text-gray-700 italic">{booking.specialRequests}</p>
              </div>
            )}
            {booking.cancellationReason && (
              <div className="col-span-2 bg-red-50 border border-red-100 rounded-lg p-3">
                <p className="text-xs text-red-400 uppercase tracking-wide mb-0.5">Cancellation Reason</p>
                <p className="text-red-700">{booking.cancellationReason}</p>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Footer */}
      <div className="px-5 py-3 bg-gray-50 border-t border-gray-100 flex items-center justify-between gap-2 flex-wrap">
        <button
          onClick={() => setExpanded((p) => !p)}
          className="flex items-center gap-1 text-sm text-gray-500 hover:text-gray-800 transition-colors"
        >
          {expanded ? <ChevronUp size={15} /> : <ChevronDown size={15} />}
          {expanded ? 'Less' : 'More'} details
        </button>

        <div className="flex items-center gap-1.5 flex-wrap">
          {canConfirm && (
            <Button
              variant="primary"
              size="sm"
              leftIcon={<CheckCircle size={13} />}
              onClick={() => onAction('confirm', booking)}
            >
              Confirm
            </Button>
          )}
          {canComplete && (
            <Button
              variant="secondary"
              size="sm"
              leftIcon={<CheckCircle size={13} />}
              onClick={() => onAction('complete', booking)}
            >
              Complete
            </Button>
          )}
          {canNoShow && (
            <Button
              variant="ghost"
              size="sm"
              leftIcon={<UserX size={13} />}
              onClick={() => onAction('noshow', booking)}
            >
              No-Show
            </Button>
          )}
          {canCancel && (
            <Button
              variant="danger"
              size="sm"
              leftIcon={<XCircle size={13} />}
              onClick={() => onAction('cancel', booking)}
            >
              Cancel
            </Button>
          )}
        </div>
      </div>
    </div>
  );
}

// ─── Skeleton ─────────────────────────────────────────────────────────────────

function BookingCardSkeleton() {
  return (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 space-y-3">
      <div className="flex justify-between">
        <div className="space-y-1.5">
          <Skeleton className="h-5 w-40" />
          <Skeleton className="h-3 w-28" />
        </div>
        <Skeleton className="h-6 w-20" />
      </div>
      <div className="flex gap-4">
        <Skeleton className="h-4 w-28" />
        <Skeleton className="h-4 w-36" />
      </div>
    </div>
  );
}

// ─── Main Page ────────────────────────────────────────────────────────────────

function ManagerBookingsPage() {
  const queryClient = useQueryClient();
  const [activeTab, setActiveTab] = useState('ALL');
  const [selectedHotelId, setSelectedHotelId] = useState('');
  const [page, setPage] = useState(0);
  const [actionState, setActionState] = useState({ type: null, booking: null });
  const [searchQuery, setSearchQuery] = useState('');

  const { register, handleSubmit, reset, formState: { errors } } = useForm({
    resolver: zodResolver(cancelSchema),
  });

  // Fetch manager hotels for the filter dropdown
  const { data: hotels = [] } = useQuery({
    queryKey: QUERY_KEYS.MANAGER_HOTELS,
    queryFn: getManagerHotels,
    staleTime: 60_000,
  });

  // Fetch bookings: use searchBookings for cross-hotel search, or per-hotel
  const bookingQueryKey = ['manager-bookings', selectedHotelId, activeTab, page, searchQuery];
  const { data, isLoading, isError } = useQuery({
    queryKey: bookingQueryKey,
    queryFn: () => {
      const params = { page, size: 15, sort: 'checkInDate,desc' };
      if (selectedHotelId) params.hotelId = selectedHotelId;
      if (activeTab !== 'ALL') params.status = activeTab;
      if (searchQuery.trim()) params.guestName = searchQuery.trim();
      return searchBookings(params);
    },
    keepPreviousData: true,
    staleTime: 15_000,
  });

  // Mutations
  const confirmMutation = useMutation({
    mutationFn: (id) => confirmBooking(id),
    onSuccess: () => { toast.success('Booking confirmed'); queryClient.invalidateQueries({ queryKey: ['manager-bookings'] }); setActionState({ type: null, booking: null }); },
    onError: (err) => toast.error(err.message || 'Failed to confirm booking'),
  });

  const cancelMutation = useMutation({
    mutationFn: ({ id, reason }) => cancelBooking(id, reason),
    onSuccess: () => { toast.success('Booking cancelled'); queryClient.invalidateQueries({ queryKey: ['manager-bookings'] }); setActionState({ type: null, booking: null }); reset(); },
    onError: (err) => toast.error(err.message || 'Failed to cancel booking'),
  });

  const completeMutation = useMutation({
    mutationFn: (id) => completeBooking(id),
    onSuccess: () => { toast.success('Booking marked as completed'); queryClient.invalidateQueries({ queryKey: ['manager-bookings'] }); setActionState({ type: null, booking: null }); },
    onError: (err) => toast.error(err.message || 'Failed to complete booking'),
  });

  const noShowMutation = useMutation({
    mutationFn: (id) => markNoShow(id),
    onSuccess: () => { toast.success('Booking marked as no-show'); queryClient.invalidateQueries({ queryKey: ['manager-bookings'] }); setActionState({ type: null, booking: null }); },
    onError: (err) => toast.error(err.message || 'Failed to mark no-show'),
  });

  const handleAction = (type, booking) => {
    reset();
    setActionState({ type, booking });
  };

  const handleConfirmAction = () => {
    const { type, booking } = actionState;
    if (!booking) return;
    if (type === 'confirm') confirmMutation.mutate(booking.id);
    if (type === 'complete') completeMutation.mutate(booking.id);
    if (type === 'noshow') noShowMutation.mutate(booking.id);
  };

  const handleCancelSubmit = ({ reason }) => {
    if (!actionState.booking) return;
    cancelMutation.mutate({ id: actionState.booking.id, reason });
  };

  const bookings = data?.content ?? [];
  const totalPages = data?.totalPages ?? 1;

  const isSingleActionPending =
    confirmMutation.isPending || completeMutation.isPending || noShowMutation.isPending;

  const actionLabels = {
    confirm: { title: 'Confirm Booking', btn: 'Confirm', icon: CheckCircle, variant: 'primary' },
    complete: { title: 'Mark as Completed', btn: 'Mark Complete', icon: CheckCircle, variant: 'secondary' },
    noshow: { title: 'Mark as No-Show', btn: 'Mark No-Show', icon: UserX, variant: 'ghost' },
  };

  const activeAction = actionState.type && actionLabels[actionState.type];

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
          <CalendarCheck size={24} className="text-amber-500" />
          Bookings Management
        </h1>
        <p className="text-gray-500 mt-0.5 text-sm">Review and manage all guest reservations</p>
      </div>

      {/* Filters bar */}
      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 flex flex-wrap gap-3 items-center">
        {/* Hotel filter */}
        {hotels.length > 1 && (
          <div className="flex items-center gap-2">
            <Filter size={15} className="text-gray-400" />
            <select
              value={selectedHotelId}
              onChange={(e) => { setSelectedHotelId(e.target.value); setPage(0); }}
              className="text-sm border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-amber-500 bg-white"
            >
              <option value="">All Hotels</option>
              {hotels.map((h) => (
                <option key={h.id} value={h.id}>{h.name}</option>
              ))}
            </select>
          </div>
        )}

        {/* Search */}
        <div className="relative flex-1 min-w-[180px]">
          <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            value={searchQuery}
            onChange={(e) => { setSearchQuery(e.target.value); setPage(0); }}
            placeholder="Search by guest name..."
            className="w-full pl-9 pr-3 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500"
          />
        </div>

        <span className="text-xs text-gray-400 ml-auto">
          {data?.totalElements ?? 0} booking{data?.totalElements !== 1 ? 's' : ''}
        </span>
      </div>

      {/* Status tabs */}
      <div className="flex gap-1 bg-white rounded-xl border border-gray-200 p-1 overflow-x-auto">
        {STATUS_TABS.map((tab) => (
          <button
            key={tab.key}
            onClick={() => { setActiveTab(tab.key); setPage(0); }}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-medium whitespace-nowrap transition-colors flex-shrink-0 ${
              activeTab === tab.key
                ? 'bg-amber-500 text-white shadow-sm'
                : 'text-gray-500 hover:text-gray-800 hover:bg-gray-50'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Content */}
      {isError ? (
        <div className="text-center py-16 text-red-500">Failed to load bookings. Please refresh.</div>
      ) : isLoading ? (
        <div className="space-y-4">
          {[1, 2, 3].map((i) => <BookingCardSkeleton key={i} />)}
        </div>
      ) : bookings.length === 0 ? (
        <EmptyState
          icon={Search}
          title="No bookings found"
          description={activeTab === 'ALL' ? 'No bookings match your current filters.' : `No ${activeTab.toLowerCase()} bookings.`}
          action={
            (activeTab !== 'ALL' || selectedHotelId || searchQuery) ? (
              <Button
                variant="ghost"
                onClick={() => { setActiveTab('ALL'); setSelectedHotelId(''); setSearchQuery(''); setPage(0); }}
              >
                Clear Filters
              </Button>
            ) : null
          }
        />
      ) : (
        <div className="space-y-4">
          {bookings.map((b) => (
            <BookingCard key={b.id} booking={b} onAction={handleAction} />
          ))}
        </div>
      )}

      {/* Pagination */}
      {!isLoading && totalPages > 1 && (
        <div className="flex items-center justify-center gap-3">
          <Button variant="secondary" size="sm" disabled={page === 0} onClick={() => setPage((p) => p - 1)}>
            Previous
          </Button>
          <span className="text-sm text-gray-500">Page {page + 1} of {totalPages}</span>
          <Button variant="secondary" size="sm" disabled={page + 1 >= totalPages} onClick={() => setPage((p) => p + 1)}>
            Next
          </Button>
        </div>
      )}

      {/* Confirm / Complete / No-Show Modal */}
      <Modal
        isOpen={!!activeAction}
        onClose={() => !isSingleActionPending && setActionState({ type: null, booking: null })}
        title={activeAction?.title ?? ''}
        size="sm"
        footer={
          <>
            <Button
              variant="ghost"
              onClick={() => setActionState({ type: null, booking: null })}
              disabled={isSingleActionPending}
            >
              Cancel
            </Button>
            <Button
              variant={activeAction?.variant ?? 'primary'}
              loading={isSingleActionPending}
              leftIcon={activeAction?.icon ? <activeAction.icon size={15} /> : null}
              onClick={handleConfirmAction}
            >
              {activeAction?.btn}
            </Button>
          </>
        }
      >
        {actionState.booking && (
          <div className="space-y-3">
            <div className="bg-gray-50 rounded-xl p-4 text-sm space-y-1">
              <p className="font-semibold text-gray-900">{actionState.booking.guestName || actionState.booking.guestEmail}</p>
              <p className="font-mono text-xs text-gray-500">{actionState.booking.bookingReference}</p>
              <div className="flex items-center gap-1.5 text-gray-600 mt-2">
                <Calendar size={13} className="text-amber-400" />
                <span>{formatDate(actionState.booking.checkInDate)} → {formatDate(actionState.booking.checkOutDate)}</span>
              </div>
            </div>
            <p className="text-sm text-gray-600">
              {actionState.type === 'confirm' && 'This will confirm the booking and notify the guest.'}
              {actionState.type === 'complete' && 'Mark this booking as completed after the guest checks out.'}
              {actionState.type === 'noshow' && 'Mark this booking as no-show. The guest did not arrive.'}
            </p>
          </div>
        )}
      </Modal>

      {/* Cancel Modal */}
      <Modal
        isOpen={actionState.type === 'cancel'}
        onClose={() => { setActionState({ type: null, booking: null }); reset(); }}
        title="Cancel Booking"
        size="sm"
        footer={
          <>
            <Button
              variant="ghost"
              onClick={() => { setActionState({ type: null, booking: null }); reset(); }}
              disabled={cancelMutation.isPending}
            >
              Keep Booking
            </Button>
            <Button
              variant="danger"
              loading={cancelMutation.isPending}
              leftIcon={<XCircle size={15} />}
              onClick={handleSubmit(handleCancelSubmit)}
            >
              Cancel Booking
            </Button>
          </>
        }
      >
        {actionState.booking && (
          <div className="space-y-4">
            <div className="bg-gray-50 rounded-xl p-4 text-sm space-y-1">
              <p className="font-semibold text-gray-900">{actionState.booking.guestName || actionState.booking.guestEmail}</p>
              <p className="font-mono text-xs text-gray-500">{actionState.booking.bookingReference}</p>
              <div className="flex items-center gap-1.5 text-gray-600 mt-2">
                <Receipt size={13} className="text-amber-400" />
                <span>{formatCurrency(actionState.booking.totalPrice)}</span>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">
                Reason for cancellation <span className="text-red-500">*</span>
              </label>
              <textarea
                rows={3}
                placeholder="Please provide a reason..."
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

export default ManagerBookingsPage;
