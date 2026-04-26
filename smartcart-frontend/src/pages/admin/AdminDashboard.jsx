import { useState, useEffect } from 'react';
import { Users, Package, ShoppingBag, DollarSign, TrendingUp, Clock } from 'lucide-react';
import { adminAPI } from '../../api/services';
import { LoadingSpinner } from '../../components/Shared';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchStats = () => {
    setLoading(true);
    setError(null);
    adminAPI.getDashboard()
      .then(r => setStats(r.data.data))
      .catch(err => {
        console.error('Dashboard Load Error:', err);
        const errorMessage = err.response?.data?.message || err.message || 'Failed to load dashboard data. Please ensure the backend is running.';
        setError(errorMessage);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchStats();
  }, []);

  const formatPrice = (p) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(p || 0);

  if (loading) return <div className="flex items-center justify-center py-20"><LoadingSpinner size="lg" /></div>;
  if (error) return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <div className="text-red-500 bg-red-50 p-6 rounded-2xl border border-red-100 max-w-md">
        <TrendingUp className="w-12 h-12 mx-auto mb-4 opacity-20" />
        <h3 className="text-lg font-bold mb-2">Something went wrong</h3>
        <p className="text-sm opacity-80">{error}</p>
        <button onClick={fetchStats} className="mt-4 btn-primary py-2 px-6">Retry</button>
      </div>
    </div>
  );
  if (!stats) return null;

  const statCards = [
    { label: 'Total Revenue', value: formatPrice(stats.totalRevenue), icon: DollarSign, color: 'from-emerald-500 to-teal-600', change: '+12.5%' },
    { label: 'Total Orders', value: stats.totalOrders, icon: ShoppingBag, color: 'from-blue-500 to-indigo-600', change: '+8.2%' },
    { label: 'Total Users', value: stats.totalUsers, icon: Users, color: 'from-purple-500 to-violet-600', change: '+24.1%' },
    { label: 'Total Products', value: stats.totalProducts, icon: Package, color: 'from-orange-500 to-red-500', change: '+5.7%' },
  ];

  const orderStatusData = [
    { name: 'Pending', value: stats.pendingOrders || 0 },
    { name: 'Delivered', value: stats.deliveredOrders || 0 },
    { name: 'Other', value: Math.max(0, (stats.totalOrders || 0) - (stats.pendingOrders || 0) - (stats.deliveredOrders || 0)) },
  ].filter(d => d.value > 0);

  const PIE_COLORS = ['#f59e0b', '#10b981', '#6366f1'];

  const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  const revenueData = stats.dailyRevenue
    ?.filter(d => d && d.day && d.value !== undefined)
    .map(d => {
      const dateObj = new Date(d.day + 'T00:00:00');
      const dayIndex = dateObj.getDay();
      return {
        name: isNaN(dayIndex) ? '???' : days[dayIndex],
        value: d.value
      };
    }) || [];

  const statusColors = {
    PENDING: 'badge-warning', CONFIRMED: 'badge-info', PROCESSING: 'badge-info',
    SHIPPED: 'badge-info', DELIVERED: 'badge-success', CANCELLED: 'badge-danger',
  };

  return (
    <div className="animate-fade-in space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-sm text-gray-500 mt-1">Welcome back! Here's your store overview.</p>
      </div>

      {/* Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {statCards.map((s, i) => (
          <div key={i} className="card-flat p-5 relative overflow-hidden">
            <div className={`absolute top-0 right-0 w-20 h-20 bg-gradient-to-br ${s.color} opacity-10 rounded-bl-[3rem]`} />
            <div className={`w-10 h-10 rounded-xl bg-gradient-to-br ${s.color} flex items-center justify-center mb-3`}>
              <s.icon className="w-5 h-5 text-white" />
            </div>
            <p className="text-sm text-gray-500">{s.label}</p>
            <p className="text-2xl font-bold text-gray-900 mt-1">{s.value}</p>
            <p className="text-xs text-green-600 font-medium mt-2 flex items-center gap-1">
              <TrendingUp className="w-3 h-3" /> {s.change} this month
            </p>
          </div>
        ))}
      </div>

      {/* Charts Row */}
      <div className="grid lg:grid-cols-3 gap-6">
        {/* Revenue Chart */}
        <div className="lg:col-span-2 card-flat p-6">
          <h3 className="text-lg font-bold text-gray-900 mb-4">Revenue Overview</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={revenueData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
              <XAxis dataKey="name" tick={{ fontSize: 12, fill: '#94a3b8' }} />
              <YAxis tick={{ fontSize: 12, fill: '#94a3b8' }} />
              <Tooltip 
                formatter={(v) => formatPrice(v)}
                contentStyle={{ borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }} />
              <Bar dataKey="value" fill="#6366f1" radius={[6, 6, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Order Status Pie */}
        <div className="card-flat p-6">
          <h3 className="text-lg font-bold text-gray-900 mb-4">Order Status</h3>
          {orderStatusData.length > 0 ? (
            <ResponsiveContainer width="100%" height={250}>
              <PieChart>
                <Pie data={orderStatusData} cx="50%" cy="50%" innerRadius={60} outerRadius={90}
                  paddingAngle={5} dataKey="value">
                  {orderStatusData.map((_, i) => <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />)}
                </Pie>
                <Tooltip contentStyle={{ borderRadius: '12px' }} />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <p className="text-sm text-gray-500 text-center py-16">No order data yet</p>
          )}
          <div className="flex justify-center gap-4 mt-2">
            {orderStatusData.map((d, i) => (
              <div key={d.name} className="flex items-center gap-2 text-xs">
                <div className="w-3 h-3 rounded-full" style={{ backgroundColor: PIE_COLORS[i] }} />
                {d.name}: {d.value}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Recent Orders */}
      <div className="card-flat p-6">
        <h3 className="text-lg font-bold text-gray-900 mb-4 flex items-center gap-2">
          <Clock className="w-5 h-5 text-gray-400" /> Recent Orders
        </h3>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-gray-500 border-b border-gray-100">
                <th className="pb-3 font-medium">Order ID</th>
                <th className="pb-3 font-medium">Customer</th>
                <th className="pb-3 font-medium">Amount</th>
                <th className="pb-3 font-medium">Status</th>
                <th className="pb-3 font-medium">Date</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {stats.recentOrders?.map(order => (
                <tr key={order.id} className="hover:bg-gray-50">
                  <td className="py-3 font-medium">{order.orderNumber}</td>
                  <td className="py-3 text-gray-600">{order.userName}</td>
                  <td className="py-3 font-semibold">{formatPrice(order.totalAmount)}</td>
                  <td className="py-3"><span className={statusColors[order.status]}>{order.status}</span></td>
                  <td className="py-3 text-gray-500">{new Date(order.createdAt).toLocaleDateString()}</td>
                </tr>
              ))}
              {(!stats.recentOrders || stats.recentOrders.length === 0) && (
                <tr><td colSpan={5} className="py-8 text-center text-gray-400">No orders yet</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
