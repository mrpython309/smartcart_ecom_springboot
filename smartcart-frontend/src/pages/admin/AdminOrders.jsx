import { useState, useEffect } from 'react';
import { adminAPI } from '../../api/services';
import { LoadingSpinner } from '../../components/Shared';
import toast from 'react-hot-toast';

const statusColors = {
  PENDING: 'badge-warning', CONFIRMED: 'badge-info', PROCESSING: 'badge-info',
  SHIPPED: 'bg-blue-100 text-blue-800', DELIVERED: 'badge-success', CANCELLED: 'badge-danger',
};
const statuses = ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'];

export default function AdminOrders() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchOrders = () => {
    setLoading(true);
    adminAPI.getOrders({ page, size: 10, sortBy: 'createdAt', sortDir: 'desc' })
      .then(r => { setOrders(r.data.data.content); setTotalPages(r.data.data.totalPages); })
      .catch(() => {})
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchOrders(); }, [page]);

  const handleStatusChange = async (orderId, status) => {
    try {
      await adminAPI.updateOrderStatus(orderId, status);
      toast.success('Status updated');
      fetchOrders();
    } catch { toast.error('Failed to update'); }
  };

  const formatPrice = (p) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(p);

  return (
    <div className="animate-fade-in">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Orders</h1>

      {loading ? <LoadingSpinner /> : (
        <div className="card-flat overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 border-b border-gray-100">
                <tr className="text-left text-gray-500">
                  <th className="px-4 py-3 font-medium">Order</th>
                  <th className="px-4 py-3 font-medium">Customer</th>
                  <th className="px-4 py-3 font-medium">Amount</th>
                  <th className="px-4 py-3 font-medium">Status</th>
                  <th className="px-4 py-3 font-medium">Date</th>
                  <th className="px-4 py-3 font-medium">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {orders.map(order => (
                  <tr key={order.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 font-medium">{order.orderNumber}</td>
                    <td className="px-4 py-3">
                      <p className="text-gray-900">{order.userName}</p>
                      <p className="text-xs text-gray-500">{order.userEmail}</p>
                    </td>
                    <td className="px-4 py-3 font-semibold">{formatPrice(order.totalAmount)}</td>
                    <td className="px-4 py-3">
                      <span className={`badge ${statusColors[order.status] || 'badge-info'}`}>{order.status}</span>
                    </td>
                    <td className="px-4 py-3 text-gray-500">{new Date(order.createdAt).toLocaleDateString()}</td>
                    <td className="px-4 py-3">
                      <select value={order.status} onChange={e => handleStatusChange(order.id, e.target.value)}
                        className="input-field !py-1.5 !px-2 text-xs !w-auto">
                        {statuses.map(s => <option key={s} value={s}>{s}</option>)}
                      </select>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {orders.length === 0 && <p className="text-center py-10 text-gray-400">No orders yet</p>}
          {totalPages > 1 && (
            <div className="flex justify-center gap-2 p-4 border-t border-gray-100">
              <button disabled={page === 0} onClick={() => setPage(page - 1)} className="btn-secondary !py-1.5 !px-3 text-sm disabled:opacity-40">Prev</button>
              <span className="px-3 py-1.5 text-sm text-gray-500">Page {page + 1} of {totalPages}</span>
              <button disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)} className="btn-secondary !py-1.5 !px-3 text-sm disabled:opacity-40">Next</button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
