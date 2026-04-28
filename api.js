const API = 'http://localhost:8080/api';

function getToken() { return localStorage.getItem('fuelup_token'); }
function setToken(t) { localStorage.setItem('fuelup_token', t); }
function removeToken() { localStorage.removeItem('fuelup_token'); }
function getUser() { return JSON.parse(localStorage.getItem('fuelup_user') || 'null'); }
function setUser(u) { localStorage.setItem('fuelup_user', JSON.stringify(u)); }
function removeUser() { localStorage.removeItem('fuelup_user'); }

async function request(path, options = {}) {
  const token = getToken();
  const headers = { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}) };
  const res = await fetch(API + path, { ...options, headers: { ...headers, ...options.headers } });
  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);
  if (!res.ok) throw new Error(data?.message || 'Error en la solicitud');
  return data;
}

const Api = {
  // AUTH
  login: (email, password) => request('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),
  register: (name, email, password, phone) => request('/auth/register', { method: 'POST', body: JSON.stringify({ name, email, password, phone }) }),

  // PRODUCTS
  getProducts: (params = {}) => {
    const q = new URLSearchParams(params).toString();
    return request(`/products?${q}`);
  },
  getFeatured: () => request('/products/featured'),
  getCategories: () => request('/categories'),

  // CART
  getCart: () => request('/cart'),
  addToCart: (productId, quantity = 1) => request(`/cart/items/${productId}?quantity=${quantity}`, { method: 'POST' }),
  updateCartItem: (itemId, quantity) => request(`/cart/items/${itemId}?quantity=${quantity}`, { method: 'PUT' }),
  removeCartItem: (itemId) => request(`/cart/items/${itemId}`, { method: 'DELETE' }),
  clearCart: () => request('/cart', { method: 'DELETE' }),

  // ORDERS
  createOrder: (body) => request('/orders', { method: 'POST', body: JSON.stringify(body) }),
  getMyOrders: () => request('/orders'),

  // WISHLIST
  getWishlist: () => request('/wishlist'),
  toggleWishlist: (productId) => request(`/wishlist/${productId}`, { method: 'POST' }),

  // REVIEWS
  getReviews: (productId) => request(`/reviews/product/${productId}`),
  createReview: (productId, body) => request(`/reviews/product/${productId}`, { method: 'POST', body: JSON.stringify(body) }),
};
