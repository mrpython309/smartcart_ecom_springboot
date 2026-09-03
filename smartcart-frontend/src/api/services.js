import API from './axios';

export const authAPI = {
  register: (data) => API.post('/auth/register', data),
  login: (data) => API.post('/auth/login', data),
};

export const userAPI = {
  getProfile: () => API.get('/users/profile'),
  updateProfile: (data) => API.put('/users/profile', data),
  getAddresses: () => API.get('/users/addresses'),
  addAddress: (data) => API.post('/users/addresses', data),
  updateAddress: (id, data) => API.put(`/users/addresses/${id}`, data),
  deleteAddress: (id) => API.delete(`/users/addresses/${id}`),
};

export const productAPI = {
  getAll: (params) => API.get('/products', { params }),
  getById: (id) => API.get(`/products/${id}`),
  search: (params) => API.get('/products/search', { params }),
  filter: (params) => API.get('/products/filter', { params }),
  getByCategory: (categoryId, params) => API.get(`/products/category/${categoryId}`, { params }),
};

export const categoryAPI = {
  getAll: () => API.get('/categories'),
  getById: (id) => API.get(`/categories/${id}`),
};

export const cartAPI = {
  get: () => API.get('/cart'),
  addItem: (data) => API.post('/cart/add', data),
  updateItem: (itemId, quantity) => API.put(`/cart/items/${itemId}?quantity=${quantity}`),
  removeItem: (itemId) => API.delete(`/cart/items/${itemId}`),
  clear: () => API.delete('/cart/clear'),
};

export const orderAPI = {
  place: (data) => API.post('/orders', data),
  getAll: () => API.get('/orders'),
  getById: (id) => API.get(`/orders/${id}`),
  cancelOrder: (id) => API.post(`/orders/${id}/cancel`),
};

export const paymentAPI = {
  createOrder: (data) => API.post('/payments/create-order', data),
  verify: (data) => API.post('/payments/verify', data),
};

export const adminAPI = {
  getDashboard: () => API.get('/admin/dashboard'),
  getUsers: (params) => API.get('/admin/users', { params }),
  updateUserRole: (userId, role) => API.put(`/admin/users/${userId}/role?role=${role}`),
  createProduct: (data) => API.post('/admin/products', data),
  updateProduct: (id, data) => API.put(`/admin/products/${id}`, data),
  deleteProduct: (id) => API.delete(`/admin/products/${id}`),
  createCategory: (data) => API.post('/admin/categories', data),
  updateCategory: (id, data) => API.put(`/admin/categories/${id}`, data),
  deleteCategory: (id) => API.delete(`/admin/categories/${id}`),
  getOrders: (params) => API.get('/admin/orders', { params }),
  updateOrderStatus: (orderId, status) => API.put(`/admin/orders/${orderId}/status?status=${status}`),
};

