// Navbar scroll effect
const navbar = document.getElementById('navbar');
window.addEventListener('scroll', () => {
  navbar.classList.toggle('scrolled', window.scrollY > 50);
});

// Scroll reveal animation
const revealElements = document.querySelectorAll('.card, .beneficio, .testimonio, .contacto-info, .contacto-form');

revealElements.forEach(el => el.classList.add('reveal'));

const observer = new IntersectionObserver((entries) => {
  entries.forEach((entry, i) => {
    if (entry.isIntersecting) {
      setTimeout(() => entry.target.classList.add('visible'), i * 80);
    }
  });
}, { threshold: 0.1 });

revealElements.forEach(el => observer.observe(el));

// Smooth active nav link
const sections = document.querySelectorAll('section[id]');
const navLinks = document.querySelectorAll('.nav-links a');

window.addEventListener('scroll', () => {
  let current = '';
  sections.forEach(section => {
    if (window.scrollY >= section.offsetTop - 200) {
      current = section.getAttribute('id');
    }
  });
  navLinks.forEach(link => {
    link.style.color = link.getAttribute('href') === `#${current}` ? '#ff6b35' : '';
  });
});

// Add to cart button effect
document.querySelectorAll('.btn-add').forEach(btn => {
  btn.addEventListener('click', function () {
    const original = this.textContent;
    this.textContent = '✓ Añadido';
    this.style.background = '#00b894';
    this.style.borderColor = '#00b894';
    this.style.color = '#fff';
    setTimeout(() => {
      this.textContent = original;
      this.style.background = '';
      this.style.borderColor = '';
      this.style.color = '';
    }, 2000);
  });
});

// Contact form submit
document.querySelector('.contacto-form').addEventListener('submit', function (e) {
  e.preventDefault();
  const btn = this.querySelector('button[type="submit"]');
  btn.textContent = '✓ Mensaje enviado';
  btn.style.background = '#00b894';
  setTimeout(() => {
    btn.textContent = 'Enviar mensaje';
    btn.style.background = '';
    this.reset();
  }, 3000);
});

// Hamburger menu (mobile)
document.getElementById('hamburger').addEventListener('click', () => {
  const links = document.querySelector('.nav-links');
  const btnNav = document.querySelector('.btn-nav');
  if (links.style.display === 'flex') {
    links.style.display = 'none';
    btnNav.style.display = 'none';
  } else {
    links.style.display = 'flex';
    links.style.flexDirection = 'column';
    links.style.position = 'absolute';
    links.style.top = '70px';
    links.style.left = '0';
    links.style.right = '0';
    links.style.background = 'rgba(13,13,13,0.98)';
    links.style.padding = '2rem';
    links.style.gap = '1.5rem';
    btnNav.style.display = 'block';
    btnNav.style.position = 'absolute';
    btnNav.style.top = '200px';
    btnNav.style.left = '50%';
    btnNav.style.transform = 'translateX(-50%)';
  }
});
