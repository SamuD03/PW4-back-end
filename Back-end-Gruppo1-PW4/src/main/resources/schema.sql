CREATE DATABASE IF NOT EXISTS LaVie;
USE LaVie;

CREATE TABLE IF NOT EXISTS user
(
    email         VARCHAR(255) PRIMARY KEY NOT NULL UNIQUE,
    name          VARCHAR(255)             NOT NULL,
    pswHash       VARCHAR(255)             NOT NULL,
    surname       VARCHAR(255)             NOT NULL,
    admin         BOOLEAN                  NOT NULL,
    emailVerified BOOLEAN                  NOT NULL
);

CREATE TABLE IF NOT EXISTS session
(
    id    CHAR(36)     NOT NULL UNIQUE PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    FOREIGN KEY (email) REFERENCES user (email)
);

-- Create the ingredient table
CREATE TABLE IF NOT EXISTS ingredient (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- Create the product table
CREATE TABLE IF NOT EXISTS product (
    id INT AUTO_INCREMENT PRIMARY KEY,
    productName VARCHAR(255) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(255) NOT NULL
);

-- Create the junction table for the many-to-many relationship
CREATE TABLE IF NOT EXISTS product_ingredient (
    product_id INT,
    ingredient_id INT,
    PRIMARY KEY (product_id, ingredient_id),
    FOREIGN KEY (product_id) REFERENCES product(id),
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(id)
);


CREATE TABLE IF NOT EXISTS stock
(
    id         INT PRIMARY KEY NOT NULL AUTO_INCREMENT,
    product_id INT             NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product (id)
);
CREATE TABLE IF NOT EXISTS verification_token
(
    email       VARCHAR(255) NOT NULL,
    token       VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP    NOT NULL,
    FOREIGN KEY (email) REFERENCES user (email)
);


-- dati mock
INSERT INTO ingredient (name) VALUES
                                  ('Farina'),
                                  ('Zucchero'),
                                  ('Burro'),
                                  ('Uova'),
                                  ('Cioccolato fondente'),
                                  ('Latte'),
                                  ('Panna montata'),
                                  ('Vaniglia'),
                                  ('Fragole'),
                                  ('Lievito per dolci');

INSERT INTO product (productName, description, quantity, price, category) VALUES
                                                                              ('Torta al Cioccolato', 'Deliziosa torta al cioccolato ricoperta di ganache', 15, 15.00, 'Torte'),
                                                                              ('Cheesecake alle Fragole', 'Cheesecake cremosa con fragole fresche', 10, 20.00, 'Torte'),
                                                                              ('Torta Margherita', 'Soffice torta Margherita aromatizzata alla vaniglia', 20, 12.00, 'Torte'),
                                                                              ('Torta alla Vaniglia', 'Torta soffice con crema alla vaniglia e panna montata', 12, 18.00, 'Torte'),
                                                                              ('Torta di Fragole', 'Torta leggera con fragole fresche e panna', 8, 16.00, 'Torte');

-- Torta al Cioccolato
INSERT INTO product_ingredient (product_id, ingredient_id) VALUES
                                                               (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 10);

-- Cheesecake alle Fragole
INSERT INTO product_ingredient (product_id, ingredient_id) VALUES
                                                               (2, 2), (2, 3), (2, 9), (2, 7);

-- Torta Margherita
INSERT INTO product_ingredient (product_id, ingredient_id) VALUES
                                                               (3, 1), (3, 2), (3, 3), (3, 4), (3, 8), (3, 10);

-- Torta alla Vaniglia
INSERT INTO product_ingredient (product_id, ingredient_id) VALUES
                                                               (4, 1), (4, 2), (4, 3), (4, 4), (4, 8), (4, 7);

-- Torta di Fragole
INSERT INTO product_ingredient (product_id, ingredient_id) VALUES
                                                               (5, 1), (5, 2), (5, 3), (5, 4), (5, 9), (5, 10);

