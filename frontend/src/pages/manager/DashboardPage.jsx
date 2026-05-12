import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import {
  Hotel,
  CalendarCheck,
  DollarSign,
  TrendingUp,
  Plus,
  BedDouble,
  ArrowRight,
  Clock,
  CheckCircle,
  XCircle,
  AlertCircle,
} from 'lucide-react';
import { getManagerHotels } from '../../api/hotelsApi';
import { getHotelStatistics } from '../../api/bookingsApi';
import { QUERY_KEYS } from '../../utils/queryKeys';
import { formatCurrency } from '../../utils/dateUtils';
import Skeleton from '../../components/ui/Skeleton';
import Button from '../../components/ui/Button';
import { useAuth } from '../../hooks/useAuth';

// ─── Stat Card ────────────────────────────────────────────────────────────────

function StatCard({ icon: Icon, label, value, color = 'amber', sub }) {
  const colorMap = {
    amber: 'bg-amber-50 text-amber-600',
    blue: 'bg-blue-50 text-blue-600',
    green: 'bg-green-50 text-green-600',
    red: 'bg-red-50 text-red-600',
    purple: 'bg-purple-50 text-purple-600',
  };

  return (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
      <div className="flex items-center justify-between mb-3">
        <span className="text-sm font-medium text-gray-500">{label}</span>
        <div className={`w-9 h-9 rounded-xl flex items-center justify-center ${colorMap[color]}`}>
          <Icon size={18} />
        </div>
      </div>
      <p className="text-2xl font-bold text-gray-900">{value}</p>
      {sub && <p className="text-xs text-gray-400 mt-1">{sub}</p>}
    </div>
  );
}

// ─── Hotel Stats Card ─────────────────────────────────────────────────────────

function HotelStatsCard({ hotel }) {
  const { data: stats, isLoading } = useQuery({
    queryKey: QUERY_KEYS.HOTEL_STATS(hotel.id),
    queryFn: () => getHotelStatistics(hotel.id),
    staleTime: 60_000,
  });

  return (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
      {/* Hotel header */}
      <div className="bg-gradient-to-r from-gray-800 to-gray-700 px-5 py-4">
        <div className="flex items-start justify-between gap-2">
          <div className="min-w-0">
            <h3 className="font-semibold text-white truncate">{hotel.name}</h3>
            <p className="text-xs text-gray-400 mt-0.5">{hotel.city}</p>
          </div>
          <div className="flex-shrink-0 flex items-center gap-1">
            {Array.from({ length: hotel.starRating ?? 0 }).map((_, i) => (
              <span key={i} className="text-amber-400 text-xs">★</span>
            ))}
          </div>
        </div>
      </div>

      {/* Stats grid */}
      <div className="grid grid-cols-2 divide-x divide-y divide-gray-100">
        <div className="px-4 py-3">
          <p className="text-xs text-gray-400 mb-0.5">Total Bookings</p>
          {isLoading ? (
            <Skeleton className="h-5 w-12" />
          ) : (
            <p className="font-bold text-gray-900">{stats?.totalBookings ?? '—'}</p>
          )}
        </div>
        <div className="px-4 py-3">
          <p className="text-xs text-gray-400 mb-0.5">Revenue</p>
          {isLoading ? (
            <Skeleton className="h-5 w-20" />
          ) : (
            <p className="font-bold text-amber-600">{formatCurrency(stats?.totalRevenue ?? 0)}</p>
          )}
        </div>
        <div className="px-4 py-3">
          <p className="text-xs text-gray-400 mb-0.5">Pending</p>
          {isLoading ? (
            <Skeleton className="h-5 w-8" />
          ) : (
            <p className="font-bold text-orange-500">{stats?.pendingBookings ?? '—'}</p>
          )}
        </div>
        <div className="px-4 py-3">
          <p className="text-xs text-gray-400 mb-0.5">Confirmed</p>
          {isLoading ? (
            <Skeleton className="h-5 w-8" />
          ) : (
            <p className="font-bold text-green-600">{stats?.confirmedBookings ?? '—'}</p>
          )}
        </div>
      </div>

      {/* Actions */}
      <div className="px-5 py-3 bg-gray-50 border-t border-gray-100 flex items-center gap-2">
        <Link to={`/manager/hotels/${hotel.id}/rooms`} className="flex-1">
          <Button variant="secondary" size="sm" className="w-full" leftIcon={<BedDouble size={13} />}>
            Rooms
          </Button>
        </Link>
        <Link to={`/manager/bookings`} className="flex-1">
          <Button variant="secondary" size="sm" className="w-full" leftIcon={<CalendarCheck size={13} />}>
            Bookings
          </Button>
        </Link>
        <Link to={`/manager/hotels/${hotel.id}/edit`}>
          <Button variant="ghost" size="sm">Edit</Button>
        </Link>
      </div>
    </div>
  );
}

// ─── Skeleton for hotel stats card ───────────────────────────────────────────

function HotelStatsCardSkeleton() {
  return (
    <div className="bg-white rounded-2xl border border-gray-100 shadow-sm overflow-hidden">
      <div className="bg-gray-200 px-5 py-4 h-16" />
      <div className="grid grid-cols-2 divide-x divide-y divide-gray-100">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="px-4 py-3 space-y-1">
            <Skeleton className="h-3 w-16" />
            <Skeleton className="h-5 w-12" />
          </div>
        ))}
      </div>
      <div className="px-5 py-3 bg-gray-50 border-t border-gray-100 flex gap-2">
        <Skeleton className="h-8 flex-1 rounded-lg" />
        <Skeleton className="h-8 flex-1 rounded-lg" />
        <Skeleton className="h-8 w-12 rounded-lg" />
      </div>
    </div>
  );
}

// ─── Main Page ────────────────────────────────────────────────────────────────

function DashboardPage() {
  const { user } = useAuth();

  const { data: hotels = [], isLoading, isError } = useQuery({
    queryKey: QUERY_KEYS.MANAGER_HOTELS,
    queryFn: getManagerHotels,
    staleTime: 30_000,
  });

  const greeting = (() => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 17) return 'Good afternoon';
    return 'Good evening';
  })();

  return (
    <div className="max-w-6xl mx-auto space-y-8">
      {/* Header */}
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            {greeting}, {user?.fullName?.split(' ')[0] || 'Manager'}
          </h1>
          <p className="text-gray-500 mt-0.5">
            {isLoading ? '...' : `You manage ${hotels.length} ${hotels.length === 1 ? 'hotel' : 'hotels'}`}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Link to="/manager/hotels/new">
            <Button variant="primary" leftIcon={<Plus size={16} />}>
              Add Hotel
            </Button>
          </Link>
        </div>
      </div>

      {/* Top-level stat cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          icon={Hotel}
          label="My Hotels"
          value={isLoading ? '—' : hotels.length}
          color="amber"
          sub="properties managed"
        />
        <StatCard
          icon={CalendarCheck}
          label="Bookings"
          value="—"
          color="blue"
          sub="this month"
        />
        <StatCard
          icon={DollarSign}
          label="Revenue"
          value="—"
          color="green"
          sub="this month"
        />
        <StatCard
          icon={TrendingUp}
          label="Occupancy"
          value="—"
          color="purple"
          sub="average rate"
        />
      </div>

      {/* Quick actions */}
      <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-5">
        <h2 className="font-semibold text-gray-900 mb-4">Quick Actions</h2>
        <div className="flex flex-wrap gap-3">
          <Link to="/manager/hotels/new">
            <Button variant="secondary" size="sm" leftIcon={<Plus size={14} />}>
              New Hotel
            </Button>
          </Link>
          <Link to="/manager/bookings">
            <Button variant="secondary" size="sm" leftIcon={<CalendarCheck size={14} />}>
              View All Bookings
            </Button>
          </Link>
          <Link to="/manager/hotels">
            <Button variant="secondary" size="sm" leftIcon={<Hotel size={14} />}>
              Manage Hotels
            </Button>
          </Link>
        </div>
      </div>

      {/* Status legend */}
      <div className="flex flex-wrap items-center gap-4 text-xs text-gray-500">
        <div className="flex items-center gap-1.5"><Clock size={12} className="text-orange-400" /> Pending — awaiting confirmation</div>
        <div className="flex items-center gap-1.5"><CheckCircle size={12} className="text-green-500" /> Confirmed — upcoming stays</div>
        <div className="flex items-center gap-1.5"><AlertCircle size={12} className="text-blue-400" /> Completed — past stays</div>
        <div className="flex items-center gap-1.5"><XCircle size={12} className="text-red-400" /> Cancelled</div>
      </div>

      {/* Hotel cards */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-semibold text-gray-900 text-lg">Your Hotels</h2>
          <Link
            to="/manager/hotels"
            className="flex items-center gap-1 text-sm text-amber-600 hover:text-amber-700 font-medium"
          >
            Manage all <ArrowRight size={14} />
          </Link>
        </div>

        {isError ? (
          <div className="text-center py-12 text-red-500">
            Failed to load hotels. Please refresh.
          </div>
        ) : isLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
            {[1, 2, 3].map((i) => <HotelStatsCardSkeleton key={i} />)}
          </div>
        ) : hotels.length === 0 ? (
          <div className="text-center py-16 bg-white rounded-2xl border border-gray-100">
            <Hotel size={48} className="mx-auto text-gray-200 mb-3" />
            <p className="text-gray-500 mb-4">No hotels yet. Add your first property.</p>
            <Link to="/manager/hotels/new">
              <Button variant="primary" leftIcon={<Plus size={16} />}>Add Hotel</Button>
            </Link>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
            {hotels.map((hotel) => (
              <HotelStatsCard key={hotel.id} hotel={hotel} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default DashboardPage;
