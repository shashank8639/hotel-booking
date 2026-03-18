-- =============================================================================
-- Hotel Booking System — V12: Module 16 Multi-Hotel Platform (Telangana)
-- Geo hierarchy + hotels catalog + room ownership. Backward compatible:
-- existing rooms are backfilled to Grand Horizon (Hyderabad).
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Roles: hotel owners
-- ---------------------------------------------------------------------------
ALTER TABLE roles DROP CHECK chk_roles_name;
ALTER TABLE roles
    ADD CONSTRAINT chk_roles_name
        CHECK (name IN ('ADMIN', 'CUSTOMER', 'HOTEL_OWNER'));

INSERT INTO roles (name)
SELECT 'HOTEL_OWNER'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'HOTEL_OWNER');

-- ---------------------------------------------------------------------------
-- Geography (scalable: add rows for India → world)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS countries (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    code            CHAR(2)         NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_countries PRIMARY KEY (id),
    CONSTRAINT uk_countries_code UNIQUE (code),
    CONSTRAINT uk_countries_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS states (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    country_id      BIGINT          NOT NULL,
    code            VARCHAR(10)     NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_states PRIMARY KEY (id),
    CONSTRAINT fk_states_country FOREIGN KEY (country_id) REFERENCES countries (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT uk_states_country_code UNIQUE (country_id, code),
    CONSTRAINT uk_states_country_name UNIQUE (country_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_states_country_id ON states (country_id);

CREATE TABLE IF NOT EXISTS cities (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    state_id        BIGINT          NOT NULL,
    name            VARCHAR(120)    NOT NULL,
    slug            VARCHAR(140)    NOT NULL,
    latitude        DECIMAL(10, 7)  NULL,
    longitude       DECIMAL(10, 7)  NULL,
    popular         TINYINT(1)      NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_cities PRIMARY KEY (id),
    CONSTRAINT fk_cities_state FOREIGN KEY (state_id) REFERENCES states (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT uk_cities_state_name UNIQUE (state_id, name),
    CONSTRAINT uk_cities_slug UNIQUE (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cities_state_id ON cities (state_id);
CREATE INDEX idx_cities_popular ON cities (popular);

-- ---------------------------------------------------------------------------
-- Amenities master + hotel link
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS amenities (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    code            VARCHAR(50)     NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    icon            VARCHAR(50)     NULL,
    category        VARCHAR(40)     NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_amenities PRIMARY KEY (id),
    CONSTRAINT uk_amenities_code UNIQUE (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------------
-- Hotels
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS hotels (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    city_id             BIGINT          NOT NULL,
    owner_user_id       BIGINT          NULL,
    name                VARCHAR(200)    NOT NULL,
    slug                VARCHAR(220)    NOT NULL,
    description         TEXT            NULL,
    category            VARCHAR(40)     NOT NULL DEFAULT 'HOTEL',
    star_rating         TINYINT         NOT NULL DEFAULT 3,
    status              VARCHAR(30)     NOT NULL DEFAULT 'PENDING_APPROVAL',
    verified            TINYINT(1)      NOT NULL DEFAULT 0,
    featured            TINYINT(1)      NOT NULL DEFAULT 0,
    avg_rating          DECIMAL(3, 2)   NOT NULL DEFAULT 0.00,
    review_count        INT             NOT NULL DEFAULT 0,
    min_price           DECIMAL(12, 2)  NULL,
    currency            CHAR(3)         NOT NULL DEFAULT 'INR',
    address_line1       VARCHAR(255)    NOT NULL,
    address_line2       VARCHAR(255)    NULL,
    postal_code         VARCHAR(20)     NULL,
    latitude            DECIMAL(10, 7)  NULL,
    longitude           DECIMAL(10, 7)  NULL,
    phone               VARCHAR(30)     NULL,
    email               VARCHAR(255)    NULL,
    website             VARCHAR(255)    NULL,
    check_in_time       VARCHAR(10)     NULL DEFAULT '14:00',
    check_out_time      VARCHAR(10)     NULL DEFAULT '11:00',
    breakfast_included  TINYINT(1)      NOT NULL DEFAULT 0,
    free_cancellation   TINYINT(1)      NOT NULL DEFAULT 0,
    pet_friendly        TINYINT(1)      NOT NULL DEFAULT 0,
    rejection_reason    VARCHAR(500)    NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_hotels PRIMARY KEY (id),
    CONSTRAINT fk_hotels_city FOREIGN KEY (city_id) REFERENCES cities (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_hotels_owner FOREIGN KEY (owner_user_id) REFERENCES users (id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT uk_hotels_slug UNIQUE (slug),
    CONSTRAINT chk_hotels_star CHECK (star_rating BETWEEN 1 AND 5),
    CONSTRAINT chk_hotels_status CHECK (status IN (
        'DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'SUSPENDED'
    )),
    CONSTRAINT chk_hotels_category CHECK (category IN (
        'HOTEL', 'LUXURY', 'BUDGET', 'BUSINESS', 'RESORT', 'APARTMENT', 'HOMESTAY'
    ))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_hotels_city_id ON hotels (city_id);
CREATE INDEX idx_hotels_status_verified ON hotels (status, verified);
CREATE INDEX idx_hotels_featured ON hotels (featured);
CREATE INDEX idx_hotels_star_rating ON hotels (star_rating);
CREATE INDEX idx_hotels_min_price ON hotels (min_price);
CREATE INDEX idx_hotels_owner_user_id ON hotels (owner_user_id);
CREATE INDEX idx_hotels_category ON hotels (category);

CREATE TABLE IF NOT EXISTS hotel_images (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    hotel_id        BIGINT          NOT NULL,
    image_url       VARCHAR(500)    NOT NULL,
    caption         VARCHAR(255)    NULL,
    display_order   INT             NOT NULL DEFAULT 0,
    is_primary      TINYINT(1)      NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_hotel_images PRIMARY KEY (id),
    CONSTRAINT fk_hotel_images_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_hotel_images_hotel_id ON hotel_images (hotel_id);

CREATE TABLE IF NOT EXISTS hotel_amenities (
    hotel_id        BIGINT NOT NULL,
    amenity_id      BIGINT NOT NULL,
    CONSTRAINT pk_hotel_amenities PRIMARY KEY (hotel_id, amenity_id),
    CONSTRAINT fk_hotel_amenities_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_hotel_amenities_amenity FOREIGN KEY (amenity_id) REFERENCES amenities (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS hotel_policies (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    hotel_id        BIGINT          NOT NULL,
    policy_type     VARCHAR(40)     NOT NULL,
    title           VARCHAR(120)    NOT NULL,
    body            TEXT            NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_hotel_policies PRIMARY KEY (id),
    CONSTRAINT fk_hotel_policies_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_hotel_policies_type CHECK (policy_type IN (
        'CHECK_IN', 'CHECK_OUT', 'CANCELLATION', 'CHILD', 'PET', 'SMOKING', 'OTHER'
    ))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_hotel_policies_hotel_id ON hotel_policies (hotel_id);

CREATE TABLE IF NOT EXISTS hotel_reviews (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    hotel_id        BIGINT          NOT NULL,
    user_id         BIGINT          NULL,
    guest_name      VARCHAR(120)    NOT NULL,
    rating          TINYINT         NOT NULL,
    title           VARCHAR(160)    NULL,
    body            TEXT            NULL,
    verified_stay   TINYINT(1)      NOT NULL DEFAULT 0,
    status          VARCHAR(20)     NOT NULL DEFAULT 'PUBLISHED',
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_hotel_reviews PRIMARY KEY (id),
    CONSTRAINT fk_hotel_reviews_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_hotel_reviews_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT chk_hotel_reviews_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_hotel_reviews_status CHECK (status IN ('PENDING', 'PUBLISHED', 'HIDDEN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_hotel_reviews_hotel_id ON hotel_reviews (hotel_id);
CREATE INDEX idx_hotel_reviews_rating ON hotel_reviews (hotel_id, rating);

-- ---------------------------------------------------------------------------
-- Rooms belong to hotels (compat backfill)
-- ---------------------------------------------------------------------------
ALTER TABLE rooms
    ADD COLUMN hotel_id BIGINT NULL AFTER id;

INSERT INTO countries (code, name) VALUES ('IN', 'India');

INSERT INTO states (country_id, code, name)
SELECT id, 'TS', 'Telangana' FROM countries WHERE code = 'IN';

INSERT INTO cities (state_id, name, slug, latitude, longitude, popular)
SELECT s.id, v.name, v.slug, v.lat, v.lng, v.popular
FROM states s
JOIN (
    SELECT 'Hyderabad' AS name, 'hyderabad' AS slug, 17.3850000 AS lat, 78.4867000 AS lng, 1 AS popular
    UNION ALL SELECT 'Warangal', 'warangal', 17.9689000, 79.5941000, 1
    UNION ALL SELECT 'Karimnagar', 'karimnagar', 18.4386000, 79.1288000, 1
    UNION ALL SELECT 'Nizamabad', 'nizamabad', 18.6725000, 78.0941000, 1
    UNION ALL SELECT 'Khammam', 'khammam', 17.2473000, 80.1514000, 1
    UNION ALL SELECT 'Mahabubnagar', 'mahabubnagar', 16.7488000, 78.0035000, 0
    UNION ALL SELECT 'Siddipet', 'siddipet', 18.1018000, 78.8520000, 0
    UNION ALL SELECT 'Nalgonda', 'nalgonda', 17.0575000, 79.2670000, 0
    UNION ALL SELECT 'Adilabad', 'adilabad', 19.6641000, 78.5320000, 0
    UNION ALL SELECT 'Medak', 'medak', 18.0330000, 78.2610000, 0
    UNION ALL SELECT 'Suryapet', 'suryapet', 17.1400000, 79.6200000, 0
    UNION ALL SELECT 'Jagtial', 'jagtial', 18.7947000, 78.9119000, 0
    UNION ALL SELECT 'Ramagundam', 'ramagundam', 18.7550000, 79.4740000, 0
    UNION ALL SELECT 'Vikarabad', 'vikarabad', 17.3381000, 77.9043000, 0
    UNION ALL SELECT 'Bhadrachalam', 'bhadrachalam', 17.6688000, 80.8889000, 1
    UNION ALL SELECT 'Kothagudem', 'kothagudem', 17.5511000, 80.6178000, 0
    UNION ALL SELECT 'Sangareddy', 'sangareddy', 17.6194000, 78.0867000, 0
    UNION ALL SELECT 'Malkajgiri', 'malkajgiri', 17.4519000, 78.5362000, 0
    UNION ALL SELECT 'Medchal', 'medchal', 17.6297000, 78.4828000, 0
) v
WHERE s.code = 'TS';

INSERT INTO amenities (code, name, icon, category) VALUES
 ('WIFI', 'Free WiFi', 'wifi', 'CONNECTIVITY'),
 ('PARKING', 'Parking', 'local_parking', 'TRANSPORT'),
 ('POOL', 'Swimming Pool', 'pool', 'LEISURE'),
 ('AC', 'Air Conditioning', 'ac_unit', 'COMFORT'),
 ('BREAKFAST', 'Breakfast Included', 'free_breakfast', 'FOOD'),
 ('PETS', 'Pet Friendly', 'pets', 'POLICY'),
 ('GYM', 'Fitness Center', 'fitness_center', 'LEISURE'),
 ('SPA', 'Spa', 'spa', 'LEISURE'),
 ('RESTAURANT', 'Restaurant', 'restaurant', 'FOOD'),
 ('ROOM_SERVICE', 'Room Service', 'room_service', 'SERVICE');

INSERT INTO hotels (
    city_id, name, slug, description, category, star_rating, status, verified, featured,
    avg_rating, review_count, min_price, currency, address_line1, postal_code,
    latitude, longitude, phone, email, breakfast_included, free_cancellation
)
SELECT c.id,
       'Grand Horizon Hotel',
       'grand-horizon-hyderabad',
       'Contemporary stays with strong service — migrated from the Module 1–15 single-hotel product. Now listed on the Telangana multi-hotel platform.',
       'BUSINESS',
       4,
       'APPROVED',
       1,
       1,
       4.50,
       1,
       2500.00,
       'INR',
       '12 Banjara Hills Road',
       '500034',
       17.4239000,
       78.4483000,
       '+91-40-0000-0000',
       'stay@grandhorizon.example',
       1,
       1
FROM cities c WHERE c.slug = 'hyderabad';

INSERT INTO hotel_images (hotel_id, image_url, caption, display_order, is_primary)
SELECT h.id,
       'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=1200&q=80',
       'Lobby & facade',
       0,
       1
FROM hotels h WHERE h.slug = 'grand-horizon-hyderabad';

INSERT INTO hotel_amenities (hotel_id, amenity_id)
SELECT h.id, a.id
FROM hotels h
CROSS JOIN amenities a
WHERE h.slug = 'grand-horizon-hyderabad'
  AND a.code IN ('WIFI', 'PARKING', 'AC', 'BREAKFAST', 'RESTAURANT');

INSERT INTO hotel_policies (hotel_id, policy_type, title, body)
SELECT h.id, 'CANCELLATION', 'Free cancellation',
       'Free cancellation up to 24 hours before check-in for most flexible rates.'
FROM hotels h WHERE h.slug = 'grand-horizon-hyderabad';

INSERT INTO hotel_reviews (hotel_id, guest_name, rating, title, body, verified_stay, status)
SELECT h.id, 'Asha R.', 5, 'Excellent stay',
       'Clean rooms and helpful staff. Perfect for a Hyderabad business trip.',
       1, 'PUBLISHED'
FROM hotels h WHERE h.slug = 'grand-horizon-hyderabad';

INSERT INTO hotels (
    city_id, name, slug, description, category, star_rating, status, verified, featured,
    avg_rating, review_count, min_price, address_line1, breakfast_included, free_cancellation, pet_friendly
)
SELECT c.id, v.name, v.slug, v.description, v.category, v.stars, 'APPROVED', 1, v.featured,
       v.rating, 0, v.min_price, v.address, v.breakfast, v.cancel, v.pets
FROM cities c
JOIN (
    SELECT 'Warangal' AS city_slug, 'Kakatiya Heritage Inn' AS name, 'kakatiya-heritage-warangal' AS slug,
           'Near Warangal Fort — heritage-inspired budget stays.' AS description,
           'BUDGET' AS category, 3 AS stars, 1 AS featured, 4.10 AS rating, 1800.00 AS min_price,
           'Fort Road' AS address, 1 AS breakfast, 1 AS cancel, 0 AS pets
    UNION ALL SELECT 'Karimnagar', 'Godavari Business Hotel', 'godavari-business-karimnagar',
           'Practical rooms for regional travellers.',
           'BUSINESS', 3, 0, 3.90, 2200.00, 'Collectorate Road', 1, 0, 0
    UNION ALL SELECT 'Nizamabad', 'Nizam Comfort Suites', 'nizam-comfort-nizamabad',
           'Family-friendly suites with parking.',
           'HOTEL', 3, 0, 4.00, 2000.00, 'Station Road', 0, 1, 1
    UNION ALL SELECT 'Khammam', 'Munneru Riverside Resort', 'munneru-riverside-khammam',
           'Quiet riverside resort stay.',
           'RESORT', 4, 1, 4.40, 3500.00, 'River Front', 1, 1, 0
    UNION ALL SELECT 'Bhadrachalam', 'Temple View Lodge', 'temple-view-bhadrachalam',
           'Pilgrimage-friendly lodge near the temple town.',
           'BUDGET', 2, 1, 4.20, 1500.00, 'Temple Road', 1, 1, 0
    UNION ALL SELECT 'Hyderabad', 'Charminar Budget Stay', 'charminar-budget-hyderabad',
           'Walkable to Old City attractions.',
           'BUDGET', 2, 0, 3.80, 1200.00, 'Near Charminar', 0, 1, 0
    UNION ALL SELECT 'Hyderabad', 'Hitech Lakeview Luxury', 'hitech-lakeview-hyderabad',
           'Luxury tower near Hitech City.',
           'LUXURY', 5, 1, 4.70, 7500.00, 'Hitech City Road', 1, 1, 0
) v ON c.slug = v.city_slug;

INSERT INTO hotel_images (hotel_id, image_url, caption, display_order, is_primary)
SELECT h.id,
       'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=1200&q=80',
       'Exterior',
       0,
       1
FROM hotels h
WHERE h.slug <> 'grand-horizon-hyderabad';

INSERT INTO hotel_amenities (hotel_id, amenity_id)
SELECT h.id, a.id
FROM hotels h
JOIN amenities a ON a.code IN ('WIFI', 'PARKING', 'AC')
WHERE h.slug <> 'grand-horizon-hyderabad';

UPDATE rooms r
SET hotel_id = (SELECT id FROM hotels WHERE slug = 'grand-horizon-hyderabad' LIMIT 1)
WHERE hotel_id IS NULL;

ALTER TABLE rooms
    MODIFY COLUMN hotel_id BIGINT NOT NULL;

ALTER TABLE rooms
    ADD CONSTRAINT fk_rooms_hotel FOREIGN KEY (hotel_id) REFERENCES hotels (id)
        ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE rooms DROP INDEX uk_rooms_room_number;
ALTER TABLE rooms
    ADD CONSTRAINT uk_rooms_hotel_room_number UNIQUE (hotel_id, room_number);

CREATE INDEX idx_rooms_hotel_id ON rooms (hotel_id);
