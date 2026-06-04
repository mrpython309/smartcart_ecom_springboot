import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { MapPin, CreditCard, Plus, CheckCircle } from 'lucide-react';
import { userAPI, orderAPI, paymentAPI } from '../api/services';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { LoadingSpinner } from '../components/Shared';
import SafeImage from '../components/SafeImage';
import toast from 'react-hot-toast';

export default function Checkout() {
  const navigate = useNavigate();
  const { cart, fetchCart } = useCart();
  const { user: authUser } = useAuth();
  const [addresses, setAddresses] = useState([]);
  const [selectedAddress, setSelectedAddress] = useState(null);
  const [paymentMethod, setPaymentMethod] = useState('RAZORPAY');
  const [loading, setLoading] = useState(true);
  const [placing, setPlacing] = useState(false);
  const [showAddAddress, setShowAddAddress] = useState(false);
  const [newAddress, setNewAddress] = useState({ street: '', city: '', state: '', zipCode: '', country: 'India', isDefault: false });
  const placingRef = useRef(false);

  useEffect(() => {
    userAPI.getAddresses().then(r => {
      setAddresses(r.data.data);
      const def = r.data.data.find(a => a.isDefault) || r.data.data[0];
      if (def) setSelectedAddress(def.id);
    }).catch(() => {}).finally(() => setLoading(false));
  }, []);

  const handleAddAddress = async (e) => {
    e.preventDefault();
    try {
      const res = await userAPI.addAddress(newAddress);
      setAddresses(prev => [...prev, res.data.data]);
      setSelectedAddress(res.data.data.id);
      setShowAddAddress(false);
      setNewAddress({ street: '', city: '', state: '', zipCode: '', country: 'India', isDefault: false });
      toast.success('Address added');
    } catch (err) {
      toast.error('Failed to add address');
    }
  };

  const handlePlaceOrder = async () => {
    if (placingRef.current) return;
    if (!selectedAddress) { toast.error('Please select a delivery address'); return; }
    placingRef.current = true;
    setPlacing(true);
    try {
      // 1. Place initial order (PENDING)
      const orderRes = await orderAPI.place({ addressId: selectedAddress, paymentMethod });
      const order = orderRes.data.data;
      
      if (paymentMethod !== 'RAZORPAY') {
        toast.success('Order placed successfully!');
        await fetchCart();
        navigate(`/orders`);
        return;
      }
      
      // 2. Create Razorpay Payment Order
      const paymentRes = await paymentAPI.createOrder({ orderId: order.id });
      
      if (!paymentRes.data.data) {
          toast.success('Free order confirmed directly!');
          await fetchCart();
          navigate('/orders');
          return;
      }
      
      const paymentOrder = paymentRes.data.data;
      
      // 3. Open Razorpay Popup
      const options = {
        key: paymentOrder.razorpayKeyId,
        amount: paymentOrder.amount * 100, // in paise
        currency: paymentOrder.currency,
        name: 'SmartCart',
        description: 'Order Payment: ' + paymentOrder.orderNumber,
        order_id: paymentOrder.razorpayOrderId,
        handler: async function (response) {
          try {
            // 4. Verify Payment after success
            await paymentAPI.verify({
               razorpayOrderId: response.razorpay_order_id,
               razorpayPaymentId: response.razorpay_payment_id,
               razorpaySignature: response.razorpay_signature,
               orderId: order.id
            });
            toast.success('Payment successful! Order confirmed.');
            await fetchCart();
            navigate('/orders');
          } catch (err) {
            toast.error('Payment verification failed.');
            navigate('/orders');
          }
        },
        prefill: {
          name: `${authUser?.firstName} ${authUser?.lastName}`,
          email: authUser?.email,
          contact: authUser?.phone || ''
        },
        theme: {
          color: '#4f46e5'
        }
      };
      
      const rzp = new window.Razorpay(options);
      rzp.on('payment.failed', function (response){
        setPlacing(false);
        toast.error('Payment failed. Try again from orders page.');
        navigate('/orders');
      });
      rzp.open();
      // Note: setPlacing(false) is handled in verify or payment.failed or the catch block
      
    } catch (err) {
      placingRef.current = false;
      setPlacing(false);
      toast.error(err.response?.data?.message || 'Failed to place order');
    }
  };

  const formatPrice = (p) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(p);

  const paymentMethods = [
    { value: 'RAZORPAY', label: 'Razorpay (Cards/UPI/NetBanking)', icon: '💳' },
    { value: 'CASH_ON_DELIVERY', label: 'Cash on Delivery', icon: '💵' },
  ];

  if (loading) return <div className="min-h-[60vh] flex items-center justify-center"><LoadingSpinner size="lg" /></div>;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in">
      <h1 className="text-2xl font-bold text-gray-900 mb-8">Checkout</h1>

      <div className="grid lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-6">
          {/* Delivery Address */}
          <div className="card-flat p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2">
                <MapPin className="w-5 h-5 text-primary-600" /> Delivery Address
              </h2>
              <button onClick={() => setShowAddAddress(!showAddAddress)}
                className="text-sm text-primary-600 font-medium flex items-center gap-1 hover:text-primary-700">
                <Plus className="w-4 h-4" /> Add New
              </button>
            </div>

            {showAddAddress && (
              <form onSubmit={handleAddAddress} className="grid grid-cols-2 gap-3 mb-6 p-4 bg-gray-50 rounded-xl">
                <input className="input-field col-span-2" placeholder="Street Address" required value={newAddress.street}
                  onChange={e => setNewAddress({...newAddress, street: e.target.value})} />
                <input className="input-field" placeholder="City" required value={newAddress.city}
                  onChange={e => setNewAddress({...newAddress, city: e.target.value})} />
                <input className="input-field" placeholder="State" required value={newAddress.state}
                  onChange={e => setNewAddress({...newAddress, state: e.target.value})} />
                <input className="input-field" placeholder="ZIP Code" required value={newAddress.zipCode}
                  onChange={e => setNewAddress({...newAddress, zipCode: e.target.value})} />
                <input className="input-field" placeholder="Country" required value={newAddress.country}
                  onChange={e => setNewAddress({...newAddress, country: e.target.value})} />
                <div className="col-span-2 flex gap-2">
                  <button type="submit" className="btn-primary text-sm">Save Address</button>
                  <button type="button" onClick={() => setShowAddAddress(false)} className="btn-secondary text-sm">Cancel</button>
                </div>
              </form>
            )}

            <div className="space-y-3">
              {addresses.length === 0 ? (
                <p className="text-sm text-gray-500">No addresses found. Please add one.</p>
              ) : addresses.map(addr => (
                <label key={addr.id} className={`flex items-start gap-3 p-4 rounded-xl border-2 cursor-pointer transition-all ${
                  selectedAddress === addr.id ? 'border-primary-500 bg-primary-50/50' : 'border-gray-100 hover:border-gray-200'}`}>
                  <input type="radio" name="address" checked={selectedAddress === addr.id}
                    onChange={() => setSelectedAddress(addr.id)} className="mt-1" />
                  <div>
                    <p className="text-sm font-medium text-gray-900">{addr.street}</p>
                    <p className="text-sm text-gray-500">{addr.city}, {addr.state} {addr.zipCode}</p>
                    <p className="text-sm text-gray-500">{addr.country}</p>
                  </div>
                  {addr.isDefault && <span className="badge-info ml-auto">Default</span>}
                </label>
              ))}
            </div>
          </div>

          {/* Payment Method */}
          <div className="card-flat p-6">
            <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2 mb-4">
              <CreditCard className="w-5 h-5 text-primary-600" /> Payment Method
            </h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {paymentMethods.map(pm => (
                <label key={pm.value} className={`flex items-center gap-3 p-4 rounded-xl border-2 cursor-pointer transition-all ${
                  paymentMethod === pm.value ? 'border-primary-500 bg-primary-50/50' : 'border-gray-100 hover:border-gray-200'}`}>
                  <input type="radio" name="payment" checked={paymentMethod === pm.value}
                    onChange={() => setPaymentMethod(pm.value)} />
                  <span className="text-xl">{pm.icon}</span>
                  <span className="text-sm font-medium text-gray-900">{pm.label}</span>
                </label>
              ))}
            </div>
          </div>
        </div>

        {/* Order Summary */}
        <div>
          <div className="card-flat p-6 sticky top-24">
            <h3 className="text-lg font-bold text-gray-900 mb-4">Order Summary</h3>
            <div className="space-y-3 mb-4 max-h-60 overflow-y-auto">
              {cart.items?.map(item => (
                <div key={item.id} className="flex items-center gap-3">
                  <SafeImage src={item.productImageUrl} alt="" className="w-12 h-12 rounded-lg object-cover" />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-900 truncate">{item.productName}</p>
                    <p className="text-xs text-gray-500">Qty: {item.quantity}</p>
                  </div>
                  <p className="text-sm font-bold">{formatPrice(item.subtotal)}</p>
                </div>
              ))}
            </div>
            <hr className="my-4" />
            <div className="space-y-2 mb-6">
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">Subtotal</span>
                <span>{formatPrice(cart.totalPrice)}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">Delivery</span>
                <span className="text-green-600">FREE</span>
              </div>
              <hr />
              <div className="flex justify-between">
                <span className="font-bold">Total</span>
                <span className="text-xl font-bold">{formatPrice(cart.totalPrice)}</span>
              </div>
            </div>
            <button onClick={handlePlaceOrder} disabled={placing || !selectedAddress}
              className="btn-primary w-full flex items-center justify-center gap-2 !py-3.5">
              {placing ? 'Placing Order...' : <><CheckCircle className="w-5 h-5" /> Place Order</>}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
