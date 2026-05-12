import { useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMutation } from '@tanstack/react-query';
import { Mail, Lock, ArrowLeft } from 'lucide-react';
import toast from 'react-hot-toast';
import { login } from '../../api/authApi';
import { useAuth } from '../../hooks/useAuth';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';

const schema = z.object({
  email: z.string().min(1, 'Email or username is required'),
  password: z.string().min(1, 'Password is required'),
});

function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login: storeLogin, isAuthenticated, isManager } = useAuth();

  const from = location.state?.from?.pathname || '/';

  useEffect(() => {
    if (isAuthenticated) {
      navigate(isManager ? '/manager/dashboard' : from, { replace: true });
    }
  }, [isAuthenticated, isManager, navigate, from]);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({ resolver: zodResolver(schema) });

  const mutation = useMutation({
    mutationFn: login,
    onSuccess: (data) => {
      const token = data.accessToken || data.token;
      const user = {
        id: data.id,
        username: data.username,
        email: data.email,
        fullName: data.fullName,
        roles: data.roles ?? [],
        hotelId: data.hotelId ?? null,
      };
      storeLogin(user, token);
      toast.success(`Welcome back, ${user.fullName || user.username}!`);
      const roles = user.roles ?? [];
      const isManagerRole =
        roles.some((r) => ['ROLE_HOTEL_MANAGER', 'HOTEL_MANAGER', 'ROLE_ADMIN', 'ADMIN'].includes(r));
      navigate(isManagerRole ? '/manager/dashboard' : from, { replace: true });
    },
    onError: (err) => {
      toast.error(err.message || 'Invalid credentials. Please try again.');
    },
  });

  const onSubmit = (values) => {
    mutation.mutate({ usernameOrEmail: values.email, password: values.password });
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-amber-50 via-white to-orange-50 flex items-center justify-center px-4">
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
          <h1 className="text-3xl font-bold text-gray-900">Welcome back</h1>
          <p className="text-gray-500 mt-1">Sign in to your VELOUR account</p>
        </div>

        {/* Card */}
        <div className="bg-white rounded-3xl shadow-xl border border-gray-100 p-8">
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
            <Input
              label="Email or Username"
              type="text"
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
              placeholder="••••••••"
              autoComplete="current-password"
              required
              {...register('password')}
              error={errors.password?.message}
            />

            <Button
              type="submit"
              variant="primary"
              size="lg"
              className="w-full mt-2"
              loading={mutation.isPending}
            >
              Sign In
            </Button>
          </form>

          <p className="text-center text-sm text-gray-500 mt-6">
            Don't have an account?{' '}
            <Link
              to="/register"
              className="text-amber-600 font-semibold hover:text-amber-700 transition-colors"
            >
              Create one
            </Link>
          </p>
        </div>

        <p className="text-center text-xs text-gray-400 mt-6">
          By signing in you agree to our{' '}
          <span className="underline cursor-pointer">Terms of Service</span> and{' '}
          <span className="underline cursor-pointer">Privacy Policy</span>.
        </p>
      </div>
    </div>
  );
}

export default LoginPage;
