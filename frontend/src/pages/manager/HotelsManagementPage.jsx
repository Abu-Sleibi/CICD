import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
  Hotel,
  Plus,
  Edit,
  Trash2,
  BedDouble,
  MapPin,
  Star,
  AlertTriangle,
} from 'lucide-react';
import toast from 'react-hot-toast';
import { getManagerHotels, deleteHotel } from '../../api/hotelsApi';
import { QUERY_KEYS } from '../../utils/queryKeys';
import { useAuth } from '../../hooks/useAuth';
import Button from '../../components/ui/Button';
import Modal from '../../components/ui/Modal';
import Skeleton from '../../components/ui/Skeleton';
import EmptyState from '../../components/ui/EmptyState';

// ─── Hotel Row Card ───────────────────────────────────────────────────────────

function HotelCard({ hotel, onDeleteClick }) {
  return (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden hover:shadow-md transition-shadow">
      <div className="flex items-start gap-4 p-5">
        {/* Image / placeholder */}
        <div className="flex-shrink-0 w-16 h-16 rounded-xl bg-amber-50 flex items-center justify-center overflow-hidden">
          {hotel.imageUrl ? (
            <img src={hotel.imageUrl} alt={hotel.name} className="w-full h-full object-cover" />
          ) : (
            <Hotel size={28} className="text-amber-300" />
          )}
        </div>

        {/* Info */}
        <div className="flex-1 min-w-0">
          <div className="flex items-start justify-between gap-2 flex-wrap">
            <div className="min-w-0">
              <h3 className="font-semibold text-gray-900 truncate">{hotel.name}</h3>
              <div className="flex items-center gap-1 mt-0.5 text-sm text-gray-500">
                <MapPin size={13} className="text-amber-400 flex-shrink-0" />
                <span className="truncate">{hotel.address ? `${hotel.address}, ` : ''}{hotel.city}</span>
              </div>
            </div>
            {hotel.starRating > 0 && (
              <div className="flex items-center gap-0.5 flex-shrink-0">
                {Array.from({ length: hotel.starRating }).map((_, i) => (
                  <Star key={i} size={13} className="fill-amber-400 text-amber-400" />
                ))}
              </div>
            )}
          </div>

          {hotel.description && (
            <p className="text-sm text-gray-500 mt-1.5 line-clamp-2">{hotel.description}</p>
          )}

          {/* Amenities preview */}
          {hotel.amenities?.length > 0 && (
            <div className="flex flex-wrap gap-1.5 mt-2">
              {hotel.amenities.slice(0, 4).map((a) => (
                <span key={a} className="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded-full">
                  {a}
                </span>
              ))}
              {hotel.amenities.length > 4 && (
                <span className="text-xs text-gray-400">+{hotel.amenities.length - 4} more</span>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Footer actions */}
      <div className="px-5 py-3 bg-gray-50 border-t border-gray-100 flex items-center justify-between gap-2 flex-wrap">
        <Link to={`/manager/hotels/${hotel.id}/rooms`}>
          <Button variant="ghost" size="sm" leftIcon={<BedDouble size={13} />}>
            Room Types
          </Button>
        </Link>

        <div className="flex items-center gap-2">
          <Link to={`/manager/hotels/${hotel.id}/edit`}>
            <Button variant="secondary" size="sm" leftIcon={<Edit size={13} />}>
              Edit
            </Button>
          </Link>
          <Button
            variant="danger"
            size="sm"
            leftIcon={<Trash2 size={13} />}
            onClick={() => onDeleteClick(hotel)}
          >
            Delete
          </Button>
        </div>
      </div>
    </div>
  );
}

// ─── Skeleton ─────────────────────────────────────────────────────────────────

function HotelCardSkeleton() {
  return (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5 space-y-3">
      <div className="flex gap-4">
        <Skeleton className="w-16 h-16 rounded-xl flex-shrink-0" />
        <div className="flex-1 space-y-2">
          <Skeleton className="h-5 w-48" />
          <Skeleton className="h-4 w-32" />
          <Skeleton className="h-4 w-full" />
        </div>
      </div>
      <div className="flex gap-2 pt-2 border-t border-gray-100">
        <Skeleton className="h-8 w-28 rounded-lg" />
        <Skeleton className="h-8 w-16 rounded-lg ml-auto" />
        <Skeleton className="h-8 w-16 rounded-lg" />
      </div>
    </div>
  );
}

// ─── Main Page ────────────────────────────────────────────────────────────────

function HotelsManagementPage() {
  const queryClient = useQueryClient();
  const { isAdmin } = useAuth();
  const [deleteTarget, setDeleteTarget] = useState(null);

  const { data: hotels = [], isLoading, isError } = useQuery({
    queryKey: QUERY_KEYS.MANAGER_HOTELS,
    queryFn: getManagerHotels,
    staleTime: 30_000,
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => deleteHotel(id),
    onSuccess: () => {
      toast.success('Hotel deleted successfully');
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.MANAGER_HOTELS });
      setDeleteTarget(null);
    },
    onError: (err) => {
      toast.error(err.message || 'Could not delete hotel. Please try again.');
    },
  });

  const handleDeleteClick = (hotel) => setDeleteTarget(hotel);
  const handleDeleteConfirm = () => {
    if (!deleteTarget) return;
    deleteMutation.mutate(deleteTarget.id);
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <Hotel size={24} className="text-amber-500" />
            Hotels Management
          </h1>
          <p className="text-gray-500 mt-0.5 text-sm">
            {isLoading ? '...' : `${hotels.length} ${hotels.length === 1 ? 'hotel' : 'hotels'} managed`}
          </p>
        </div>
        {isAdmin && (
          <Link to="/manager/hotels/new">
            <Button variant="primary" leftIcon={<Plus size={16} />}>
              Add Hotel
            </Button>
          </Link>
        )}
      </div>

      {/* Content */}
      {isError ? (
        <div className="text-center py-16 text-red-500">
          Failed to load hotels. Please refresh the page.
        </div>
      ) : isLoading ? (
        <div className="space-y-4">
          {[1, 2, 3].map((i) => <HotelCardSkeleton key={i} />)}
        </div>
      ) : hotels.length === 0 ? (
        <EmptyState
          icon={Hotel}
          title="No hotels yet"
          description={isAdmin ? 'Add your first hotel property to get started.' : 'No hotels are assigned to your account.'}
          action={
            isAdmin ? (
              <Link to="/manager/hotels/new">
                <Button variant="primary" leftIcon={<Plus size={16} />}>Add Hotel</Button>
              </Link>
            ) : null
          }
        />
      ) : (
        <div className="space-y-4">
          {hotels.map((hotel) => (
            <HotelCard
              key={hotel.id}
              hotel={hotel}
              onDeleteClick={handleDeleteClick}
            />
          ))}
        </div>
      )}

      {/* Delete confirmation modal */}
      <Modal
        isOpen={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        title="Delete Hotel"
        size="sm"
        footer={
          <>
            <Button
              variant="ghost"
              onClick={() => setDeleteTarget(null)}
              disabled={deleteMutation.isPending}
            >
              Cancel
            </Button>
            <Button
              variant="danger"
              loading={deleteMutation.isPending}
              leftIcon={<Trash2 size={15} />}
              onClick={handleDeleteConfirm}
            >
              Delete Hotel
            </Button>
          </>
        }
      >
        {deleteTarget && (
          <div className="space-y-4">
            <div className="flex items-start gap-3 p-4 bg-red-50 rounded-xl border border-red-100">
              <AlertTriangle size={20} className="text-red-500 flex-shrink-0 mt-0.5" />
              <div>
                <p className="font-semibold text-red-800">{deleteTarget.name}</p>
                <p className="text-sm text-red-600 mt-0.5">{deleteTarget.city}</p>
              </div>
            </div>
            <p className="text-sm text-gray-600">
              Deleting this hotel will remove all associated room types and data. This action cannot be undone.
            </p>
          </div>
        )}
      </Modal>
    </div>
  );
}

export default HotelsManagementPage;
