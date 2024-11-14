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
    notification  BOOLEAN      NOT NULL DEFAULT false
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
    category    VARCHAR(255)   NOT NULL,
    url         VARCHAR(255)   NOT NULL DEFAULT 'nan'
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

-- Create the verification_token table
CREATE TABLE IF NOT EXISTS verification_token
(
    email       VARCHAR(255) NOT NULL,
    token       VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP    NOT NULL,
    FOREIGN KEY (email) REFERENCES user (email)
);
