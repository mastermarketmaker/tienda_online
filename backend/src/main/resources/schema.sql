-- FuelUp Database Schema
-- Ejecutar en pgAdmin sobre la base de datos fuelup_db

-- ========================
-- TIPOS ENUM
-- ========================
DO $$ BEGIN
    CREATE TYPE role_type AS ENUM ('USER', 'ADMIN');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE order_status AS ENUM ('PENDING','CONFIRMED','PROCESSING','SHIPPED','DELIVERED','CANCELLED','REFUNDED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE payment_status AS ENUM ('PENDING','COMPLETED','FAILED','REFUNDED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TYPE discount_type AS ENUM ('PERCENTAGE','FIXED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- ========================
-- USUARIOS
-- ========================
CREATE TABLE IF NOT EXISTS users (
    id                    BIGSERIAL PRIMARY KEY,
    name                  VARCHAR(255) NOT NULL,
    email                 VARCHAR(255) NOT NULL UNIQUE,
    password              VARCHAR(255) NOT NULL,
    phone                 VARCHAR(50),
    role                  VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled               BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified        BOOLEAN NOT NULL DEFAULT FALSE,
    verification_token    VARCHAR(255),
    reset_password_token  VARCHAR(255),
    reset_password_expiry TIMESTAMP,
    created_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP
);

-- ========================
-- DIRECCIONES
-- ========================
CREATE TABLE IF NOT EXISTS addresses (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    street      VARCHAR(255) NOT NULL,
    city        VARCHAR(100) NOT NULL,
    province    VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20)  NOT NULL,
    country     VARCHAR(100) NOT NULL
);

-- ========================
-- CATEGORÍAS
-- ========================
CREATE TABLE IF NOT EXISTS categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    image_url   VARCHAR(500),
    slug        VARCHAR(150) UNIQUE,
    active      BOOLEAN NOT NULL DEFAULT TRUE
);

-- ========================
-- PRODUCTOS
-- ========================
CREATE TABLE IF NOT EXISTS products (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    description    TEXT,
    price          NUMERIC(10,2) NOT NULL,
    discount_price NUMERIC(10,2),
    stock          INTEGER NOT NULL DEFAULT 0,
    image_url      VARCHAR(500),
    slug           VARCHAR(255) UNIQUE,
    brand          VARCHAR(100),
    flavor         VARCHAR(100),
    weight         VARCHAR(50),
    active         BOOLEAN NOT NULL DEFAULT TRUE,
    featured       BOOLEAN NOT NULL DEFAULT FALSE,
    category_id    BIGINT REFERENCES categories(id),
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP
);

-- ========================
-- CARRITOS
-- ========================
CREATE TABLE IF NOT EXISTS carts (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS cart_items (
    id         BIGSERIAL PRIMARY KEY,
    cart_id    BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity   INTEGER NOT NULL,
    UNIQUE (cart_id, product_id)
);

-- ========================
-- PEDIDOS
-- ========================
CREATE TABLE IF NOT EXISTS orders (
    id                BIGSERIAL PRIMARY KEY,
    order_number      VARCHAR(50) NOT NULL UNIQUE,
    user_id           BIGINT NOT NULL REFERENCES users(id),
    status            VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    subtotal          NUMERIC(10,2) NOT NULL,
    shipping_cost     NUMERIC(10,2) NOT NULL DEFAULT 4.99,
    discount          NUMERIC(10,2) NOT NULL DEFAULT 0,
    total             NUMERIC(10,2) NOT NULL,
    coupon_code       VARCHAR(50),
    shipping_street   VARCHAR(255),
    shipping_city     VARCHAR(100),
    shipping_province VARCHAR(100),
    shipping_postal_code VARCHAR(20),
    shipping_country  VARCHAR(100),
    notes             TEXT,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP,
    shipped_at        TIMESTAMP,
    delivered_at      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity   INTEGER NOT NULL,
    unit_price NUMERIC(10,2) NOT NULL
);

-- ========================
-- RESEÑAS
-- ========================
CREATE TABLE IF NOT EXISTS reviews (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    rating     INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title      VARCHAR(255),
    comment    TEXT,
    verified   BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, product_id)
);

-- ========================
-- WISHLIST
-- ========================
CREATE TABLE IF NOT EXISTS wishlists (
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS wishlist_products (
    wishlist_id BIGINT NOT NULL REFERENCES wishlists(id) ON DELETE CASCADE,
    product_id  BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    PRIMARY KEY (wishlist_id, product_id)
);

-- ========================
-- PAGOS
-- ========================
CREATE TABLE IF NOT EXISTS payments (
    id                        BIGSERIAL PRIMARY KEY,
    order_id                  BIGINT NOT NULL UNIQUE REFERENCES orders(id),
    stripe_payment_intent_id  VARCHAR(255),
    stripe_charge_id          VARCHAR(255),
    status                    VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    amount                    NUMERIC(10,2) NOT NULL,
    currency                  VARCHAR(10),
    receipt_url               VARCHAR(500),
    created_at                TIMESTAMP NOT NULL DEFAULT NOW(),
    paid_at                   TIMESTAMP
);

-- ========================
-- CUPONES
-- ========================
CREATE TABLE IF NOT EXISTS coupons (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(50) NOT NULL UNIQUE,
    discount_type       VARCHAR(20) NOT NULL,
    discount_value      NUMERIC(10,2) NOT NULL,
    minimum_order_amount NUMERIC(10,2),
    maximum_discount    NUMERIC(10,2),
    active              BOOLEAN NOT NULL DEFAULT TRUE,
    usage_limit         INTEGER,
    usage_count         INTEGER NOT NULL DEFAULT 0,
    expires_at          TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ========================
-- ÍNDICES
-- ========================
CREATE INDEX IF NOT EXISTS idx_products_category  ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_active    ON products(active);
CREATE INDEX IF NOT EXISTS idx_products_featured  ON products(featured);
CREATE INDEX IF NOT EXISTS idx_orders_user        ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status      ON orders(status);
CREATE INDEX IF NOT EXISTS idx_reviews_product    ON reviews(product_id);

-- ========================
-- DATOS INICIALES
-- ========================

-- Admin (contraseña: Admin1234!)
INSERT INTO users (name, email, password, role, email_verified)
VALUES ('Admin FuelUp', 'admin@fuelup.es',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa',
        'ADMIN', true)
ON CONFLICT (email) DO NOTHING;

-- Categorías
INSERT INTO categories (name, slug, description) VALUES
    ('Proteínas',   'proteinas',   'Whey, caseína y más'),
    ('Creatinas',   'creatinas',   'Para fuerza y volumen'),
    ('Aminoácidos', 'aminoacidos', 'BCAA y EAA'),
    ('Pre-Workout', 'pre-workout', 'Energía explosiva'),
    ('Vitaminas',   'vitaminas',   'Salud y bienestar')
ON CONFLICT (name) DO NOTHING;

-- Productos
INSERT INTO products (name, slug, description, price, discount_price, stock, brand, flavor, weight, featured, category_id)
SELECT 'Whey Protein Pro', 'whey-protein-pro',
       '25g de proteína por servicio. Recuperación muscular máxima.',
       39.99, NULL, 100, 'FuelUp', 'Chocolate', '1kg', true, id
FROM categories WHERE slug = 'proteinas'
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (name, slug, description, price, stock, brand, weight, featured, category_id)
SELECT 'Creatina Monohidrato', 'creatina-monohidrato',
       'Aumenta tu fuerza y resistencia. Micronizada para máxima absorción.',
       24.99, 80, 'FuelUp', '500g', true, id
FROM categories WHERE slug = 'creatinas'
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (name, slug, description, price, stock, brand, flavor, weight, featured, category_id)
SELECT 'BCAA Essential 2:1:1', 'bcaa-essential',
       'Aminoácidos de cadena ramificada para reducir la fatiga muscular.',
       29.99, 60, 'FuelUp', 'Sandía', '300g', false, id
FROM categories WHERE slug = 'aminoacidos'
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (name, slug, description, price, stock, brand, flavor, weight, featured, category_id)
SELECT 'Pre-Workout Extreme', 'pre-workout-extreme',
       'Energía explosiva con beta-alanina y cafeína.',
       34.99, 50, 'FuelUp', 'Frutas del bosque', '400g', true, id
FROM categories WHERE slug = 'pre-workout'
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (name, slug, description, price, stock, brand, weight, featured, category_id)
SELECT 'Omega 3 Ultra', 'omega-3-ultra',
       'Ácidos grasos esenciales para salud cardiovascular y articular.',
       19.99, 120, 'FuelUp', '90 cápsulas', false, id
FROM categories WHERE slug = 'vitaminas'
ON CONFLICT (slug) DO NOTHING;

INSERT INTO products (name, slug, description, price, discount_price, stock, brand, flavor, weight, featured, category_id)
SELECT 'Mass Gainer 3000', 'mass-gainer-3000',
       'Volumen y masa muscular con carbohidratos complejos.',
       44.99, 35.99, 40, 'FuelUp', 'Vainilla', '3kg', true, id
FROM categories WHERE slug = 'proteinas'
ON CONFLICT (slug) DO NOTHING;

-- Cupones
INSERT INTO coupons (code, discount_type, discount_value, minimum_order_amount, expires_at)
VALUES
    ('FUELUP10',   'PERCENTAGE', 10, 30.00, NOW() + INTERVAL '6 months'),
    ('BIENVENIDO', 'FIXED',       5, 20.00, NOW() + INTERVAL '3 months')
ON CONFLICT (code) DO NOTHING;

-- Carrito y wishlist del admin
INSERT INTO carts (user_id)
SELECT id FROM users WHERE email = 'admin@fuelup.es'
ON CONFLICT DO NOTHING;

INSERT INTO wishlists (user_id)
SELECT id FROM users WHERE email = 'admin@fuelup.es'
ON CONFLICT DO NOTHING;
