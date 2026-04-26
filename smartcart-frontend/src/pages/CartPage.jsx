import { Link } from 'react-router-dom';
import { Minus, Plus, Trash2, ShoppingBag, ArrowRight } from 'lucide-react';
import { useCart } from '../context/CartContext';
import { EmptyState, LoadingSpinner } from '../components/Shared';

export default function CartPage() {
  const { cart, loading, updateQuantity, removeItem } = useCart();

  const formatPrice = (p) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(p);

  if (loading) return <div className="min-h-[60vh] flex items-center justify-center"><LoadingSpinner size="lg" /></div>;

  if (!cart.items || cart.items.length === 0) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-16">
        <EmptyState icon={ShoppingBag} title="Your cart is empty"
          description="Looks like you haven't added anything to your cart yet. Start shopping!"
          action={<Link to="/products" className="btn-primary">Browse Products</Link>} />
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in">
      <h1 className="text-2xl font-bold text-gray-900 mb-8">Shopping Cart ({cart.totalItems} items)</h1>

      <div className="grid lg:grid-cols-3 gap-8">
        {/* Items */}
        <div className="lg:col-span-2 space-y-4">
          {cart.items.map(item => (
            <div key={item.id} className="card-flat p-4 sm:p-6 flex gap-4">
              <Link to={`/products/${item.productId}`} className="w-24 h-24 sm:w-32 sm:h-32 rounded-xl overflow-hidden bg-gray-100 shrink-0">
                <img src={item.productImageUrl} alt={item.productName} className="w-full h-full object-cover" />
              </Link>
              <div className="flex-1 min-w-0">
                <Link to={`/products/${item.productId}`} className="text-sm sm:text-base font-semibold text-gray-900 hover:text-primary-600 line-clamp-2">
                  {item.productName}
                </Link>
                <div className="flex items-baseline gap-2 mt-1">
                  <span className="font-bold text-gray-900">{formatPrice(item.unitPrice)}</span>
                  {item.productDiscountPrice && item.productDiscountPrice < item.productPrice && (
                    <span className="text-sm text-gray-400 line-through">{formatPrice(item.productPrice)}</span>
                  )}
                </div>
                <div className="flex items-center justify-between mt-3">
                  <div className="flex items-center border border-gray-200 rounded-lg">
                    <button onClick={() => updateQuantity(item.id, item.quantity - 1)} disabled={item.quantity <= 1}
                      className="p-1.5 hover:bg-gray-50 disabled:opacity-30"><Minus className="w-4 h-4" /></button>
                    <span className="w-10 text-center text-sm font-semibold">{item.quantity}</span>
                    <button onClick={() => updateQuantity(item.id, item.quantity + 1)} disabled={item.quantity >= item.productStock}
                      className="p-1.5 hover:bg-gray-50 disabled:opacity-30"><Plus className="w-4 h-4" /></button>
                  </div>
                  <button onClick={() => removeItem(item.id)}
                    className="p-2 text-red-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors">
                    <Trash2 className="w-5 h-5" />
                  </button>
                </div>
              </div>
              <div className="text-right hidden sm:block">
                <p className="text-lg font-bold text-gray-900">{formatPrice(item.subtotal)}</p>
              </div>
            </div>
          ))}
        </div>

        {/* Summary */}
        <div>
          <div className="card-flat p-6 sticky top-24">
            <h3 className="text-lg font-bold text-gray-900 mb-4">Order Summary</h3>
            <div className="space-y-3 mb-6">
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">Subtotal ({cart.totalItems} items)</span>
                <span className="font-medium">{formatPrice(cart.totalPrice)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">Delivery</span>
                <span className="text-green-600 font-medium">FREE</span>
              </div>
              <hr />
              <div className="flex justify-between">
                <span className="font-bold text-gray-900">Total</span>
                <span className="text-xl font-bold text-gray-900">{formatPrice(cart.totalPrice)}</span>
              </div>
            </div>
            <Link to="/checkout" className="btn-primary w-full flex items-center justify-center gap-2 !py-3.5">
              Proceed to Checkout <ArrowRight className="w-5 h-5" />
            </Link>
            <Link to="/products" className="block text-center text-sm text-primary-600 font-medium mt-4 hover:text-primary-700">
              Continue Shopping
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
