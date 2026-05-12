import { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation } from '@tanstack/react-query';
import { User, Mail, Lock, AtSign, ArrowLeft } from 'lucide-react';
import toast from 'react-hot-toast';
import { register as registerUser } from '../../api/authApi';
import { useAuth } from '../../hooks/useAuth';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';

const schema = z
  .object({
    fullName: z.string().min(2, 'Full name must be at least 2 characters'),
    username: z
      .string()
      .min(3, 'Username must be at least 3 characters')
      .max(30, 'Username too long')
      .regex(/^[a-zA-Z0-9_]+$/, 'Only letters, numbers, and underscores'),
    email: z.string().email('Please enter a valid email address'),
    password: z
      .string()
      .min(8, 'Password must be at least 8 characters'),
    confirmPassword: z.string(),
  })
  .refine((d) => d.password === d.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  });

function RegisterPage() {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    if (isAuthenticated) navigate('/', { replace: true });
  }, [isAuthenticated, navigate]);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: zodResolver(schema) });

  const mutation = useMutation({
    mutationFn: registerUser,
    onSuccess: () => {
      toast.success('Account created! Please sign in.');
      navigate('/login');
    },
    onError: (err) => {
      toast.error(err.message || 'Registration failed. Please try again.');
    },
  });

  const onSubmit = (values) => {
    mutation.mutate({
      fullName: values.fullName,
      username: values.username,
      email: values.email,
      password: values.password,
    });
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-amber-50 via-white to-orange-50 flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-md">
        {/* Back to home */}
        <div className="mb-4">
          <Link
            to="/"
            className="inline-flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-800 transition-colors"
          >
            <ArrowLeft size={15} />
            Back to Home
          </Link>
        </div>

        {/* Logo */}
        <div className="text-center mb-8">
          <div className="mb-3">
            <span style={{ fontFamily: "'Playfair Display', Georgia, serif", fontWeight: 700, letterSpacing: '0.18em', color: '#C9A84C', fontSize: '2.2rem' }}>
              VELOUR
            </span>
            <p style={{ fontStyle: 'italic', fontSize: '0.75rem', letterSpacing: '0.06em', color: '#9ca3af', marginTop: '2px' }}>
              Redefine Your Stay
            </p>
          </div>
          <h1 className="text-3xl font-bold text-gray-900">Create an account</h1>
          <p className="text-gray-500 mt-1">Join VELOUR and start booking</p>
        </div>

        {/* Card */}
        <div className="bg-white rounded-3xl shadow-xl border border-gray-100 p-8">
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <Input
              label="Full Name"
              type="text"
              icon={User}
              placeholder="John Doe"
              autoComplete="name"
              required
              {...register('fullName')}
              error={errors.fullName?.message}
            />

            <Input
              label="Username"
              type="text"
              icon={AtSign}
              placeholder="johndoe"
              autoComplete="username"
              required
              {...register('username')}
              error={errors.username?.message}
            />

            <Input
              label="Email Address"
              type="email"
              icon={Mail}
              placeholder="john@example.com"
              autoComplete="email"
              required
              {...register('email')}
              error={errors.email?.message}
            />

            <Input
              label="Password"
              type="password"
              icon={Lock}
              placeholder="At least 8 characters"
              autoComplete="new-password"
              required
              {...register('password')}
              error={errors.password?.message}
            />

            <Input
              label="Confirm Password"
              type="password"
              icon={Lock}
              placeholder="Repeat your password"
              autoComplete="new-password"
              required
              {...register('confirmPassword')}
              error={errors.confirmPassword?.message}
            />

            <Button
              type="submit"
              variant="primary"
              size="lg"
              className="w-full mt-2"
              loading={mutation.isPending}
            >
              Create Account
            </Button>
          </form>

          <p className="text-center text-sm text-gray-500 mt-6">
            Already have an account?{' '}
            <Link
              to="/login"
              className="text-amber-600 font-semibold hover:text-amber-700 transition-colors"
            >
              Sign in
            </Link>
          </p>
        </div>

        <p className="text-center text-xs text-gray-400 mt-6">
          By creating an account you agree to our{' '}
          <span className="underline cursor-pointer">Terms of Service</span> and{' '}
          <span className="underline cursor-pointer">Privacy Policy</span>.
        </p>
      </div>
    </div>
  );
}

export default RegisterPage;
