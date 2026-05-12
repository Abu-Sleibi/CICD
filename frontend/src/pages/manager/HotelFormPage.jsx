import { useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Hotel, Save, ArrowLeft, Star } from 'lucide-react';
import toast from 'react-hot-toast';
import { getHotelById, createHotel, updateHotel } from '../../api/hotelsApi';
import { QUERY_KEYS } from '../../utils/queryKeys';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Skeleton from '../../components/ui/Skeleton';

const COMMON_AMENITIES = [
  'WiFi', 'Pool', 'Spa', 'Gym', 'Parking', 'Restaurant', 'Bar',
  'Room Service', 'Laundry', 'Business Center', 'Pet Friendly',
  'Airport Shuttle', 'Concierge', 'Air Conditioning', 'Beachfront',
];

const hotelSchema = z.object({
  name: z.string().min(2, 'Hotel name must be at least 2 characters').max(100),
  description: z.string().min(10, 'Description must be at least 10 characters').max(1000),
  city: z.string().min(2, 'City is required').max(50),
  country: z.string().min(2, 'Country is required').max(50),
  address: z.string().min(5, 'Address is required').max(200),
  postalCode: z.string().optional(),
  starRating: z.coerce.number().int().min(1).max(5),
  phone: z.string().regex(/^\+?[0-9]{10,15}$/, 'Phone must be 10–15 digits').optional().or(z.literal('')),
  email: z.string().email('Invalid email').optional().or(z.literal('')),
  imageUrl: z.string().url('Must be a valid URL').optional().or(z.literal('')),
  checkInTime: z.string().optional(),
  checkOutTime: z.string().optional(),
  amenities: z.array(z.string()).optional(),
});

// ─── Star Rating Picker ───────────────────────────────────────────────────────

function StarPicker({ value, onChange }) {
  return (
    <div className="flex gap-1">
      {[1, 2, 3, 4, 5].map((n) => (
        <button
          key={n}
          type="button"
          onClick={() => onChange(n)}
          className="p-0.5 focus:outline-none focus:ring-2 focus:ring-amber-500 rounded"
        >
          <Star
            size={24}
            className={n <= value ? 'fill-amber-400 text-amber-400' : 'text-gray-300'}
          />
        </button>
      ))}
    </div>
  );
}

// ─── Amenities Checkbox Group ─────────────────────────────────────────────────

function AmenitiesSelector({ value = [], onChange }) {
  const toggle = (amenity) => {
    if (value.includes(amenity)) {
      onChange(value.filter((a) => a !== amenity));
    } else {
      onChange([...value, amenity]);
    }
  };

  return (
    <div className="flex flex-wrap gap-2">
      {COMMON_AMENITIES.map((amenity) => {
        const checked = value.includes(amenity);
        return (
          <button
            key={amenity}
            type="button"
            onClick={() => toggle(amenity)}
            className={`px-3 py-1.5 rounded-full text-sm font-medium border transition-colors ${
              checked
                ? 'bg-amber-500 text-white border-amber-500'
                : 'bg-white text-gray-600 border-gray-300 hover:border-amber-400'
            }`}
          >
            {amenity}
          </button>
        );
      })}
    </div>
  );
}

// ─── Form section wrapper ─────────────────────────────────────────────────────

function Section({ title, children }) {
  return (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 space-y-5">
      <h2 className="font-semibold text-gray-900 text-base border-b border-gray-100 pb-3">{title}</h2>
      {children}
    </div>
  );
}

// ─── Field error helper ───────────────────────────────────────────────────────

function FieldError({ error }) {
  if (!error) return null;
  return <p className="text-xs text-red-500 mt-0.5">{error.message}</p>;
}

// ─── Main Page ────────────────────────────────────────────────────────────────

function HotelFormPage() {
  const { id } = useParams();
  const isEdit = !!id;
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const {
    register,
    handleSubmit,
    reset,
    control,
    watch,
    formState: { errors, isDirty },
  } = useForm({
    resolver: zodResolver(hotelSchema),
    defaultValues: {
      name: '',
      description: '',
      city: '',
      country: '',
      address: '',
      postalCode: '',
      starRating: 3,
      phone: '',
      email: '',
      imageUrl: '',
      checkInTime: '14:00',
      checkOutTime: '11:00',
      amenities: [],
    },
  });

  // Fetch existing hotel data for edit mode
  const { data: hotel, isLoading: isLoadingHotel } = useQuery({
    queryKey: QUERY_KEYS.HOTEL_DETAIL(id),
    queryFn: () => getHotelById(id),
    enabled: isEdit,
  });

  useEffect(() => {
    if (hotel) {
      reset({
        name: hotel.name ?? '',
        description: hotel.description ?? '',
        city: hotel.city ?? '',
        country: hotel.country ?? '',
        address: hotel.address ?? '',
        postalCode: hotel.postalCode ?? '',
        starRating: hotel.starRating ?? 3,
        phone: hotel.phone ?? '',
        email: hotel.email ?? '',
        imageUrl: hotel.imageUrl ?? '',
        checkInTime: hotel.checkInTime ?? '14:00',
        checkOutTime: hotel.checkOutTime ?? '11:00',
        amenities: hotel.amenities ?? [],
      });
    }
  }, [hotel, reset]);

  const mutation = useMutation({
    mutationFn: (data) => isEdit ? updateHotel(id, data) : createHotel(data),
    onSuccess: (saved) => {
      toast.success(isEdit ? 'Hotel updated successfully' : 'Hotel created successfully');
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.MANAGER_HOTELS });
      if (isEdit) {
        queryClient.invalidateQueries({ queryKey: QUERY_KEYS.HOTEL_DETAIL(id) });
      }
      navigate('/manager/hotels');
    },
    onError: (err) => {
      toast.error(err.message || 'Failed to save hotel. Please try again.');
    },
  });

  const onSubmit = (data) => {
    const payload = {
      name: data.name,
      description: data.description,
      address: data.address,
      city: data.city,
      country: data.country,
      postalCode: data.postalCode || undefined,
      phone: data.phone || undefined,
      email: data.email || undefined,
      starRating: Number(data.starRating),
    };
    mutation.mutate(payload);
  };

  if (isEdit && isLoadingHotel) {
    return (
      <div className="max-w-2xl mx-auto space-y-6">
        <Skeleton className="h-8 w-64" />
        {[1, 2, 3].map((i) => (
          <div key={i} className="bg-white rounded-2xl border border-gray-100 p-6 space-y-4">
            <Skeleton className="h-5 w-32" />
            <Skeleton className="h-10 w-full rounded-lg" />
            <Skeleton className="h-10 w-full rounded-lg" />
          </div>
        ))}
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center gap-3">
        <Link to="/manager/hotels">
          <Button variant="ghost" size="sm" leftIcon={<ArrowLeft size={15} />}>
            Back
          </Button>
        </Link>
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <Hotel size={22} className="text-amber-500" />
            {isEdit ? 'Edit Hotel' : 'Add New Hotel'}
          </h1>
          {isEdit && hotel && (
            <p className="text-sm text-gray-500 mt-0.5">{hotel.name}</p>
          )}
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        {/* Basic Info */}
        <Section title="Basic Information">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              Hotel Name <span className="text-red-500">*</span>
            </label>
            <input
              {...register('name')}
              placeholder="e.g. Grand Seaside Hotel"
              className={`w-full px-3 py-2.5 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500 ${
                errors.name ? 'border-red-400' : 'border-gray-300'
              }`}
            />
            <FieldError error={errors.name} />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Description</label>
            <textarea
              {...register('description')}
              rows={3}
              placeholder="Describe your hotel..."
              className="w-full px-3 py-2.5 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500 resize-none"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">
              Star Rating <span className="text-red-500">*</span>
            </label>
            <Controller
              name="starRating"
              control={control}
              render={({ field }) => (
                <StarPicker value={field.value} onChange={field.onChange} />
              )}
            />
            <FieldError error={errors.starRating} />
          </div>
        </Section>

        {/* Location */}
        <Section title="Location">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">
                City <span className="text-red-500">*</span>
              </label>
              <input
                {...register('city')}
                placeholder="e.g. New York"
                className={`w-full px-3 py-2.5 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500 ${
                  errors.city ? 'border-red-400' : 'border-gray-300'
                }`}
              />
              <FieldError error={errors.city} />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">
                Country <span className="text-red-500">*</span>
              </label>
              <input
                {...register('country')}
                placeholder="e.g. United States"
                className={`w-full px-3 py-2.5 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500 ${
                  errors.country ? 'border-red-400' : 'border-gray-300'
                }`}
              />
              <FieldError error={errors.country} />
            </div>

            <div className="sm:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1.5">
                Address <span className="text-red-500">*</span>
              </label>
              <input
                {...register('address')}
                placeholder="e.g. 123 Ocean Drive"
                className={`w-full px-3 py-2.5 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500 ${
                  errors.address ? 'border-red-400' : 'border-gray-300'
                }`}
              />
              <FieldError error={errors.address} />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Postal Code</label>
              <input
                {...register('postalCode')}
                placeholder="e.g. 10001"
                className="w-full px-3 py-2.5 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500"
              />
            </div>
          </div>
        </Section>

        {/* Contact */}
        <Section title="Contact & Media">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Phone Number</label>
              <input
                {...register('phone')}
                placeholder="+15550100"
                className={`w-full px-3 py-2.5 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500 ${
                  errors.phone ? 'border-red-400' : 'border-gray-300'
                }`}
              />
              <FieldError error={errors.phone} />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Email</label>
              <input
                {...register('email')}
                type="email"
                placeholder="info@hotel.com"
                className={`w-full px-3 py-2.5 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500 ${
                  errors.email ? 'border-red-400' : 'border-gray-300'
                }`}
              />
              <FieldError error={errors.email} />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1.5">Image URL</label>
            <input
              {...register('imageUrl')}
              placeholder="https://example.com/hotel-photo.jpg"
              className={`w-full px-3 py-2.5 text-sm border rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500 ${
                errors.imageUrl ? 'border-red-400' : 'border-gray-300'
              }`}
            />
            <FieldError error={errors.imageUrl} />
          </div>

          {/* Image preview */}
          {watch('imageUrl') && !errors.imageUrl && (
            <div className="mt-2 rounded-xl overflow-hidden h-40 bg-gray-100">
              <img
                src={watch('imageUrl')}
                alt="Hotel preview"
                className="w-full h-full object-cover"
                onError={(e) => { e.currentTarget.style.display = 'none'; }}
              />
            </div>
          )}
        </Section>

        {/* Check-in / Check-out times */}
        <Section title="Policies">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Check-in Time</label>
              <input
                {...register('checkInTime')}
                type="time"
                className="w-full px-3 py-2.5 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1.5">Check-out Time</label>
              <input
                {...register('checkOutTime')}
                type="time"
                className="w-full px-3 py-2.5 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-amber-500"
              />
            </div>
          </div>
        </Section>

        {/* Amenities */}
        <Section title="Amenities">
          <Controller
            name="amenities"
            control={control}
            render={({ field }) => (
              <AmenitiesSelector value={field.value} onChange={field.onChange} />
            )}
          />
          {watch('amenities')?.length > 0 && (
            <p className="text-xs text-gray-400 mt-1">
              {watch('amenities').length} amenit{watch('amenities').length === 1 ? 'y' : 'ies'} selected
            </p>
          )}
        </Section>

        {/* Submit */}
        <div className="flex items-center justify-end gap-3 pb-6">
          <Link to="/manager/hotels">
            <Button variant="ghost" type="button" disabled={mutation.isPending}>
              Cancel
            </Button>
          </Link>
          <Button
            type="submit"
            variant="primary"
            loading={mutation.isPending}
            leftIcon={<Save size={16} />}
          >
            {isEdit ? 'Save Changes' : 'Create Hotel'}
          </Button>
        </div>
      </form>
    </div>
  );
}

export default HotelFormPage;
