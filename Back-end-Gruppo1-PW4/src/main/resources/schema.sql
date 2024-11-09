CREATE DATABASE IF NOT EXISTS LaVie;
USE LaVie;

-- Create the user table
CREATE TABLE IF NOT EXISTS user
(
    id       INT PRIMARY KEY AUTO_INCREMENT,
    email    VARCHAR(255) UNIQUE,
    name     VARCHAR(255) NOT NULL,
    pswHash  VARCHAR(255) NOT NULL,
    surname  VARCHAR(255) NOT NULL,
    number   VARCHAR(14) UNIQUE,
    admin    BOOLEAN      NOT NULL,
    verified BOOLEAN      NOT NULL,
    notification  BOOLEAN      NOT NULL
);

-- Create the session table
CREATE TABLE IF NOT EXISTS session
(
    id      VARCHAR(36) PRIMARY KEY,
    user_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user (id)
);

-- Create the ingredient table
CREATE TABLE IF NOT EXISTS ingredient
(
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Create the product table
CREATE TABLE IF NOT EXISTS product
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    productName VARCHAR(255)   NOT NULL UNIQUE,
    description TEXT           NOT NULL,
    quantity    INT            NOT NULL,
    price       DOUBLE NOT NULL,
    category    VARCHAR(255)   NOT NULL
);

-- Create the product_ingredient junction table
CREATE TABLE IF NOT EXISTS product_ingredient
(
    product_id    INT,
    ingredient_id INT,
    PRIMARY KEY (product_id, ingredient_id),
    FOREIGN KEY (product_id) REFERENCES product (id),
    FOREIGN KEY (ingredient_id) REFERENCES ingredient (id)
);

-- Create the stock table
CREATE TABLE IF NOT EXISTS stock
(
    id         INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product (id)
);

-- Create the verification_token table
CREATE TABLE IF NOT EXISTS verification_token
(
    email       VARCHAR(255) NOT NULL,
    token       VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP    NOT NULL,
    FOREIGN KEY (email) REFERENCES user (email)
);

-- Insert mock data into the ingredient table
INSERT INTO ingredient (name)
VALUES ('Farina'),
       ('Zucchero'),
       ('Burro'),
       ('Uova'),
       ('Cioccolato fondente'),
       ('Latte'),
       ('Panna montata'),
       ('Vaniglia'),
       ('Fragole'),
       ('Lievito per dolci');

-- Insert mock data into the product table
INSERT INTO product (productName, description, quantity, price, category)
VALUES ('Torta al Cioccolato', 'Deliziosa torta al cioccolato ricoperta di ganache', 15, 15.00, 'Torte'),
       ('Cheesecake alle Fragole', 'Cheesecake cremosa con fragole fresche', 10, 20.00, 'Torte'),
       ('Torta Margherita', 'Soffice torta Margherita aromatizzata alla vaniglia', 20, 12.00, 'Torte'),
       ('Torta alla Vaniglia', 'Torta soffice con crema alla vaniglia e panna montata', 12, 18.00, 'Torte'),
       ('Torta di Fragole', 'Torta leggera con fragole fresche e panna', 8, 16.00, 'Torte');

-- Insert data into the product_ingredient junction table
-- Torta al Cioccolato
INSERT INTO product_ingredient (product_id, ingredient_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (1, 4),
       (1, 5),
       (1, 10);

-- Cheesecake alle Fragole
INSERT INTO product_ingredient (product_id, ingredient_id)
VALUES (2, 2),
       (2, 3),
       (2, 9),
       (2, 7);

-- Torta Margherita
INSERT INTO product_ingredient (product_id, ingredient_id)
VALUES (3, 1),
       (3, 2),
       (3, 3),
       (3, 4),
       (3, 8),
       (3, 10);

-- Torta alla Vaniglia
INSERT INTO product_ingredient (product_id, ingredient_id)
VALUES (4, 1),
       (4, 2),
       (4, 3),
       (4, 4),
       (4, 8),
       (4, 7);

-- Torta di Fragole
INSERT INTO product_ingredient (product_id, ingredient_id)
VALUES (5, 1),
       (5, 2),
       (5, 3),
       (5, 4),
       (5, 9),
       (5, 10);
