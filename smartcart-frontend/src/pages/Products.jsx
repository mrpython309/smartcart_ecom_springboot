import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { SlidersHorizontal, X } from 'lucide-react';
import { productAPI, categoryAPI } from '../api/services';
import ProductCard from '../components/ProductCard';
import { ProductSkeleton, EmptyState } from '../components/Shared';
import { ShoppingBag } from 'lucide-react';

export default function Products() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(0);
  const [showFilters, setShowFilters] = useState(false);

  const page = parseInt(searchParams.get('page') || '0');
  const categoryId = searchParams.get('categoryId') || '';
  const search = searchParams.get('search') || '';
  const sortBy = searchParams.get('sortBy') || 'createdAt';
  const sortDir = searchParams.get('sortDir') || 'desc';
  const minPrice = searchParams.get('minPrice') || '';
  const maxPrice = searchParams.get('maxPrice') || '';

  useEffect(() => {
    categoryAPI.getAll().then(r => setCategories(r.data.data)).catch(() => {});
  }, []);

  useEffect(() => {
    setLoading(true);
    const params = { page, size: 12, sortBy, sortDir };
    if (search) params.query = search;
    if (categoryId) params.categoryId = categoryId;
    if (minPrice) params.minPrice = minPrice;
    if (maxPrice) params.maxPrice = maxPrice;

    productAPI.filter(params)
      .then(r => {
        setProducts(r.data.data.content);
        setTotalPages(r.data.data.totalPages);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [page, categoryId, search, sortBy, sortDir, minPrice, maxPrice]);

  const updateParam = (key, value) => {
    const params = new URLSearchParams(searchParams);
    if (value) params.set(key, value); else params.delete(key);
    if (key !== 'page') params.set('page', '0');
    setSearchParams(params);
  };

  const clearFilters = () => setSearchParams({});

  const hasFilters = categoryId || search || minPrice || maxPrice;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            {search ? `Results for "${search}"` : 'All Products'}
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            {products.length > 0 ? `Showing ${products.length} products` : 'No products found'}
          </p>
        </div>
        <div className="flex items-center gap-3">
          <select value={`${sortBy}-${sortDir}`} onChange={e => {
            const [sb, sd] = e.target.value.split('-');
            const params = new URLSearchParams(searchParams);
            params.set('sortBy', sb); 
            params.set('sortDir', sd); 
            params.set('page', '0');
            setSearchParams(params);
          }} className="input-field !w-auto !py-2 text-sm">
            <option value="createdAt-desc">Newest First</option>
            <option value="price-asc">Price: Low to High</option>
            <option value="price-desc">Price: High to Low</option>
            <option value="rating-desc">Top Rated</option>
            <option value="name-asc">A — Z</option>
          </select>
          <button onClick={() => setShowFilters(!showFilters)}
            className="lg:hidden btn-secondary !py-2 !px-3 flex items-center gap-2 text-sm">
            <SlidersHorizontal className="w-4 h-4" /> Filters
          </button>
        </div>
      </div>

      <div className="flex gap-8">
        {/* Sidebar Filters */}
        <aside className={`${showFilters ? 'fixed inset-0 z-50 bg-black/50 lg:relative lg:bg-transparent' : 'hidden'} lg:block lg:w-64 shrink-0`}>
          <div className={`${showFilters ? 'absolute right-0 top-0 bottom-0 w-80 bg-white p-6 shadow-2xl overflow-y-auto' : ''} lg:relative lg:w-auto lg:shadow-none lg:p-0`}>
            {showFilters && (
              <button onClick={() => setShowFilters(false)} className="lg:hidden absolute top-4 right-4">
                <X className="w-6 h-6" />
              </button>
            )}

            <div className="card-flat p-5 space-y-6">
              <div>
                <h3 className="text-sm font-semibold text-gray-900 mb-3">Categories</h3>
                <div className="space-y-2">
                  <label className="flex items-center gap-2 cursor-pointer">
                    <input type="radio" name="category" checked={!categoryId}
                      onChange={() => updateParam('categoryId', '')}
                      className="w-4 h-4 text-primary-600" />
                    <span className="text-sm text-gray-700">All Categories</span>
                  </label>
                  {categories.map(c => (
                    <label key={c.id} className="flex items-center gap-2 cursor-pointer">
                      <input type="radio" name="category" checked={categoryId === String(c.id)}
                        onChange={() => updateParam('categoryId', c.id)}
                        className="w-4 h-4 text-primary-600" />
                      <span className="text-sm text-gray-700">{c.name}</span>
                      <span className="text-xs text-gray-400 ml-auto">{c.productCount}</span>
                    </label>
                  ))}
                </div>
              </div>

              <hr />

              <div>
                <h3 className="text-sm font-semibold text-gray-900 mb-3">Price Range</h3>
                <div className="flex items-center gap-2">
                  <input type="number" placeholder="Min" value={minPrice}
                    onChange={e => updateParam('minPrice', e.target.value)}
                    className="input-field !py-2 text-sm" />
                  <span className="text-gray-400">—</span>
                  <input type="number" placeholder="Max" value={maxPrice}
                    onChange={e => updateParam('maxPrice', e.target.value)}
                    className="input-field !py-2 text-sm" />
                </div>
              </div>

              {hasFilters && (
                <>
                  <hr />
                  <button onClick={clearFilters} className="text-sm text-red-500 font-medium hover:text-red-600">
                    Clear All Filters
                  </button>
                </>
              )}
            </div>
          </div>
        </aside>

        {/* Product Grid */}
        <main className="flex-1">
          {loading ? (
            <div className="grid grid-cols-2 md:grid-cols-3 gap-4 md:gap-6">
              {Array(12).fill(0).map((_, i) => <ProductSkeleton key={i} />)}
            </div>
          ) : products.length === 0 ? (
            <EmptyState icon={ShoppingBag} title="No products found"
              description="Try adjusting your filters or search to find what you're looking for."
              action={<button onClick={clearFilters} className="btn-primary">Clear Filters</button>} />
          ) : (
            <>
              <div className="grid grid-cols-2 md:grid-cols-3 gap-4 md:gap-6">
                {products.map(p => <ProductCard key={p.id} product={p} />)}
              </div>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="flex justify-center gap-2 mt-10">
                  <button onClick={() => updateParam('page', Math.max(0, page - 1))} disabled={page === 0}
                    className="btn-secondary !py-2 !px-4 text-sm disabled:opacity-40">Previous</button>
                  {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => {
                    const p = page < 3 ? i : page - 2 + i;
                    if (p >= totalPages) return null;
                    return (
                      <button key={p} onClick={() => updateParam('page', p)}
                        className={`w-10 h-10 rounded-xl text-sm font-medium transition-all ${p === page ? 'bg-primary-600 text-white' : 'hover:bg-gray-100 text-gray-700'}`}>
                        {p + 1}
                      </button>
                    );
                  })}
                  <button onClick={() => updateParam('page', Math.min(totalPages - 1, page + 1))} disabled={page >= totalPages - 1}
                    className="btn-secondary !py-2 !px-4 text-sm disabled:opacity-40">Next</button>
                </div>
              )}
            </>
          )}
        </main>
      </div>
    </div>
  );
}
