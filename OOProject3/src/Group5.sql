CREATE DATABASE Group5;
USE Group5;

-- Users table for managing roles and authentication
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role ENUM('cashier', 'admin', 'manager') NOT NULL
);

-- Movies table for storing movie details
CREATE TABLE movies (
    movie_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    poster_path VARCHAR(255),
    summary_path VARCHAR(255),
    duration INT DEFAULT 120 -- Default duration is 2 hours
);

-- Movie genres table for categorizing movies
CREATE TABLE movie_genres (
    movie_id INT,
    genre VARCHAR(50),
    PRIMARY KEY (movie_id, genre),
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id)
);

-- Halls table for managing cinema halls
CREATE TABLE halls (
    hall_id INT PRIMARY KEY AUTO_INCREMENT,
    name ENUM('HALL_A', 'HALL_B') NOT NULL,
    capacity INT NOT NULL
);

-- Sessions table for managing movie sessions
CREATE TABLE sessions (
    session_id INT PRIMARY KEY AUTO_INCREMENT,
    start_time TIME NOT NULL,
    duration INT DEFAULT 120 -- Duration in minutes
);

-- Schedule table for assigning movies to halls and sessions
CREATE TABLE schedule (
    schedule_id INT PRIMARY KEY AUTO_INCREMENT,
    movie_id INT,
    hall_id INT,
    session_id INT,
    schedule_date DATE NOT NULL,
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id),
    FOREIGN KEY (hall_id) REFERENCES halls(hall_id),
    FOREIGN KEY (session_id) REFERENCES sessions(session_id),
    UNIQUE (hall_id, session_id, schedule_date)
);

-- Products table for managing inventory
CREATE TABLE products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL, -- Changed to VARCHAR for flexibility
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL,
    image_path VARCHAR(255)
);

-- Customers table for storing customer details
CREATE TABLE customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    birth_date DATE
);

-- Sales table for tracking purchases
CREATE TABLE sales (
    sale_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT,
    cashier_id INT,
    sale_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) NOT NULL,
    invoice_path VARCHAR(255),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (cashier_id) REFERENCES users(user_id)
);

-- Tickets table for managing ticket details
CREATE TABLE tickets (
    ticket_id INT PRIMARY KEY AUTO_INCREMENT,
    sale_id INT,
    schedule_id INT,
    seat_number VARCHAR(10) NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    discount_applied DECIMAL(5,2) DEFAULT 0.00,
    is_cancelled BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sale_id) REFERENCES sales(sale_id),
    FOREIGN KEY (schedule_id) REFERENCES schedule(schedule_id)
);

-- Sale items table for managing product purchases
CREATE TABLE sale_items (
    sale_id INT,
    product_id INT,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (sale_id) REFERENCES sales(sale_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    PRIMARY KEY (sale_id, product_id)
);

-- Config table for storing adjustable parameters
CREATE TABLE config ( 
    config_key VARCHAR(50) PRIMARY KEY,
    config_value VARCHAR(255) NOT NULL
);

-- Insert initial users
INSERT INTO users (username, password, first_name, last_name, role) VALUES
('cashier1', 'cashier1', 'Cashier', 'One', 'cashier'),
('admin1', 'admin1', 'Admin', 'One', 'admin'),
('manager1', 'manager1', 'Manager', 'One', 'manager');

-- Insert initial halls
INSERT INTO halls (name, capacity) VALUES
('HALL_A', 16),
('HALL_B', 48);

-- Insert initial sessions
INSERT INTO sessions (start_time) VALUES
('10:00:00'),
('12:00:00'),
('14:00:00'),
('16:00:00'),
('18:00:00'),
('20:00:00');

-- Insert initial config values for discounts and taxes
INSERT INTO config (config_key, config_value) VALUES
('ticket_base_price', '100.00'),
('above_60_discount_rate', '50.00'),  -- 50% discount for above 60
('below_18_discount_rate', '50.00'), -- 50% discount for below 18
('ticket_tax_rate', '20.00'),        -- 20% tax on tickets
('product_tax_rate', '10.00');       -- 10% tax on products

-- Create a view for available seats
CREATE VIEW available_seats AS
SELECT
    s.schedule_id,
    h.name AS hall_name,
    h.capacity - COUNT(t.ticket_id) AS seats_available
FROM schedule s
JOIN halls h ON s.hall_id = h.hall_id
LEFT JOIN tickets t ON s.schedule_id = t.schedule_id AND t.is_cancelled = FALSE
GROUP BY s.schedule_id, h.name;

-- Insert sample products
INSERT INTO products (name, type, price, stock_quantity) VALUES
    ('Cola', 'beverage', 1.99, 100),
    ('Chips', 'snack', 2.49, 50),
    ('Toy Car', 'toy', 9.99, 30);

-- Insert sample movies
INSERT INTO movies (title, poster_path, summary_path, duration) VALUES
    ('Movie A', 'poster_a.jpg', 'summary_a.txt', 120),
    ('Movie B', 'poster_b.jpg', 'summary_b.txt', 120);
