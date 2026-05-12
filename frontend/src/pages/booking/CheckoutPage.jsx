import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Lock, CreditCard, User, Mail, Phone, FileText, ArrowLeft } from 'lucide-react';
import toast from 'react-hot-toast';
import { createBooking } from '../../api/bookingsApi';
import BookingSummary from '../../components/booking/BookingSummary';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import { useAuth } from '../../hooks/useAuth';

const guestSchema = z.object({
  fullName: z.string().min(2, 'Full name is required'),
  email: z.string().email('Valid email is required'),
  phone: z.string().min(7, 'Phone number is required'),
  specialRequests: z.string().optional(),
  cardNumber: z
    .string()
    .min(19, 'Enter a valid 16-digit card number')
    .max(19),
  cardExpiry: z
    .string()
    .regex(/^\d{2}\/\d{2}$/, 'Format: MM/YY'),
  cardCvv: z
    .string()
    .min(3, 'CVV must be 3–4 digits')
    .max(4),
  cardName: z.string().min(2, 'Cardholder name is required'),
});

function formatCardNumber(value) {
  const digits = value.replace(/\D/g, '').slice(0, 16);
  return digits.replace(/(.{4})/g, '$1 ').trim();
}

function formatExpiry(value) {
  const digits = value.replace(/\D/g, '').slice(0, 4);
  if (digits.length >= 3) {
    return `${digits.slice(0, 2)}/${digits.slice(2)}`;
  }
  return digits;
}

function CheckoutPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user, isAuthenticated } = useAuth();
  const [checkoutData, setCheckoutData] = useState(null);

  useEffect(() => {
    const raw = sessionStorage.getItem('checkoutData');
    if (!raw) {
      toast.error('No booking data found. Please start over.');
      navigate('/search');
      return;
    }
    try {
      setCheckoutData(JSON.parse(raw));
    } catch {
      navigate('/search');
    }
  }, [navigate]);

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(guestSchema),
    defaultValues: {
      fullName: user?.fullName || '',
      email: user?.email || '',
      phone: '',
      specialRequests: '',
      cardNumber: '',
      cardExpiry: '',
      cardCvv: '',
      cardName: user?.fullName || '',
    },
  });

  const cardNumberRaw = watch('cardNumber');

  const bookingMutation = useMutation({
    mutationFn: createBooking,
    onSuccess: (data) => {
      sessionStorage.removeItem('checkoutData');
      queryClient.invalidateQueries({ queryKey: ['bookings', 'me'] });
      const ref = data.bookingReference || data.reference || data.id || 'CONF';
      toast.success('Booking confirmed!');
      navigate(`/booking/confirmation/${ref}`);
    },
    onError: (err) => {
      toast.error(err.message || 'Booking failed. Please try again.');
    },
  });

  const onSubmit = (values) => {
    if (!checkoutData) return;

    // Payment data is NOT sent to backend (mock)
    const payload = {
      guest: {
        fullName: values.fullName,
        email: values.email,
        phone: values.phone,
      },
      roomTypeId: checkoutData.roomTypeId,
      numberOfRooms: checkoutData.numberOfRooms,
      checkInDate: checkoutData.checkInDate,
      checkOutDate: checkoutData.checkOutDate,
      numberOfGuests: checkoutData.numberOfGuests,
      specialRequests: values.specialRequests || '',
    };

    bookingMutation.mutate(payload);
  };

  if (!checkoutData) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <p className="text-gray-500">Loading checkout...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <button
          onClick={() => navigate(-1)}
          className="inline-flex items-center gap-2 text-sm text-gray-500 hover:text-gray-800 mb-6 transition-colors"
        >
          <ArrowLeft size={16} />
          Back
        </button>

        <h1 className="text-2xl font-bold text-gray-900 mb-8">Complete Your Booking</h1>

        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Left: Forms */}
            <div className="lg:col-span-2 space-y-6">
              {/* Guest info */}
              <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
                <div className="flex items-center gap-2 mb-5">
                  <User size={20} className="text-amber-500" />
                  <h2 className="text-lg font-semibold text-gray-900">Guest Information</h2>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <Input
                    label="Full Name"
                    icon={User}
                    placeholder="John Doe"
                    required
                    {...register('fullName')}
                    error={errors.fullName?.message}
                  />
                  <Input
                    label="Email Address"
                    type="email"
                    icon={Mail}
                    placeholder="john@example.com"
                    required
                    {...register('email')}
                    error={errors.email?.message}
                  />
                  <Input
                    label="Phone Number"
                    type="tel"
                    icon={Phone}
                    placeholder="+1 234 567 8900"
                    required
                    {...register('phone')}
                    error={errors.phone?.message}
                  />
                </div>

                <div className="mt-4">
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Special Requests
                    <span className="text-gray-400 font-normal ml-1">(optional)</span>
                  </label>
                  <div className="relative">
                    <FileText
                      size={16}
                      className="absolute left-3 top-3 text-gray-400 pointer-events-none"
                    />
                    <textarea
                      placeholder="Any special requests or preferences..."
                      rows={3}
                      className="w-full pl-9 pr-3 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 resize-none"
                      {...register('specialRequests')}
                    />
                  </div>
                </div>
              </div>

              {/* Payment (mock) */}
              <div className="bg-white rounded-2xl p-6 shadow-sm border border-gray-100">
                <div className="flex items-center gap-2 mb-2">
                  <CreditCard size={20} className="text-amber-500" />
                  <h2 className="text-lg font-semibold text-gray-900">Payment Details</h2>
                </div>
                <div className="flex items-center gap-1.5 text-xs text-gray-500 mb-5">
                  <Lock size={12} />
                  <span>Your payment info is encrypted and secure. This is a demo — no real charge.</span>
                </div>

                {/* Mock card UI */}
                <div className="bg-gradient-to-br from-gray-800 to-gray-900 rounded-2xl p-5 text-white mb-5">
                  <div className="flex justify-between items-start mb-8">
                    <div className="text-xs tracking-widest" style={{ fontFamily: "'Playfair Display', Georgia, serif", color: '#C9A84C', letterSpacing: '0.15em' }}>VELOUR</div>
                    <CreditCard size={28} className="text-amber-400" />
                  </div>
                  <div className="font-mono text-lg tracking-widest mb-4 min-h-[28px]">
                    {cardNumberRaw || '•••• •••• •••• ••••'}
                  </div>
                  <div className="flex justify-between text-sm">
                    <div>
                      <div className="text-xs text-gray-400 uppercase">Card Holder</div>
                      <div className="font-medium">{watch('cardName') || 'YOUR NAME'}</div>
                    </div>
                    <div>
                      <div className="text-xs text-gray-400 uppercase">Expires</div>
                      <div className="font-medium">{watch('cardExpiry') || 'MM/YY'}</div>
                    </div>
                  </div>
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="sm:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Card Number <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      inputMode="numeric"
                      placeholder="1234 5678 9012 3456"
                      maxLength={19}
                      className={`w-full px-3 py-2.5 border rounded-lg text-sm font-mono focus:outline-none focus:ring-2 focus:ring-amber-500 ${
                        errors.cardNumber ? 'border-red-400' : 'border-gray-300'
                      }`}
                      {...register('cardNumber', {
                        onChange: (e) => {
                          const formatted = formatCardNumber(e.target.value);
                          setValue('cardNumber', formatted);
                        },
                      })}
                    />
                    {errors.cardNumber && (
                      <p className="text-xs text-red-500 mt-0.5">{errors.cardNumber.message}</p>
                    )}
                  </div>

                  <Input
                    label="Cardholder Name"
                    placeholder="John Doe"
                    required
                    {...register('cardName')}
                    error={errors.cardName?.message}
                  />

                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Expiry <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="text"
                        placeholder="MM/YY"
                        maxLength={5}
                        className={`w-full px-3 py-2.5 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 ${
                          errors.cardExpiry ? 'border-red-400' : 'border-gray-300'
                        }`}
                        {...register('cardExpiry', {
                          onChange: (e) => {
                            setValue('cardExpiry', formatExpiry(e.target.value));
                          },
                        })}
                      />
                      {errors.cardExpiry && (
                        <p className="text-xs text-red-500 mt-0.5">{errors.cardExpiry.message}</p>
                      )}
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        CVV <span className="text-red-500">*</span>
                      </label>
                      <input
                        type="password"
                        placeholder="•••"
                        maxLength={4}
                        className={`w-full px-3 py-2.5 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 ${
                          errors.cardCvv ? 'border-red-400' : 'border-gray-300'
                        }`}
                        {...register('cardCvv')}
                      />
                      {errors.cardCvv && (
                        <p className="text-xs text-red-500 mt-0.5">{errors.cardCvv.message}</p>
                      )}
                    </div>
                  </div>
                </div>
              </div>

              {/* Submit */}
              <Button
                type="submit"
                variant="primary"
                size="lg"
                className="w-full"
                loading={bookingMutation.isPending}
                leftIcon={<Lock size={18} />}
              >
                Confirm & Pay {checkoutData.totalPrice ? `$${Number(checkoutData.totalPrice).toFixed(2)}` : ''}
              </Button>
            </div>

            {/* Right: Summary */}
            <div className="lg:col-span-1">
              <div className="sticky top-24">
                <BookingSummary booking={checkoutData} />
              </div>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}

export default CheckoutPage;
