-- authentication
SELECT s.id FROM session s JOIN user u ON s.user_id = u.id WHERE u.email = ? OR u.number = ?
-- email service
-- 1
SELECT name FROM user WHERE email = ?
-- 2
SELECT email FROM verification_token WHERE token = ? AND expiry_date > CURRENT_TIMESTAMP
-- 3
SELECT verified FROM user WHERE id = ?
-- order
SELECT id, productName, description, price, category, quantity FROM product WHERE id = ?
-- product
-- 1
SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.ingredients
-- 2
SELECT 1 FROM Product p JOIN p.ingredients i WHERE i.id = ?
-- session
--1
SELECT * FROM session WHERE id = ?
--2
SELECT user_id FROM session WHERE id = ?
--3
SELECT COUNT(*) FROM session WHERE user_id = (SELECT id FROM user WHERE email = ? OR number = ?)
-- user
-- 1
SELECT id, pswHash FROM user WHERE id = ? AND pswHash = ?
-- 2
SELECT id, name, surname, email, pswHash, number, admin, verified FROM user WHERE email = ? OR number = ?
-- 3
SELECT id, name, surname, email, number, admin, verified, notification FROM user WHERE admin = ?
-- 4
SELECT admin FROM user WHERE id = ?
-- 5
SELECT pswHash FROM user WHERE id = ?
-- 6
SELECT email FROM user WHERE id = ?
-- 7
SELECT id, name, surname, email, pswHash, number, admin, verified, notification FROM user WHERE id = ?
-- 8
SELECT id, name, surname, email FROM user WHERE admin = true
-- 9
SELECT verified FROM user WHERE number = ?







-- find ingredient for a product

SELECT i.name

FROM ingredient i

         JOIN product_ingredient pi ON i.id = pi.ingredient_id

         JOIN product p ON pi.product_id = p.id

WHERE p.productName = 'Torta Margherita';

