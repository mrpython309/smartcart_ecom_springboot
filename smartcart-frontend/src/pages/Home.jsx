import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, ShoppingBag, Truck, Shield, Headphones, Star, Zap } from 'lucide-react';
import { productAPI, categoryAPI } from '../api/services';
import ProductCard from '../components/ProductCard';
import { ProductSkeleton } from '../components/Shared';

export default function Home() {
  const [featured, setFeatured] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      productAPI.getAll({ page: 0, size: 8, sortBy: 'rating', sortDir: 'desc' }),
      categoryAPI.getAll()
    ]).then(([prodRes, catRes]) => {
      setFeatured(prodRes.data.data.content);
      setCategories(catRes.data.data);
    }).catch(() => {}).finally(() => setLoading(false));
  }, []);

  const features = [
    { icon: Truck, title: 'Free Delivery', desc: 'On orders above ₹499' },
    { icon: Shield, title: 'Secure Payment', desc: '100% secure checkout' },
    { icon: Headphones, title: '24/7 Support', desc: 'Dedicated support team' },
    { icon: Star, title: 'Best Quality', desc: 'Premium verified products' },
  ];

  return (
    <div className="animate-fade-in">
      {/* Hero Section */}
      <section className="gradient-hero text-white relative overflow-hidden">
        <div className="absolute inset-0 overflow-hidden">
          <div className="absolute -top-40 -right-40 w-96 h-96 bg-white/5 rounded-full blur-3xl" />
          <div className="absolute -bottom-40 -left-40 w-96 h-96 bg-accent-500/10 rounded-full blur-3xl" />
        </div>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 md:py-32 relative z-10">
          <div className="max-w-2xl">
            <div className="inline-flex items-center gap-2 bg-white/10 backdrop-blur-sm px-4 py-2 rounded-full text-sm mb-6">
              <Zap className="w-4 h-4 text-accent-400" />
              <span>Flash Sale — Up to 40% Off</span>
            </div>
            <h1 className="text-4xl md:text-6xl font-extrabold leading-tight mb-6">
              Shop Smart,<br />
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-accent-300 to-accent-500">
                Live Better
              </span>
            </h1>
            <p className="text-lg text-gray-300 mb-8 max-w-lg">
              Discover premium products at unbeatable prices. From cutting-edge electronics to trending fashion — everything you need, delivered to your doorstep.
            </p>
            <div className="flex flex-wrap gap-4">
              <Link to="/products" className="btn-accent !py-3.5 !px-8 !text-base flex items-center gap-2">
                Shop Now <ArrowRight className="w-5 h-5" />
              </Link>
              <Link to="/products?categoryId=1" className="bg-white/10 hover:bg-white/20 backdrop-blur-sm text-white font-semibold py-3.5 px-8 rounded-xl transition-all">
                Explore Electronics
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Features Strip */}
      <section className="border-b border-gray-100 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
            {features.map((f, i) => (
              <div key={i} className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-primary-50 flex items-center justify-center shrink-0">
                  <f.icon className="w-5 h-5 text-primary-600" />
                </div>
                <div>
                  <p className="text-sm font-semibold text-gray-900">{f.title}</p>
                  <p className="text-xs text-gray-500">{f.desc}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Categories */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Shop by Category</h2>
            <p className="text-sm text-gray-500 mt-1">Browse our curated collections</p>
          </div>
          <Link to="/products" className="text-primary-600 hover:text-primary-700 text-sm font-semibold flex items-center gap-1">
            View All <ArrowRight className="w-4 h-4" />
          </Link>
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
          {categories.map(cat => (
            <Link key={cat.id} to={`/products?categoryId=${cat.id}`}
              className="group relative rounded-2xl overflow-hidden aspect-square bg-gray-100">
              <img src={cat.imageUrl} alt={cat.name} className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500" />
              <div className="absolute inset-0 bg-gradient-to-t from-gray-900/80 via-gray-900/20 to-transparent" />
              <div className="absolute bottom-0 left-0 right-0 p-4">
                <h3 className="text-white font-semibold text-sm">{cat.name}</h3>
                <p className="text-gray-300 text-xs">{cat.productCount} products</p>
              </div>
            </Link>
          ))}
        </div>
      </section>

      {/* Featured Products */}
      <section className="bg-gray-50/50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
          <div className="flex items-center justify-between mb-8">
            <div>
              <h2 className="text-2xl font-bold text-gray-900">Featured Products</h2>
              <p className="text-sm text-gray-500 mt-1">Hand-picked top-rated items</p>
            </div>
            <Link to="/products" className="text-primary-600 hover:text-primary-700 text-sm font-semibold flex items-center gap-1">
              View All <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 md:gap-6">
            {loading
              ? Array(8).fill(0).map((_, i) => <ProductSkeleton key={i} />)
              : featured.map(p => <ProductCard key={p.id} product={p} />)
            }
          </div>
        </div>
      </section>

      {/* CTA Banner */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="gradient-primary rounded-3xl p-10 md:p-16 text-white text-center relative overflow-hidden">
          <div className="absolute inset-0">
            <div className="absolute -top-20 -right-20 w-64 h-64 bg-white/5 rounded-full" />
            <div className="absolute -bottom-20 -left-20 w-64 h-64 bg-white/5 rounded-full" />
          </div>
          <div className="relative z-10">
            <h2 className="text-3xl md:text-4xl font-bold mb-4">Ready to Start Shopping?</h2>
            <p className="text-lg text-gray-200 mb-8 max-w-lg mx-auto">
              Join thousands of happy customers. Create your account and start exploring amazing deals today.
            </p>
            <div className="flex flex-wrap justify-center gap-4">
              <Link to="/register" className="bg-white text-primary-700 font-semibold py-3.5 px-8 rounded-xl hover:bg-gray-100 transition-all shadow-lg">
                Create Account
              </Link>
              <Link to="/products" className="border-2 border-white/30 text-white font-semibold py-3.5 px-8 rounded-xl hover:bg-white/10 transition-all">
                Browse Products
              </Link>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
