import { Routes, Route } from 'react-router-dom';
import Header from './components/Header';
import Footer from './components/Footer';
import { ProtectedRoute, AdminRoute } from './components/Shared';

// Pages
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Products from './pages/Products';
import ProductDetail from './pages/ProductDetail';
import CartPage from './pages/CartPage';
import Checkout from './pages/Checkout';
import UserDashboard from './pages/UserDashboard';
import OrderHistory from './pages/OrderHistory';

// Admin Pages
import AdminLayout from './pages/admin/AdminLayout';
import AdminDashboard from './pages/admin/AdminDashboard';
import AdminProducts from './pages/admin/AdminProducts';
import AdminOrders from './pages/admin/AdminOrders';
import AdminUsers from './pages/admin/AdminUsers';
import AdminCategories from './pages/admin/AdminCategories';

function MainLayout({ children }) {
  return (
    <>
      <Header />
      <main className="min-h-[calc(100vh-16rem)]">{children}</main>
      <Footer />
    </>
  );
}

export default function App() {
  return (
    <Routes>
      {/* Auth pages — no header/footer */}
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      {/* Main pages with header/footer */}
      <Route path="/" element={<MainLayout><Home /></MainLayout>} />
      <Route path="/products" element={<MainLayout><Products /></MainLayout>} />
      <Route path="/products/:id" element={<MainLayout><ProductDetail /></MainLayout>} />

      {/* Protected user pages */}
      <Route path="/cart" element={<MainLayout><ProtectedRoute><CartPage /></ProtectedRoute></MainLayout>} />
      <Route path="/checkout" element={<MainLayout><ProtectedRoute><Checkout /></ProtectedRoute></MainLayout>} />
      <Route path="/account" element={<MainLayout><ProtectedRoute><UserDashboard /></ProtectedRoute></MainLayout>} />
      <Route path="/orders" element={<MainLayout><ProtectedRoute><OrderHistory /></ProtectedRoute></MainLayout>} />

      {/* Admin pages — uses AdminLayout with sidebar */}
      <Route path="/admin" element={<AdminRoute><AdminLayout /></AdminRoute>}>
        <Route index element={<AdminDashboard />} />
        <Route path="products" element={<AdminProducts />} />
        <Route path="orders" element={<AdminOrders />} />
        <Route path="users" element={<AdminUsers />} />
        <Route path="categories" element={<AdminCategories />} />
      </Route>

      {/* 404 */}
      <Route path="*" element={
        <MainLayout>
          <div className="flex flex-col items-center justify-center min-h-[60vh] text-center">
            <h1 className="text-6xl font-bold text-gray-200 mb-4">404</h1>
            <p className="text-xl font-semibold text-gray-900 mb-2">Page Not Found</p>
            <p className="text-gray-500 mb-6">The page you're looking for doesn't exist.</p>
            <a href="/" className="btn-primary">Go Home</a>
          </div>
        </MainLayout>
      } />
    </Routes>
  );
}
