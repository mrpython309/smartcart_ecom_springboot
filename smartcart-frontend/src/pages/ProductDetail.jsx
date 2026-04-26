import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ShoppingCart, Star, Truck, Shield, ArrowLeft, Minus, Plus, Package } from 'lucide-react';
import { productAPI } from '../api/services';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { LoadingSpinner } from '../components/Shared';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';

export default function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { addToCart } = useCart();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [quantity, setQuantity] = useState(1);
  const [adding, setAdding] = useState(false);

  useEffect(() => {
    setLoading(true);
    productAPI.getById(id)
      .then(r => setProduct(r.data.data))
      .catch(() => toast.error('Product not found'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleAddToCart = async () => {
    if (!isAuthenticated) { navigate('/login'); return; }
    setAdding(true);
    try {
      await addToCart(product.id, quantity);
    } catch {} finally {
      setAdding(false);
    }
  };

  const formatPrice = (p) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(p);

  if (loading) return <div className="min-h-[60vh] flex items-center justify-center"><LoadingSpinner size="lg" /></div>;
  if (!product) return <div className="text-center py-20">Product not found</div>;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in">
      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-sm text-gray-500 mb-6">
        <Link to="/" className="hover:text-primary-600">Home</Link>
        <span>/</span>
        <Link to="/products" className="hover:text-primary-600">Products</Link>
        <span>/</span>
        <span className="text-gray-900 font-medium truncate">{product.name}</span>
      </div>

      <div className="grid lg:grid-cols-2 gap-10">
        {/* Image */}
        <div className="relative">
          <div className="aspect-square rounded-2xl overflow-hidden bg-gray-100 shadow-lg">
            <img src={product.imageUrl} alt={product.name} className="w-full h-full object-cover" />
          </div>
          {product.discountPercentage > 0 && (
            <span className="absolute top-4 left-4 bg-red-500 text-white text-sm font-bold px-4 py-2 rounded-xl shadow-lg">
              {product.discountPercentage}% OFF
            </span>
          )}
        </div>

        {/* Details */}
        <div>
          <span className="text-sm text-primary-600 font-medium">{product.categoryName}</span>
          <h1 className="text-3xl font-bold text-gray-900 mt-1 mb-2">{product.name}</h1>
          {product.brand && <p className="text-gray-500 mb-4">by <span className="font-medium">{product.brand}</span></p>}

          {/* Rating */}
          <div className="flex items-center gap-3 mb-6">
            <div className="flex items-center gap-1 bg-green-600 text-white px-2.5 py-1 rounded-lg text-sm font-semibold">
              <span>{product.rating?.toFixed(1)}</span>
              <Star className="w-4 h-4 fill-current" />
            </div>
            <span className="text-sm text-gray-500">{product.reviewCount} reviews</span>
          </div>

          {/* Price */}
          <div className="flex items-baseline gap-3 mb-6">
            <span className="text-4xl font-bold text-gray-900">{formatPrice(product.effectivePrice)}</span>
            {product.discountPercentage > 0 && (
              <>
                <span className="text-xl text-gray-400 line-through">{formatPrice(product.price)}</span>
                <span className="text-sm text-green-600 font-semibold">Save {formatPrice(product.price - product.effectivePrice)}</span>
              </>
            )}
          </div>

          {/* Stock Status */}
          <div className="mb-6">
            {product.stock > 0 ? (
              <span className="inline-flex items-center gap-1.5 text-sm text-green-600 font-medium">
                <Package className="w-4 h-4" /> In Stock ({product.stock} available)
              </span>
            ) : (
              <span className="text-sm text-red-500 font-medium">Out of Stock</span>
            )}
          </div>

          {/* Quantity + Add to Cart */}
          {product.stock > 0 && (
            <div className="flex items-center gap-4 mb-8">
              <div className="flex items-center border border-gray-200 rounded-xl">
                <button onClick={() => setQuantity(Math.max(1, quantity - 1))}
                  className="p-3 hover:bg-gray-50 rounded-l-xl"><Minus className="w-4 h-4" /></button>
                <span className="w-12 text-center font-semibold">{quantity}</span>
                <button onClick={() => setQuantity(Math.min(product.stock, quantity + 1))}
                  className="p-3 hover:bg-gray-50 rounded-r-xl"><Plus className="w-4 h-4" /></button>
              </div>
              <button onClick={handleAddToCart} disabled={adding}
                className="btn-primary flex-1 flex items-center justify-center gap-2 !py-3.5">
                <ShoppingCart className="w-5 h-5" />
                {adding ? 'Adding...' : 'Add to Cart'}
              </button>
            </div>
          )}

          {/* Features */}
          <div className="grid grid-cols-2 gap-4 p-5 bg-gray-50 rounded-2xl">
            <div className="flex items-center gap-3">
              <Truck className="w-5 h-5 text-primary-600" />
              <div>
                <p className="text-sm font-medium text-gray-900">Free Delivery</p>
                <p className="text-xs text-gray-500">On orders above ₹499</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Shield className="w-5 h-5 text-primary-600" />
              <div>
                <p className="text-sm font-medium text-gray-900">Warranty</p>
                <p className="text-xs text-gray-500">1 Year Brand Warranty</p>
              </div>
            </div>
          </div>

          {/* Description */}
          {product.description && (
            <div className="mt-8">
              <h3 className="text-lg font-semibold text-gray-900 mb-3">Description</h3>
              <p className="text-gray-600 leading-relaxed">{product.description}</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
