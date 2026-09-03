import { useState, useEffect, useRef } from 'react';
import { Search, X, Sparkles, Loader2, ArrowRight } from 'lucide-react';
import { aiAPI } from '../api/services';
import { Link, useNavigate } from 'react-router-dom';

export default function AiSearchModal({ isOpen, onClose }) {
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [results, setResults] = useState([]);
  const [aiMessage, setAiMessage] = useState('');
  const [searched, setSearched] = useState(false);
  const inputRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (isOpen && inputRef.current) {
      inputRef.current.focus();
    }
  }, [isOpen]);

  if (!isOpen) return null;

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!query.trim()) return;

    setLoading(true);
    setSearched(true);
    try {
      const response = await aiAPI.search({ query: query.trim() });
      if (response.data.success) {
        setResults(response.data.data.products.content);
        setAiMessage(response.data.data.aiMessage);
      }
    } catch (error) {
      console.error('AI Search failed:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleProductClick = (id) => {
    onClose();
    navigate(`/products/${id}`);
  };

  return (
    <div className="fixed inset-0 z-[100] flex items-start justify-center pt-20 px-4">
      {/* Backdrop */}
      <div 
        className="fixed inset-0 bg-gray-900/40 backdrop-blur-sm transition-opacity"
        onClick={onClose}
      />

      {/* Modal */}
      <div className="relative w-full max-w-2xl bg-white rounded-2xl shadow-2xl overflow-hidden animate-slide-down border border-purple-100">
        
        {/* Header / Input */}
        <div className="relative border-b border-gray-100 bg-gradient-to-r from-purple-50 to-pink-50 p-4">
          <form onSubmit={handleSearch} className="relative flex items-center">
            <Sparkles className="absolute left-4 w-5 h-5 text-purple-500 animate-pulse" />
            <input
              ref={inputRef}
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Ask anything... e.g., 'budget phone under $500 with good camera'"
              className="w-full pl-12 pr-12 py-3 bg-white/80 rounded-xl border border-purple-200 
                       focus:outline-none focus:ring-2 focus:ring-purple-400 focus:bg-white
                       text-gray-800 placeholder-gray-400 text-lg transition-all shadow-sm"
            />
            {query && (
              <button
                type="button"
                onClick={() => setQuery('')}
                className="absolute right-14 p-1 text-gray-400 hover:text-gray-600 rounded-lg"
              >
                <X className="w-5 h-5" />
              </button>
            )}
            <button
              type="button"
              onClick={onClose}
              className="absolute right-4 p-2 text-gray-400 hover:text-gray-600 hover:bg-white rounded-lg transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </form>
        </div>

        {/* Results Area */}
        <div className="max-h-[60vh] overflow-y-auto p-4">
          {loading ? (
            <div className="flex flex-col items-center justify-center py-12 space-y-4">
              <Loader2 className="w-8 h-8 text-purple-500 animate-spin" />
              <p className="text-purple-600 font-medium animate-pulse">AI is analyzing your request...</p>
            </div>
          ) : searched ? (
            results.length > 0 ? (
              <div className="space-y-3">
                {aiMessage && (
                  <div className="mb-6 p-4 bg-gradient-to-r from-purple-100 to-pink-50 rounded-2xl rounded-tl-sm border border-purple-200 flex items-start gap-3 shadow-sm animate-fade-in">
                    <div className="p-2 bg-purple-500 rounded-full shrink-0">
                      <Sparkles className="w-4 h-4 text-white" />
                    </div>
                    <p className="text-gray-800 text-sm leading-relaxed font-medium">
                      {aiMessage}
                    </p>
                  </div>
                )}
                <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4 px-2">
                  AI Curated Results
                </h3>
                {results.map((product) => (
                  <div 
                    key={product.id}
                    onClick={() => handleProductClick(product.id)}
                    className="flex items-center gap-4 p-3 rounded-xl hover:bg-purple-50 transition-colors cursor-pointer group border border-transparent hover:border-purple-100"
                  >
                    <img 
                      src={product.imageUrl} 
                      alt={product.name}
                      className="w-16 h-16 object-cover rounded-lg bg-gray-100"
                    />
                    <div className="flex-1 min-w-0">
                      <h4 className="font-semibold text-gray-900 truncate group-hover:text-purple-700 transition-colors">
                        {product.name}
                      </h4>
                      <p className="text-sm text-gray-500 truncate">{product.categoryName} • {product.brand}</p>
                    </div>
                    <div className="text-right">
                      <p className="font-bold text-gray-900">${product.effectivePrice}</p>
                      {product.discountPercentage > 0 && (
                        <p className="text-xs text-red-500 line-through">${product.price}</p>
                      )}
                    </div>
                    <ArrowRight className="w-5 h-5 text-gray-300 group-hover:text-purple-500 transition-colors opacity-0 group-hover:opacity-100 transform translate-x-[-10px] group-hover:translate-x-0" />
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-12">
                <div className="w-16 h-16 mx-auto bg-gray-50 rounded-full flex items-center justify-center mb-4">
                  <Search className="w-8 h-8 text-gray-400" />
                </div>
                <p className="text-gray-600 font-medium">No matches found.</p>
                <p className="text-sm text-gray-400 mt-1">Try asking differently or adjusting your price limit.</p>
              </div>
            )
          ) : (
            <div className="text-center py-12">
              <div className="w-16 h-16 mx-auto bg-purple-50 rounded-full flex items-center justify-center mb-4">
                <Sparkles className="w-8 h-8 text-purple-400" />
              </div>
              <h3 className="text-lg font-semibold text-gray-800 mb-2">Smart AI Search</h3>
              <p className="text-gray-500 max-w-md mx-auto text-sm">
                Describe exactly what you're looking for in plain English. 
                Our AI will instantly find the perfect products for you.
              </p>
              <div className="mt-6 flex flex-wrap justify-center gap-2">
                <span className="px-3 py-1.5 bg-gray-50 rounded-full text-xs text-gray-600 border border-gray-100">
                  "Gaming laptop under $1000"
                </span>
                <span className="px-3 py-1.5 bg-gray-50 rounded-full text-xs text-gray-600 border border-gray-100">
                  "Summer dress for vacation"
                </span>
              </div>
            </div>
          )}
        </div>
        
        <div className="bg-gray-50 p-3 text-center border-t border-gray-100">
          <p className="text-xs text-gray-400 font-medium">Powered by Gemini AI</p>
        </div>
      </div>
    </div>
  );
}
