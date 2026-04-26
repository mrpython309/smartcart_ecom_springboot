import { NavLink, Outlet } from 'react-router-dom';
import { LayoutDashboard, Package, ShoppingBag, Users, FolderTree, ArrowLeft } from 'lucide-react';
import { Link } from 'react-router-dom';

const navItems = [
  { to: '/admin', icon: LayoutDashboard, label: 'Dashboard', end: true },
  { to: '/admin/products', icon: Package, label: 'Products' },
  { to: '/admin/orders', icon: ShoppingBag, label: 'Orders' },
  { to: '/admin/users', icon: Users, label: 'Users' },
  { to: '/admin/categories', icon: FolderTree, label: 'Categories' },
];

export default function AdminLayout() {
  return (
    <div className="flex min-h-[calc(100vh-4rem)]">
      {/* Sidebar */}
      <aside className="w-64 bg-gray-900 text-white shrink-0 hidden lg:block">
        <div className="p-6">
          <Link to="/" className="flex items-center gap-2 text-sm text-gray-400 hover:text-white mb-6">
            <ArrowLeft className="w-4 h-4" /> Back to Store
          </Link>
          <h2 className="text-lg font-bold">Admin Panel</h2>
          <p className="text-xs text-gray-500 mt-1">Manage your store</p>
        </div>
        <nav className="px-3 space-y-1">
          {navItems.map(item => (
            <NavLink key={item.to} to={item.to} end={item.end}
              className={({ isActive }) => `flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all ${
                isActive ? 'bg-primary-600 text-white shadow-lg shadow-primary-600/30' : 'text-gray-400 hover:text-white hover:bg-gray-800'}`}>
              <item.icon className="w-5 h-5" />
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      {/* Mobile nav */}
      <div className="lg:hidden fixed bottom-0 left-0 right-0 z-50 bg-white border-t border-gray-200 flex">
        {navItems.map(item => (
          <NavLink key={item.to} to={item.to} end={item.end}
            className={({ isActive }) => `flex-1 flex flex-col items-center py-2 text-xs ${
              isActive ? 'text-primary-600' : 'text-gray-400'}`}>
            <item.icon className="w-5 h-5 mb-0.5" />
            {item.label}
          </NavLink>
        ))}
      </div>

      {/* Content */}
      <main className="flex-1 bg-gray-50 p-4 sm:p-6 lg:p-8 overflow-y-auto pb-20 lg:pb-8">
        <Outlet />
      </main>
    </div>
  );
}
