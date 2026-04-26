import { Link } from 'react-router-dom';
import { ShoppingCart, Star } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { useNavigate } from 'react-router-dom';
import SafeImage from './SafeImage';

export default function ProductCard({ product }) {
  const { isAuthenticated } = useAuth();
  const { addToCart } = useCart();
  const navigate = useNavigate();

  const handleAddToCart = async (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    try {
      await addToCart(product.id, 1);
    } catch {
      // handled in context
    }
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(price);
  };

  return (
    <Link to={`/products/${product.id}`} className="card group">
      {/* Image */}
      <div className="relative overflow-hidden aspect-square bg-gray-100">
        <SafeImage
          src={product.imageUrl}
          alt={product.name}
          className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500 bg-gray-100"
        />
        {product.discountPercentage > 0 && (
          <span className="absolute top-3 left-3 bg-red-500 text-white text-xs font-bold px-2.5 py-1 rounded-lg shadow">
            -{product.discountPercentage}%
          </span>
        )}
        {product.stock <= 5 && product.stock > 0 && (
          <span className="absolute top-3 right-3 bg-amber-500 text-white text-xs font-bold px-2.5 py-1 rounded-lg shadow">
            Only {product.stock} left
          </span>
        )}
        <button
          onClick={handleAddToCart}
          className="absolute bottom-3 right-3 w-10 h-10 bg-primary-600 hover:bg-primary-700 text-white 
                   rounded-xl flex items-center justify-center shadow-lg opacity-0 group-hover:opacity-100 
                   translate-y-2 group-hover:translate-y-0 transition-all duration-300"
        >
          <ShoppingCart className="w-5 h-5" />
        </button>
      </div>

      {/* Info */}
      <div className="p-4">
        <p className="text-xs text-primary-600 font-medium mb-1">{product.categoryName}</p>
        <h3 className="text-sm font-semibold text-gray-900 line-clamp-2 mb-1 group-hover:text-primary-600 transition-colors">
          {product.name}
        </h3>
        {product.brand && (
          <p className="text-xs text-gray-500 mb-2">by {product.brand}</p>
        )}
        
        {/* Rating */}
        <div className="flex items-center gap-1 mb-2">
          <div className="flex items-center gap-0.5 bg-green-600 text-white px-1.5 py-0.5 rounded text-xs font-semibold">
            <span>{product.rating?.toFixed(1)}</span>
            <Star className="w-3 h-3 fill-current" />
          </div>
          <span className="text-xs text-gray-500">({product.reviewCount})</span>
        </div>

        {/* Price */}
        <div className="flex items-baseline gap-2">
          <span className="text-lg font-bold text-gray-900">{formatPrice(product.effectivePrice)}</span>
          {product.discountPercentage > 0 && (
            <span className="text-sm text-gray-400 line-through">{formatPrice(product.price)}</span>
          )}
        </div>
      </div>
    </Link>
  );
}
