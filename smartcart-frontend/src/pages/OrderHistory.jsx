import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { Package, ChevronRight } from 'lucide-react';
import { orderAPI } from '../api/services';
import { LoadingSpinner, EmptyState } from '../components/Shared';
import SafeImage from '../components/SafeImage';

const statusColors = {
  PENDING: 'badge-warning', CONFIRMED: 'badge-info', PROCESSING: 'badge-info',
  SHIPPED: 'badge-info', DELIVERED: 'badge-success', CANCELLED: 'badge-danger',
};

export default function OrderHistory() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandedId, setExpandedId] = useState(null);
  const [cancelLoadingId, setCancelLoadingId] = useState(null);

  const fetchOrders = () => {
    orderAPI.getAll()
      .then(r => setOrders(r.data.data))
      .catch((err) => {
        console.error("Fetch orders failed:", err);
        // Silently fail unless it's a critical error to avoid annoying repeated alerts
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const handleCancelOrder = async (orderId) => {
    if (!window.confirm("Are you sure you want to cancel this order? If you paid, a refund will be issued via Razorpay.")) return;
    
    setCancelLoadingId(orderId);
    try {
      const response = await orderAPI.cancelOrder(orderId);
      const order = response.data.data;
      
      if (order.payment?.refundStatus === 'FAILED') {
        alert('Order cancelled successfully, but the automatic refund failed. Our administrator will process your refund manually.');
      } else {
        alert('Order cancelled and refund processed successfully.');
      }
      fetchOrders();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to cancel the order. Please try again.');
    } finally {
      setCancelLoadingId(null);
    }
  };

  const formatPrice = (p) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(p);
  const formatDate = (d) => new Date(d).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });

  if (loading) return <div className="min-h-[60vh] flex items-center justify-center"><LoadingSpinner size="lg" /></div>;

  if (orders.length === 0) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-16">
        <EmptyState icon={Package} title="No orders yet"
          description="You haven't placed any orders yet. Start shopping!"
          action={<Link to="/products" className="btn-primary">Browse Products</Link>} />
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in">
      <h1 className="text-2xl font-bold text-gray-900 mb-8">My Orders</h1>

      <div className="space-y-4">
        {orders.map(order => (
          <div key={order.id} className="card-flat overflow-hidden">
            <button onClick={() => setExpandedId(expandedId === order.id ? null : order.id)}
              className="w-full p-4 sm:p-6 flex items-center justify-between hover:bg-gray-50 transition-colors">
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-xl bg-primary-50 flex items-center justify-center shrink-0">
                  <Package className="w-6 h-6 text-primary-600" />
                </div>
                <div className="text-left">
                  <p className="text-sm font-bold text-gray-900">{order.orderNumber}</p>
                  <p className="text-xs text-gray-500">{formatDate(order.createdAt)} • {order.items?.length} items</p>
                </div>
              </div>
              <div className="flex items-center gap-4">
                <span className={statusColors[order.status]}>{order.status}</span>
                <span className="text-lg font-bold text-gray-900 hidden sm:block">{formatPrice(order.totalAmount)}</span>
                <ChevronRight className={`w-5 h-5 text-gray-400 transition-transform ${expandedId === order.id ? 'rotate-90' : ''}`} />
              </div>
            </button>

            {expandedId === order.id && (
              <div className="border-t border-gray-100 p-4 sm:p-6 bg-gray-50/50 animate-slide-down">
                <div className="space-y-3 mb-4">
                  {order.items?.map(item => (
                    <div key={item.id} className="flex items-center gap-3">
                      <SafeImage src={item.productImageUrl} alt="" className="w-14 h-14 rounded-lg object-cover" />
                      <div className="flex-1">
                        <p className="text-sm font-medium">{item.productName}</p>
                        <p className="text-xs text-gray-500">Qty: {item.quantity} × {formatPrice(item.unitPrice)}</p>
                      </div>
                      <p className="text-sm font-bold">{formatPrice(item.subtotal)}</p>
                    </div>
                  ))}
                </div>
                <hr className="mb-4" />
                <div className="grid sm:grid-cols-2 gap-4 text-sm">
                  <div>
                    <p className="text-gray-500 mb-1">Shipping Address</p>
                    <p className="font-medium">{order.shippingAddress}, {order.shippingCity}</p>
                    <p>{order.shippingState} {order.shippingZipCode}</p>
                  </div>
                  <div>
                    <p className="text-gray-500 mb-1">Payment</p>
                    {order.payment && (
                      <>
                        <p className="font-medium">{order.payment.method?.replace(/_/g, ' ')}</p>
                        <p className="text-xs text-gray-500">ID: {order.payment.transactionId}</p>
                      </>
                    )}
                    <p className="text-lg font-bold mt-2">{formatPrice(order.totalAmount)}</p>
                    
                    {(order.status === 'PENDING' || order.status === 'CONFIRMED' || order.status === 'PROCESSING') && (
                      <button
                        onClick={() => handleCancelOrder(order.id)}
                        disabled={cancelLoadingId === order.id}
                        className="mt-4 px-4 py-2 bg-red-600 hover:bg-red-700 text-white text-sm font-medium rounded-lg disabled:opacity-50 transition-colors"
                      >
                        {cancelLoadingId === order.id ? 'Processing Refund...' : 'Cancel Order'}
                      </button>
                    )}
                  </div>
                </div>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
