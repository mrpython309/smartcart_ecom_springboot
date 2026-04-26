import { Link } from 'react-router-dom';
import { ShoppingCart, Mail, Phone, MapPin } from 'lucide-react';

export default function Footer() {
  return (
    <footer className="bg-gray-900 text-gray-300">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-10">
          {/* Brand */}
          <div>
            <div className="flex items-center gap-2 mb-4">
              <div className="w-9 h-9 rounded-xl bg-primary-600 flex items-center justify-center">
                <ShoppingCart className="w-5 h-5 text-white" />
              </div>
              <span className="text-xl font-bold text-white">SmartCart</span>
            </div>
            <p className="text-sm text-gray-400 leading-relaxed mb-4">
              Your premium destination for quality products. Shop smart, live better with SmartCart.
            </p>
            <div className="flex gap-3">
              {['twitter','facebook','instagram','linkedin'].map(s => (
                <a key={s} href="#" className="w-9 h-9 rounded-lg bg-gray-800 hover:bg-primary-600 flex items-center justify-center transition-colors">
                  <span className="text-xs font-bold text-gray-400 hover:text-white uppercase">{s[0]}</span>
                </a>
              ))}
            </div>
          </div>

          {/* Quick Links */}
          <div>
            <h4 className="text-white font-semibold mb-4">Quick Links</h4>
            <ul className="space-y-2.5">
              {[{t:'All Products',l:'/products'},{t:'Electronics',l:'/products?categoryId=1'},
                {t:'Fashion',l:'/products?categoryId=2'},{t:'Home & Living',l:'/products?categoryId=3'},
                {t:'Books',l:'/products?categoryId=4'}].map(item => (
                <li key={item.t}><Link to={item.l} className="text-sm hover:text-primary-400 transition-colors">{item.t}</Link></li>
              ))}
            </ul>
          </div>

          {/* Account */}
          <div>
            <h4 className="text-white font-semibold mb-4">My Account</h4>
            <ul className="space-y-2.5">
              {[{t:'My Profile',l:'/account'},{t:'Order History',l:'/orders'},
                {t:'Shopping Cart',l:'/cart'},{t:'Wishlist',l:'#'}].map(item => (
                <li key={item.t}><Link to={item.l} className="text-sm hover:text-primary-400 transition-colors">{item.t}</Link></li>
              ))}
            </ul>
          </div>

          {/* Contact */}
          <div>
            <h4 className="text-white font-semibold mb-4">Contact</h4>
            <ul className="space-y-3">
              <li className="flex items-start gap-3 text-sm">
                <MapPin className="w-4 h-4 mt-0.5 text-primary-400 shrink-0" />
                123 Commerce St, Tech Hub, India
              </li>
              <li className="flex items-center gap-3 text-sm">
                <Phone className="w-4 h-4 text-primary-400 shrink-0" />
                +91 98765 43210
              </li>
              <li className="flex items-center gap-3 text-sm">
                <Mail className="w-4 h-4 text-primary-400 shrink-0" />
                support@smartcart.com
              </li>
            </ul>
          </div>
        </div>
      </div>
      <div className="border-t border-gray-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-5 flex flex-col sm:flex-row justify-between items-center gap-2">
          <p className="text-xs text-gray-500">© 2026 SmartCart. All rights reserved.</p>
          <div className="flex gap-4 text-xs text-gray-500">
            <a href="#" className="hover:text-gray-300">Privacy</a>
            <a href="#" className="hover:text-gray-300">Terms</a>
            <a href="#" className="hover:text-gray-300">Cookies</a>
          </div>
        </div>
      </div>
    </footer>
  );
}
