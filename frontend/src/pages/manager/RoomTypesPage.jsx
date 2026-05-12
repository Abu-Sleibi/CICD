import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import {
  BedDouble,
  Plus,
  Edit,
  Trash2,
  ArrowLeft,
  Users,
  DollarSign,
  AlertTriangle,
  Hash,
} from 'lucide-react';
import toast from 'react-hot-toast';
import { getRoomTypesByHotel, createRoomType, updateRoomType, deleteRoomType } from '../../api/roomTypesApi';
import { getHotelById } from '../../api/hotelsApi';
import { QUERY_KEYS } from '../../utils/queryKeys';
import { formatCurrency } from '../../utils/dateUtils';
import Button from '../../components/ui/Button';
import Modal from '../../components/ui/Modal';
import Skeleton from '../../components/ui/Skeleton';
import EmptyState from '../../components/ui/EmptyState';

const ROOM_AMENITIES = [
  'King Bed', 'Queen Bed', 'Twin Beds', 'Sofa Bed', 'Balcony', 'Ocean View',
  'WiFi', 'Smart TV', 'Mini Bar', 'Bathtub', 'Shower', 'Safe', 'Coffee Maker',
  'Work Desk', 'Air Conditioning', 'Kitchenette', 'Living Room',
];

const roomTypeSchema = z.object({
  name: z.string().min(2, 'Name must be at least 2 characters').max(50, 'Name must be at most 50 characters'),
  description: z.string().min(10, 'Description must be at least 10 characters').max(500, 'Description must be at most 500 characters'),
  pricePerNight: z.coerce.number().positive('Price must be greater than 0'),
  capacity: z.coerce.number().int().min(1, 'Capacity must be at least 1').max(10, 'Capacity must be at most 10'),
  totalRooms: z.coerce.number().int().min(1, 'Must have at least 1 room').max(500),
  imageUrl: z.string().url('Must be a valid URL').optional().or(z.literal('')),
  amenities: z.array(z.string()).optional(),
});

// ─── Amenities selector ───────────────────────────────────────────────────────

function AmenitiesSelector({ value = [], onChange }) {
  const toggle = (a) =>
    onChange(value.includes(a) ? value.filter((x) => x !== a) : [...value, a]);

  return (
    <div className="flex flex-wrap gap-1.5">
      {ROOM_AMENITIES.map((a) => (
        <button
          key={a}
          type="button"
          onClick={() => toggle(a)}
          className={`px-2.5 py-1 rounded-full text-xs font-medium border transition-colors ${
            value.includes(a)
              ? 'bg-amber-500 text-white border-amber-500'
              : 'bg-white text-gray-600 border-gray-300 hover:border-amber-400'
          }`}
        >
          {a}
        </button>
      ))}
    </div>
  );
}

// ─── Room Type Card ───────────────────────────────────────────────────────────

function RoomTypeCard({ roomType, onEdit, onDelete }) {
  return (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden hover:shadow-md transition-shadow">
      <div className="flex items-start gap-4 p-5">
        {/* Image / icon */}
        <div className="flex-shrink-0 w-14 h-14 rounded-xl bg-amber-50 flex items-center justify-center overflow-hidden">
          {roomType.imageUrl ? (
            <img src={roomType.imageUrl} alt={roomType.name} className="w-full h-full object-cover" />
          ) : (
            <BedDouble size={24} className="text-amber-400" />
          )}
        </div>

        {/* Info */}
        <div className="flex-1 min-w-0">
          <div className="flex items-start justify-between gap-2">
            <h3 className="font-semibold text-gray-900">{roomType.name}</h3>
            <span className="text-lg font-bold text-amber-600 flex-shrink-0">
              {formatCurrency(roomType.pricePerNight)}
              <span className="text-xs font-normal text-gray-400">/night</span>
            </span>
          </div>

          {roomType.description && (
            <p className="text-sm text-gray-500 mt-1 line-clamp-2">{roomType.description}</p>
          )}

          <div className="flex items-center gap-4 mt-2 text-sm text-gray-600">
            <div className="flex items-center gap-1">
              <Users size={13} className="text-amber-400" />
              <span>Up to {roomType.capacity} {roomType.capacity === 1 ? 'guest' : 'guests'}</span>
            </div>
            <div className="flex items-center gap-1">
              <Hash size={13} className="text-amber-400" />
              <span>{roomType.totalRooms} {roomType.totalRooms === 1 ? 'room' : 'rooms'}</span>
            </div>
          </div>

          {roomType.amenities?.length > 0 && (
            <div className="flex flex-wrap gap-1 mt-2">
              {roomType.amenities.slice(0, 5).map((a) => (
                <span key={a} className="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded-full">{a}</span>
              ))}
              {roomType.amenities.length > 5 && (
                <span className="text-xs text-gray-400">+{roomType.amenities.length - 5} more</span>
              )}
            </div>
          )}
        </div>
      </div>

      <div className="px-5 py-3 bg-gray-50 border-t border-gray-100 flex justify-end gap-2">
        <Button variant="secondary" size="sm" leftIcon={<Edit size={13} />} onClick={() => onEdit(roomType)}>
          Edit
        </Button>
        <Button variant="danger" size="sm" leftIcon={<Trash2 size={13} />} onClick={() => onDelete(roomType)}>
          Delete
        </Button>
      </div>
    </div>
  );
}

// ─── Room Type Form (inside modal) ────────────────────────────────────────────

function RoomTypeForm({ defaultValues, onSubmit, isPending }) {
  const {
    register,
    handleSubmit,
    control,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(roomTypeSchema),
    defaultValues: defaultValues ?? {
      name: '',
      description: '',
      pricePerNight: '',
      capacity: 2,
      totalRooms: 1,
      imageUrl: '',
      amenities: [],
    },
  });

  return (
    <form id="room-type-form" onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Room Type Name <span className="text-red-500">*</span>
        </label>
        <input
          {...register('name')}
          placeholder="e.g. Deluxe Suite"
          className={`w-full px-3 py-2.5 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500 ${
            errors.name ? 'border-red-400' : 'border-gray-300'
          }`}
        />
        {errors.name && <p className="text-xs text-red-500 mt-0.5">{errors.name.message}</p>}
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">
          Description <span className="text-red-500">*</span>
        </label>
        <textarea
          {...register('description')}
          rows={2}
          placeholder="Describe this room type (at least 10 characters)..."
          className={`w-full px-3 py-2.5 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500 resize-none ${
            errors.description ? 'border-red-400' : 'border-gray-300'
          }`}
        />
        {errors.description && <p className="text-xs text-red-500 mt-0.5">{errors.description.message}</p>}
      </div>

      <div className="grid grid-cols-3 gap-3">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Price/Night <span className="text-red-500">*</span>
          </label>
          <div className="relative">
            <DollarSign size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input
              {...register('pricePerNight')}
              type="number"
              step="0.01"
              min="0"
              placeholder="0.00"
              className={`w-full pl-8 pr-3 py-2.5 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500 ${
                errors.pricePerNight ? 'border-red-400' : 'border-gray-300'
              }`}
            />
          </div>
          {errors.pricePerNight && <p className="text-xs text-red-500 mt-0.5">{errors.pricePerNight.message}</p>}
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Capacity <span className="text-red-500">*</span>
          </label>
          <input
            {...register('capacity')}
            type="number"
            min="1"
            max="20"
            className={`w-full px-3 py-2.5 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500 ${
              errors.capacity ? 'border-red-400' : 'border-gray-300'
            }`}
          />
          {errors.capacity && <p className="text-xs text-red-500 mt-0.5">{errors.capacity.message}</p>}
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Total Rooms <span className="text-red-500">*</span>
          </label>
          <input
            {...register('totalRooms')}
            type="number"
            min="1"
            className={`w-full px-3 py-2.5 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500 ${
              errors.totalRooms ? 'border-red-400' : 'border-gray-300'
            }`}
          />
          {errors.totalRooms && <p className="text-xs text-red-500 mt-0.5">{errors.totalRooms.message}</p>}
        </div>
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">Image URL</label>
        <input
          {...register('imageUrl')}
          placeholder="https://example.com/room.jpg"
          className={`w-full px-3 py-2.5 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500 ${
            errors.imageUrl ? 'border-red-400' : 'border-gray-300'
          }`}
        />
        {errors.imageUrl && <p className="text-xs text-red-500 mt-0.5">{errors.imageUrl.message}</p>}
      </div>

      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">Room Amenities</label>
        <Controller
          name="amenities"
          control={control}
          render={({ field }) => (
            <AmenitiesSelector value={field.value} onChange={field.onChange} />
          )}
        />
      </div>
    </form>
  );
}

// ─── Main Page ────────────────────────────────────────────────────────────────

function RoomTypesPage() {
  const { hotelId } = useParams();
  const queryClient = useQueryClient();
  const [modalState, setModalState] = useState({ mode: null, target: null }); // mode: 'add' | 'edit' | 'delete'

  const { data: hotel } = useQuery({
    queryKey: QUERY_KEYS.HOTEL_DETAIL(hotelId),
    queryFn: () => getHotelById(hotelId),
    enabled: !!hotelId,
  });

  const { data: roomTypes = [], isLoading, isError } = useQuery({
    queryKey: QUERY_KEYS.ROOM_TYPES(hotelId),
    queryFn: () => getRoomTypesByHotel(hotelId),
    enabled: !!hotelId,
  });

  const createMutation = useMutation({
    mutationFn: (data) => createRoomType({ ...data, hotelId }),
    onSuccess: () => {
      toast.success('Room type created');
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.ROOM_TYPES(hotelId) });
      setModalState({ mode: null, target: null });
    },
    onError: (err) => toast.error(err.message || 'Failed to create room type'),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => updateRoomType(id, { ...data, hotelId }),
    onSuccess: () => {
      toast.success('Room type updated');
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.ROOM_TYPES(hotelId) });
      setModalState({ mode: null, target: null });
    },
    onError: (err) => toast.error(err.message || 'Failed to update room type'),
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => deleteRoomType(id),
    onSuccess: () => {
      toast.success('Room type deleted');
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.ROOM_TYPES(hotelId) });
      setModalState({ mode: null, target: null });
    },
    onError: (err) => toast.error(err.message || 'Failed to delete room type'),
  });

  const handleFormSubmit = (data) => {
    const payload = {
      ...data,
      amenities: data.amenities ?? [],
      pricePerNight: Number(data.pricePerNight),
      capacity: Number(data.capacity),
      totalRooms: Number(data.totalRooms),
    };
    if (!payload.imageUrl) delete payload.imageUrl;

    if (modalState.mode === 'add') {
      createMutation.mutate(payload);
    } else if (modalState.mode === 'edit' && modalState.target) {
      updateMutation.mutate({ id: modalState.target.id, data: payload });
    }
  };

  const isPending = createMutation.isPending || updateMutation.isPending;

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center gap-3 flex-wrap">
        <Link to="/manager/hotels">
          <Button variant="ghost" size="sm" leftIcon={<ArrowLeft size={15} />}>
            Hotels
          </Button>
        </Link>
        <div className="flex-1 min-w-0">
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <BedDouble size={22} className="text-amber-500" />
            Room Types
          </h1>
          {hotel && (
            <p className="text-sm text-gray-500 mt-0.5">{hotel.name} · {hotel.city}</p>
          )}
        </div>
        <Button
          variant="primary"
          leftIcon={<Plus size={16} />}
          onClick={() => setModalState({ mode: 'add', target: null })}
        >
          Add Room Type
        </Button>
      </div>

      {/* Content */}
      {isError ? (
        <div className="text-center py-16 text-red-500">Failed to load room types.</div>
      ) : isLoading ? (
        <div className="space-y-4">
          {[1, 2].map((i) => (
            <div key={i} className="bg-white rounded-2xl border border-gray-100 p-5 space-y-3">
              <div className="flex gap-4">
                <Skeleton className="w-14 h-14 rounded-xl" />
                <div className="flex-1 space-y-2">
                  <Skeleton className="h-5 w-48" />
                  <Skeleton className="h-4 w-full" />
                  <Skeleton className="h-4 w-32" />
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : roomTypes.length === 0 ? (
        <EmptyState
          icon={BedDouble}
          title="No room types yet"
          description="Add room types so guests can book your hotel."
          action={
            <Button
              variant="primary"
              leftIcon={<Plus size={16} />}
              onClick={() => setModalState({ mode: 'add', target: null })}
            >
              Add Room Type
            </Button>
          }
        />
      ) : (
        <div className="space-y-4">
          {roomTypes.map((rt) => (
            <RoomTypeCard
              key={rt.id}
              roomType={rt}
              onEdit={(rt) => setModalState({ mode: 'edit', target: rt })}
              onDelete={(rt) => setModalState({ mode: 'delete', target: rt })}
            />
          ))}
        </div>
      )}

      {/* Add / Edit Modal */}
      <Modal
        isOpen={modalState.mode === 'add' || modalState.mode === 'edit'}
        onClose={() => !isPending && setModalState({ mode: null, target: null })}
        title={modalState.mode === 'add' ? 'Add Room Type' : 'Edit Room Type'}
        size="lg"
        footer={
          <>
            <Button
              variant="ghost"
              onClick={() => setModalState({ mode: null, target: null })}
              disabled={isPending}
            >
              Cancel
            </Button>
            <Button
              variant="primary"
              form="room-type-form"
              type="submit"
              loading={isPending}
              leftIcon={<BedDouble size={15} />}
            >
              {modalState.mode === 'add' ? 'Create Room Type' : 'Save Changes'}
            </Button>
          </>
        }
      >
        {(modalState.mode === 'add' || modalState.mode === 'edit') && (
          <RoomTypeForm
            key={modalState.target?.id ?? 'new'}
            defaultValues={
              modalState.target
                ? {
                    name: modalState.target.name ?? '',
                    description: modalState.target.description ?? '',
                    pricePerNight: modalState.target.pricePerNight ?? '',
                    capacity: modalState.target.capacity ?? 2,
                    totalRooms: modalState.target.totalRooms ?? 1,
                    imageUrl: modalState.target.imageUrl ?? '',
                    amenities: modalState.target.amenities ?? [],
                  }
                : undefined
            }
            onSubmit={handleFormSubmit}
            isPending={isPending}
          />
        )}
      </Modal>

      {/* Delete Modal */}
      <Modal
        isOpen={modalState.mode === 'delete'}
        onClose={() => setModalState({ mode: null, target: null })}
        title="Delete Room Type"
        size="sm"
        footer={
          <>
            <Button
              variant="ghost"
              onClick={() => setModalState({ mode: null, target: null })}
              disabled={deleteMutation.isPending}
            >
              Cancel
            </Button>
            <Button
              variant="danger"
              loading={deleteMutation.isPending}
              leftIcon={<Trash2 size={15} />}
              onClick={() => deleteMutation.mutate(modalState.target?.id)}
            >
              Delete
            </Button>
          </>
        }
      >
        {modalState.target && (
          <div className="space-y-3">
            <div className="flex items-start gap-3 p-4 bg-red-50 rounded-xl border border-red-100">
              <AlertTriangle size={20} className="text-red-500 flex-shrink-0 mt-0.5" />
              <div>
                <p className="font-semibold text-red-800">{modalState.target.name}</p>
                <p className="text-sm text-red-600 mt-0.5">
                  {formatCurrency(modalState.target.pricePerNight)}/night · {modalState.target.totalRooms} rooms
                </p>
              </div>
            </div>
            <p className="text-sm text-gray-600">
              This room type will be permanently removed. Existing bookings will not be affected.
            </p>
          </div>
        )}
      </Modal>
    </div>
  );
}

export default RoomTypesPage;
