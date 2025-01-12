CREATE DATABASE Group5;
USE Group5;

CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role ENUM('cashier', 'admin', 'manager') NOT NULL
);

CREATE TABLE movies (
    movie_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    poster_path VARCHAR(255),
    summary_path VARCHAR(255),
    duration INT DEFAULT 120
);

CREATE TABLE movie_genres (
    movie_id INT,
    genre VARCHAR(50),
    PRIMARY KEY (movie_id, genre),
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id)
);

CREATE TABLE halls (
    hall_id INT PRIMARY KEY AUTO_INCREMENT,
    name ENUM('HALL_A', 'HALL_B') NOT NULL,
    capacity INT NOT NULL
);

CREATE TABLE sessions (
    session_id INT PRIMARY KEY AUTO_INCREMENT,
    start_time TIME NOT NULL
);

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

CREATE TABLE products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type ENUM('beverage', 'biscuit', 'toy') NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL,
    image_path VARCHAR(255)
);

CREATE TABLE customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    birth_date DATE
);

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

CREATE TABLE sale_items (
    sale_id INT,
    product_id INT,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (sale_id) REFERENCES sales(sale_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    PRIMARY KEY (sale_id, product_id)
);

CREATE TABLE config ( 
    config_key VARCHAR(50) PRIMARY KEY,
    config_value VARCHAR(255) NOT NULL
);

INSERT INTO users (username, password, first_name, last_name, role) VALUES
('cashier1', 'cashier1', 'Cashier', 'One', 'cashier'),
('admin1', 'admin1', 'Admin', 'One', 'admin'),
('manager1', 'manager1', 'Manager', 'One', 'manager');

INSERT INTO halls (name, capacity) VALUES
('HALL_A', 16),
('HALL_B', 48);

INSERT INTO sessions (start_time) VALUES
('10:00:00'),
('12:00:00'),
('14:00:00'),
('16:00:00'),
('18:00:00'),
('20:00:00');

INSERT INTO config (config_key, config_value) VALUES
('ticket_base_price', '100.00'),
('age_discount_rate', '50.00'),
('ticket_tax_rate', '20.00'),
('product_tax_rate', '10.00');

CREATE VIEW available_seats AS
SELECT
    s.schedule_id,
    h.capacity - COUNT(t.ticket_id) as seats_available
FROM schedule s
JOIN halls h ON s.hall_id = h.hall_id
LEFT JOIN tickets t ON s.schedule_id = t.schedule_id AND t.is_cancelled = FALSE
GROUP BY s.schedule_id;
