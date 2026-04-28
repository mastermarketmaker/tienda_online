// ===== ESTADO =====
let currentPage = 0;
let currentCategory = '';
let currentSearch = '';
let currentSort = 'createdAt,desc';
let totalPages = 0;
let wishlistIds = new Set();
let searchTimer;

// ===== TOAST =====
function toast(msg, type = 'success') {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.className = `toast-show toast-${type}`;
  setTimeout(() => t.className = '', 3000);
}

// ===== NAVBAR SCROLL =====
const navbar = document.getElementById('navbar');
window.addEventListener('scroll', () => navbar.classList.toggle('scrolled', window.scrollY > 50));

// ===== AUTH UI =====
function updateAuthUI() {
  const user = getUser();
  const navAuth = document.getElementById('nav-auth');
  const navUser = document.getElementById('nav-user');
  const userName = document.getElementById('user-name');
  if (user) {
    navAuth.classList.add('hidden');
    navUser.classList.remove('hidden');
    userName.textContent = user.name.split(' ')[0];
  } else {
    navAuth.classList.remove('hidden');
    navUser.classList.add('hidden');
  }
}

// ===== MODAL AUTH =====
const overlay = document.getElementById('modal-overlay');
document.getElementById('btn-open-auth').addEventListener('click', e => { e.preventDefault(); openModal('login'); });
document.getElementById('modal-close').addEventListener('click', () => overlay.classList.add('hidden'));
overlay.addEventListener('click', e => { if (e.target === overlay) overlay.classList.add('hidden'); });

function openModal(tab) {
  overlay.classList.remove('hidden');
  switchTab(tab);
}

document.querySelectorAll('.modal-tab').forEach(btn => {
  btn.addEventListener('click', () => switchTab(btn.dataset.tab));
});
document.querySelectorAll('.modal-switch a').forEach(a => {
  a.addEventListener('click', e => { e.preventDefault(); switchTab(a.dataset.tab); });
});

function switchTab(tab) {
  document.querySelectorAll('.modal-tab').forEach(b => b.classList.toggle('active', b.dataset.tab === tab));
  document.getElementById('form-login').classList.toggle('hidden', tab !== 'login');
  document.getElementById('form-register').classList.toggle('hidden', tab !== 'register');
}

// LOGIN
document.getElementById('form-login').addEventListener('submit', async e => {
  e.preventDefault();
  const email = document.getElementById('login-email').value;
  const password = document.getElementById('login-password').value;
  try {
    const res = await Api.login(email, password);
    setToken(res.accessToken);
    setUser(res.user);
    overlay.classList.add('hidden');
    updateAuthUI();
    loadCart();
    loadWishlist();
    toast(`¡Bienvenido, ${res.user.name.split(' ')[0]}!`);
  } catch (err) {
    toast(err.message, 'error');
  }
});

// REGISTER
document.getElementById('form-register').addEventListener('submit', async e => {
  e.preventDefault();
  const name = document.getElementById('reg-name').value;
  const email = document.getElementById('reg-email').value;
  const password = document.getElementById('reg-password').value;
  const phone = document.getElementById('reg-phone').value;
  try {
    const res = await Api.register(name, email, password, phone);
    setToken(res.accessToken);
    setUser(res.user);
    overlay.classList.add('hidden');
    updateAuthUI();
    loadCart();
    toast(`¡Cuenta creada! Bienvenido, ${res.user.name.split(' ')[0]}!`);
  } catch (err) {
    toast(err.message, 'error');
  }
});

// LOGOUT
document.getElementById('btn-logout').addEventListener('click', e => {
  e.preventDefault();
  removeToken(); removeUser();
  updateAuthUI();
  renderCart({ items: [], total: 0, totalItems: 0 });
  wishlistIds.clear();
  renderProducts([]);
  loadProducts();
  toast('Sesión cerrada');
  document.getElementById('user-dropdown').classList.remove('open');
});

// USER DROPDOWN
document.getElementById('user-btn').addEventListener('click', () => {
  document.getElementById('user-dropdown').classList.toggle('open');
});
document.addEventListener('click', e => {
  if (!e.target.closest('.user-menu')) document.getElementById('user-dropdown').classList.remove('open');
});

// ===== CARRITO SIDEBAR =====
document.getElementById('btn-cart').addEventListener('click', openCart);
document.getElementById('cart-close').addEventListener('click', closeCart);
document.getElementById('cart-overlay').addEventListener('click', closeCart);

function openCart() {
  document.getElementById('cart-sidebar').classList.remove('closed');
  document.getElementById('cart-overlay').classList.remove('hidden');
  if (getToken()) loadCart();
}
function closeCart() {
  document.getElementById('cart-sidebar').classList.add('closed');
  document.getElementById('cart-overlay').classList.add('hidden');
}

async function loadCart() {
  if (!getToken()) return;
  try {
    const cart = await Api.getCart();
    renderCart(cart);
  } catch {}
}

function renderCart(cart) {
  const items = cart.items || [];
  const count = cart.totalItems || 0;
  const total = cart.total || 0;

  const badge = document.getElementById('cart-count');
  badge.textContent = count;
  badge.classList.toggle('hidden', count === 0);

  document.getElementById('cart-total-price').textContent = formatPrice(total);

  const container = document.getElementById('cart-items');
  if (items.length === 0) {
    container.innerHTML = '<div class="cart-empty"><p>Tu carrito está vacío</p><a href="#productos" onclick="closeCart()">Ver productos</a></div>';
    return;
  }

  container.innerHTML = items.map(item => `
    <div class="cart-item" data-item-id="${item.id}">
      <div class="cart-item-emoji">${getCategoryEmoji(item.productName)}</div>
      <div class="cart-item-info">
        <strong>${item.productName}</strong>
        <span>${formatPrice(item.unitPrice)}</span>
      </div>
      <div class="cart-item-qty">
        <button onclick="changeQty(${item.id}, ${item.quantity - 1})">−</button>
        <span>${item.quantity}</span>
        <button onclick="changeQty(${item.id}, ${item.quantity + 1})">+</button>
      </div>
      <button class="cart-item-remove" onclick="removeItem(${item.id})">✕</button>
    </div>
  `).join('');
}

async function changeQty(itemId, qty) {
  if (!getToken()) { openModal('login'); return; }
  try {
    const cart = await Api.updateCartItem(itemId, qty);
    renderCart(cart);
  } catch (err) { toast(err.message, 'error'); }
}

async function removeItem(itemId) {
  if (!getToken()) return;
  try {
    const cart = await Api.removeCartItem(itemId);
    renderCart(cart);
  } catch (err) { toast(err.message, 'error'); }
}

async function addToCart(productId) {
  if (!getToken()) { openModal('login'); return; }
  try {
    const cart = await Api.addToCart(productId, 1);
    renderCart(cart);
    openCart();
    toast('Producto añadido al carrito ✓');
  } catch (err) { toast(err.message, 'error'); }
}

// CHECKOUT
document.getElementById('btn-checkout').addEventListener('click', async () => {
  if (!getToken()) { openModal('login'); closeCart(); return; }
  try {
    const order = await Api.createOrder({});
    closeCart();
    toast(`Pedido ${order.orderNumber} creado ✓ Total: ${formatPrice(order.total)}`);
    loadCart();
  } catch (err) { toast(err.message, 'error'); }
});

// ===== WISHLIST =====
async function loadWishlist() {
  if (!getToken()) return;
  try {
    const items = await Api.getWishlist();
    wishlistIds = new Set(items.map(p => p.id));
    document.querySelectorAll('.wish-btn').forEach(btn => {
      const id = parseInt(btn.dataset.id);
      btn.textContent = wishlistIds.has(id) ? '❤️' : '🤍';
    });
  } catch {}
}

async function toggleWish(productId, btn) {
  if (!getToken()) { openModal('login'); return; }
  try {
    const items = await Api.toggleWishlist(productId);
    wishlistIds = new Set(items.map(p => p.id));
    btn.textContent = wishlistIds.has(productId) ? '❤️' : '🤍';
    toast(wishlistIds.has(productId) ? 'Añadido a favoritos ❤️' : 'Eliminado de favoritos');
  } catch (err) { toast(err.message, 'error'); }
}

document.getElementById('btn-wishlist').addEventListener('click', async e => {
  e.preventDefault();
  document.getElementById('user-dropdown').classList.remove('open');
  if (!getToken()) { openModal('login'); return; }
  try {
    const items = await Api.getWishlist();
    if (items.length === 0) { toast('Tu lista de deseos está vacía'); return; }
    toast(`Tienes ${items.length} producto(s) en favoritos`);
  } catch {}
});

// ===== MIS PEDIDOS =====
document.getElementById('btn-mis-pedidos').addEventListener('click', async e => {
  e.preventDefault();
  document.getElementById('user-dropdown').classList.remove('open');
  if (!getToken()) { openModal('login'); return; }
  try {
    const data = await Api.getMyOrders();
    const orders = data.content || [];
    const container = document.getElementById('pedidos-list');
    if (orders.length === 0) {
      container.innerHTML = '<p style="color:#999;text-align:center">No tienes pedidos todavía</p>';
    } else {
      container.innerHTML = orders.map(o => `
        <div class="pedido-card">
          <div class="pedido-header">
            <strong>${o.orderNumber}</strong>
            <span class="badge-status status-${o.status.toLowerCase()}">${translateStatus(o.status)}</span>
          </div>
          <div class="pedido-items">${o.items.map(i => `${i.productName} x${i.quantity}`).join(', ')}</div>
          <div class="pedido-footer">
            <span>${new Date(o.createdAt).toLocaleDateString('es-ES')}</span>
            <strong>${formatPrice(o.total)}</strong>
          </div>
        </div>
      `).join('');
    }
    document.getElementById('pedidos-overlay').classList.remove('hidden');
  } catch (err) { toast(err.message, 'error'); }
});
document.getElementById('pedidos-close').addEventListener('click', () => document.getElementById('pedidos-overlay').classList.add('hidden'));
document.getElementById('pedidos-overlay').addEventListener('click', e => { if (e.target.id === 'pedidos-overlay') e.target.classList.add('hidden'); });

// ===== CATEGORÍAS =====
async function loadCategories() {
  try {
    const cats = await Api.getCategories();
    const container = document.getElementById('categoria-btns');
    cats.forEach(cat => {
      const btn = document.createElement('button');
      btn.className = 'cat-btn';
      btn.dataset.id = cat.id;
      btn.textContent = cat.name;
      btn.addEventListener('click', () => filterByCategory(cat.id, btn));
      container.appendChild(btn);
    });
  } catch {}
}

function filterByCategory(id, btn) {
  currentCategory = id;
  currentPage = 0;
  document.querySelectorAll('.cat-btn').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  loadProducts();
}

// ===== PRODUCTOS =====
async function loadProducts() {
  const grid = document.getElementById('productos-grid');
  grid.innerHTML = '<div class="loading-spinner">Cargando productos...</div>';

  const [sortBy, direction] = currentSort.split(',');
  const params = { page: currentPage, size: 9, sortBy, direction };
  if (currentCategory) params.categoryId = currentCategory;
  if (currentSearch) params.search = currentSearch;

  try {
    const data = await Api.getProducts(params);
    totalPages = data.totalPages;
    renderProducts(data.content || []);
    renderPagination(data);
  } catch (err) {
    grid.innerHTML = `<p style="color:#ff6b35;text-align:center">Error cargando productos: ${err.message}</p>`;
  }
}

function renderProducts(products) {
  const grid = document.getElementById('productos-grid');
  if (products.length === 0) {
    grid.innerHTML = '<p style="color:#999;text-align:center;grid-column:1/-1;padding:3rem">No se encontraron productos</p>';
    return;
  }
  grid.innerHTML = products.map(p => {
    const price = p.discountPrice
      ? `<span class="price-old">${formatPrice(p.price)}</span> <span class="price">${formatPrice(p.discountPrice)}</span>`
      : `<span class="price">${formatPrice(p.price)}</span>`;
    const badge = p.featured ? '<div class="card-badge">DESTACADO</div>' : '';
    const outOfStock = p.stock === 0 ? '<div class="card-badge oferta">AGOTADO</div>' : '';
    const stars = p.averageRating > 0 ? '★'.repeat(Math.round(p.averageRating)) + `<small>(${p.reviewCount})</small>` : '';
    const wished = wishlistIds.has(p.id) ? '❤️' : '🤍';
    return `
      <div class="card reveal">
        ${badge}${outOfStock}
        <button class="wish-btn" data-id="${p.id}" onclick="toggleWish(${p.id}, this)">${wished}</button>
        <div class="card-img" style="background:${getCategoryColor(p.category?.name)}">
          <span class="card-emoji">${getCategoryEmoji(p.name)}</span>
        </div>
        <div class="card-body">
          <p class="card-brand">${p.brand || ''}</p>
          <h3>${p.name}</h3>
          <p>${(p.description || '').substring(0, 80)}...</p>
          ${stars ? `<div class="card-stars">${stars}</div>` : ''}
          <div class="card-footer">
            <div>${price}</div>
            <button class="btn-add" onclick="addToCart(${p.id})" ${p.stock === 0 ? 'disabled' : ''}>
              ${p.stock === 0 ? 'Agotado' : '+ Añadir'}
            </button>
          </div>
        </div>
      </div>`;
  }).join('');

  // Scroll reveal
  document.querySelectorAll('.card.reveal').forEach((el, i) => {
    setTimeout(() => el.classList.add('visible'), i * 60);
  });
}

function renderPagination(data) {
  const container = document.getElementById('pagination');
  if (data.totalPages <= 1) { container.innerHTML = ''; return; }
  let html = '';
  if (!data.first) html += `<button onclick="goPage(${currentPage - 1})">← Anterior</button>`;
  html += `<span>Página ${currentPage + 1} de ${data.totalPages}</span>`;
  if (!data.last) html += `<button onclick="goPage(${currentPage + 1})">Siguiente →</button>`;
  container.innerHTML = html;
}

function goPage(page) {
  currentPage = page;
  loadProducts();
  document.getElementById('productos').scrollIntoView({ behavior: 'smooth' });
}

// BÚSQUEDA
document.getElementById('search-input').addEventListener('input', e => {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    currentSearch = e.target.value.trim();
    currentPage = 0;
    loadProducts();
  }, 400);
});

// ORDENAR
document.getElementById('sort-select').addEventListener('change', e => {
  currentSort = e.target.value;
  currentPage = 0;
  loadProducts();
});

// ===== CONTACTO =====
document.getElementById('contacto-form').addEventListener('submit', e => {
  e.preventDefault();
  const btn = e.target.querySelector('button[type="submit"]');
  btn.textContent = '✓ Mensaje enviado';
  btn.style.background = '#00b894';
  setTimeout(() => { btn.textContent = 'Enviar mensaje'; btn.style.background = ''; e.target.reset(); }, 3000);
});

// ===== HELPERS =====
function formatPrice(n) {
  return Number(n).toLocaleString('es-ES', { minimumFractionDigits: 2 }) + '€';
}

function getCategoryColor(name = '') {
  const map = { 'Proteínas': '#1a2a3a', 'Creatinas': '#2d1b00', 'Aminoácidos': '#001a2a', 'Pre-Workout': '#2a0a0a', 'Vitaminas': '#001a10' };
  return map[name] || '#1a1a2e';
}

function getCategoryEmoji(name = '') {
  const n = name.toLowerCase();
  if (n.includes('whey') || n.includes('protein') || n.includes('gainer')) return '🥛';
  if (n.includes('creatin')) return '⚡';
  if (n.includes('bcaa') || n.includes('amino')) return '💪';
  if (n.includes('pre') || n.includes('workout')) return '🔥';
  if (n.includes('omega')) return '🐟';
  if (n.includes('vitamin') || n.includes('mineral')) return '💊';
  return '🏋️';
}

function translateStatus(s) {
  const map = { PENDING: 'Pendiente', CONFIRMED: 'Confirmado', PROCESSING: 'En proceso', SHIPPED: 'Enviado', DELIVERED: 'Entregado', CANCELLED: 'Cancelado', REFUNDED: 'Reembolsado' };
  return map[s] || s;
}

// ===== HAMBURGER =====
document.getElementById('hamburger').addEventListener('click', () => {
  const links = document.querySelector('.nav-links');
  links.style.display = links.style.display === 'flex' ? 'none' : 'flex';
  if (links.style.display === 'flex') {
    Object.assign(links.style, { flexDirection: 'column', position: 'absolute', top: '70px', left: 0, right: 0, background: 'rgba(13,13,13,0.98)', padding: '2rem', gap: '1.5rem' });
  }
});

// ===== INIT =====
(async () => {
  updateAuthUI();
  await loadCategories();
  await loadProducts();
  if (getToken()) {
    loadCart();
    loadWishlist();
  }
})();
