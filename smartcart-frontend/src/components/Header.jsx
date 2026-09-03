import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ShoppingCart, Search, Menu, X, User, LogOut, Package, LayoutDashboard, ChevronDown } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import AiSearchModal from './AiSearchModal';
import { Sparkles } from 'lucide-react';

export default function Header() {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const { cart } = useCart();
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const [aiModalOpen, setAiModalOpen] = useState(false);

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/products?search=${encodeURIComponent(searchQuery.trim())}`);
      setSearchQuery('');
    }
  };

  const handleLogout = () => {
    logout();
    setProfileOpen(false);
    navigate('/');
  };

  return (
    <header className="sticky top-0 z-50 glass shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16 gap-4">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2 shrink-0">
            <div className="w-9 h-9 rounded-xl gradient-primary flex items-center justify-center">
              <ShoppingCart className="w-5 h-5 text-white" />
            </div>
            <span className="text-xl font-bold text-gray-900 hidden sm:block">
              Smart<span className="text-primary-600">Cart</span>
            </span>
          </Link>

          {/* Search Bar */}
          <div className="flex-1 max-w-xl hidden md:flex items-center gap-2">
            <form onSubmit={handleSearch} className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search products, brands, categories..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-gray-200 bg-gray-50/80 
                           focus:bg-white focus:outline-none focus:ring-2 focus:ring-primary-500 
                           focus:border-transparent transition-all text-sm"
                />
              </div>
            </form>
            <button
              onClick={() => setAiModalOpen(true)}
              className="flex items-center gap-2 px-4 py-2.5 rounded-xl bg-gradient-to-r from-purple-600 to-pink-500 text-white font-medium shadow-sm hover:shadow-md hover:from-purple-700 hover:to-pink-600 transition-all transform hover:-translate-y-0.5"
              title="AI Shopping Assistant"
            >
              <Sparkles className="w-5 h-5 animate-pulse" />
              <span className="text-sm hidden lg:block">Ask AI</span>
            </button>
          </div>

          {/* Right Actions */}
          <div className="flex items-center gap-2">
            {isAuthenticated ? (
              <>
                {/* Cart Button */}
                <Link to="/cart" className="relative p-2 rounded-xl hover:bg-gray-100 transition-colors">
                  <ShoppingCart className="w-6 h-6 text-gray-700" />
                  {cart.totalItems > 0 && (
                    <span className="absolute -top-0.5 -right-0.5 w-5 h-5 bg-primary-600 text-white 
                                   text-xs font-bold rounded-full flex items-center justify-center animate-scale-in">
                      {cart.totalItems}
                    </span>
                  )}
                </Link>

                {/* Profile Dropdown */}
                <div className="relative">
                  <button
                    onClick={() => setProfileOpen(!profileOpen)}
                    className="flex items-center gap-2 p-2 rounded-xl hover:bg-gray-100 transition-colors"
                  >
                    <div className="w-8 h-8 rounded-full gradient-primary flex items-center justify-center text-white text-sm font-semibold">
                      {user?.firstName?.charAt(0)}
                    </div>
                    <span className="text-sm font-medium text-gray-700 hidden lg:block">{user?.firstName}</span>
                    <ChevronDown className="w-4 h-4 text-gray-500 hidden lg:block" />
                  </button>

                  {profileOpen && (
                    <>
                      <div className="fixed inset-0 z-40" onClick={() => setProfileOpen(false)} />
                      <div className="absolute right-0 top-full mt-2 w-56 bg-white rounded-xl shadow-xl border border-gray-100 py-2 z-50 animate-slide-down">
                        <div className="px-4 py-3 border-b border-gray-100">
                          <p className="text-sm font-semibold text-gray-900">{user?.firstName} {user?.lastName}</p>
                          <p className="text-xs text-gray-500">{user?.email}</p>
                        </div>
                        <Link to="/account" onClick={() => setProfileOpen(false)}
                          className="flex items-center gap-3 px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50">
                          <User className="w-4 h-4" /> My Account
                        </Link>
                        <Link to="/orders" onClick={() => setProfileOpen(false)}
                          className="flex items-center gap-3 px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50">
                          <Package className="w-4 h-4" /> My Orders
                        </Link>
                        {isAdmin && (
                          <Link to="/admin" onClick={() => setProfileOpen(false)}
                            className="flex items-center gap-3 px-4 py-2.5 text-sm text-primary-600 hover:bg-primary-50">
                            <LayoutDashboard className="w-4 h-4" /> Admin Panel
                          </Link>
                        )}
                        <hr className="my-1 border-gray-100" />
                        <button onClick={handleLogout}
                          className="flex items-center gap-3 px-4 py-2.5 text-sm text-red-600 hover:bg-red-50 w-full">
                          <LogOut className="w-4 h-4" /> Logout
                        </button>
                      </div>
                    </>
                  )}
                </div>
              </>
            ) : (
              <div className="flex items-center gap-2">
                <Link to="/login" className="btn-secondary !py-2 !px-4 text-sm">Login</Link>
                <Link to="/register" className="btn-primary !py-2 !px-4 text-sm">Sign Up</Link>
              </div>
            )}

            {/* Mobile Menu Toggle */}
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="md:hidden p-2 rounded-xl hover:bg-gray-100"
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
            <button
              onClick={() => setAiModalOpen(true)}
              className="md:hidden p-2 rounded-xl bg-gradient-to-r from-purple-600 to-pink-500 text-white shadow-sm"
            >
              <Sparkles className="w-5 h-5 animate-pulse" />
            </button>
          </div>
        </div>

        {/* Mobile Search */}
        {mobileMenuOpen && (
          <div className="md:hidden pb-4 animate-slide-down">
            <form onSubmit={handleSearch}>
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search products..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-gray-200 bg-gray-50 
                           focus:outline-none focus:ring-2 focus:ring-primary-500 text-sm"
                />
              </div>
            </form>
            <nav className="mt-3 space-y-1">
              <Link to="/products" onClick={() => setMobileMenuOpen(false)}
                className="block px-4 py-2.5 rounded-lg text-sm text-gray-700 hover:bg-gray-50">All Products</Link>
              {isAuthenticated && (
                <>
                  <Link to="/account" onClick={() => setMobileMenuOpen(false)}
                    className="block px-4 py-2.5 rounded-lg text-sm text-gray-700 hover:bg-gray-50">My Account</Link>
                  <Link to="/orders" onClick={() => setMobileMenuOpen(false)}
                    className="block px-4 py-2.5 rounded-lg text-sm text-gray-700 hover:bg-gray-50">My Orders</Link>
                </>
              )}
            </nav>
          </div>
        )}
      </div>

      {/* Category Bar */}
      <div className="border-t border-gray-100 bg-white/50 hidden md:block">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <nav className="flex items-center gap-6 h-10 text-sm">
            <Link to="/products" className="text-gray-600 hover:text-primary-600 transition-colors font-medium">All Products</Link>
            <Link to="/products?categoryId=1" className="text-gray-600 hover:text-primary-600 transition-colors">Electronics</Link>
            <Link to="/products?categoryId=2" className="text-gray-600 hover:text-primary-600 transition-colors">Fashion</Link>
            <Link to="/products?categoryId=3" className="text-gray-600 hover:text-primary-600 transition-colors">Home & Living</Link>
            <Link to="/products?categoryId=4" className="text-gray-600 hover:text-primary-600 transition-colors">Books</Link>
            <Link to="/products?categoryId=5" className="text-gray-600 hover:text-primary-600 transition-colors">Sports</Link>
            <Link to="/products?categoryId=6" className="text-gray-600 hover:text-primary-600 transition-colors">Beauty</Link>
          </nav>
        </div>
      </div>

      <AiSearchModal isOpen={aiModalOpen} onClose={() => setAiModalOpen(false)} />
    </header>
  );
}
