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

CREATE TABLE IF NOT EXISTS product
(
    id          INT PRIMARY KEY NOT NULL UNIQUE,
    productName VARCHAR(255)    NOT NULL,
    ingredients VARCHAR(255)    NOT NULL,
    `desc`      VARCHAR(255)    NOT NULL,
    quantity    INT             NOT NULL,
    price       DOUBLE          NOT NULL
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
