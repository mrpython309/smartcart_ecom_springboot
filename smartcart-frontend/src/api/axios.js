import axios from 'axios';

const API = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: { 'Content-Type': 'application/json' },
});

// Request interceptor — attach JWT token
API.interceptors.request.use((config) => {
  const token = localStorage.getItem('smartcart_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => Promise.reject(error));

// Response interceptor — handle 401
API.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('smartcart_token');
      localStorage.removeItem('smartcart_user');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    // Return a more user-friendly error message if available
    const message = error.response?.data?.message || error.message || 'Something went wrong';
    error.friendlyMessage = message;
    return Promise.reject(error);
  }
);

export default API;
