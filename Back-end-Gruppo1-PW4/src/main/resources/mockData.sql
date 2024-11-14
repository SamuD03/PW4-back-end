use lavie;
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


INSERT INTO product (productName, description, quantity, price, category)
VALUES ('Saint Honorè', 'Realizzata secondo il metodo francese: base di sfoglia caramellata farcita a piacere con crema alla vaniglia, al pistacchio o al cioccolato, choux caramellati, frutta fresca e una elegante decorazione di panna montata.', 5, 25.00, 'Torte'),
       ('Venere Nera', 'Mousse al cioccolato fondente con fogli croccanti di cioccolato e gelatina al frutto di bosco.', 8, 22.00, 'Torte'),
       ('Afrodite', 'Mousse panna cotta alla vaniglia bourbon, cuore di cremoso morbido al cioccolato e lamponi frozen su una base di crumble al cacao salato.', 10, 30.00, 'Torte'),
       ('Due note', 'Soffice mousse al cioccolato bianco con cremoso al pistacchio e gelatina all’arancia rossa.', 7, 27.00, 'Torte'),
       ('Millefoglie', 'Friabile e croccante sfoglia caramellata, farcita con crema chantilly alla vaniglia e a piacere frutta fresca o cioccolato fondente.', 15, 18.00, 'Torte'),
       ('Delicata', 'Pan di spagna bagnato con succo d’arancia o di fragole, farcito con crema chantilly, fragole marinate in zucchero e limone oppure cioccolato, ricoperto e decorato con panna montata non zuccherata e frutta fresca.', 10, 20.00, 'Torte'),
       ('Mirtilla (Ma contenta)', 'Morbidone alla mandorla con mirtilli freschi, mousse al cioccolato bianco profumata alla vaniglia e un doppio inserto di cremoso ai mirtilli e mirtilli in purezza.', 6, 32.00, 'Torte'),
       ('Fashion', 'Mousse alla mandorla, cuore di fragole fresche macerate con zucchero e limone, base di crumble alla mandorla amara.', 8, 26.00, 'Torte'),
       ('Foresta nera', 'Pan di spagna al cioccolato fondente e mandorle farcito con panna montata alla vaniglia bourbon e amarene.', 12, 24.00, 'Torte'),
       ('Cielo Stellato', 'Vellutata mousseline al Grand Marnier, alternata a strati di biscuit al cacao e fogli croccanti di cioccolato.', 10, 28.00, 'Torte'),
       ('Creme tarte', 'La nostra creme tarte, pasta sfoglia caramellata e ciuffi di crema chantilly per deliziarvi con una torta golosa e personalizzabile.', 5, 30.00, 'Torte');


INSERT INTO product_ingredient (product_id, ingredient_id)
VALUES (6, 3), -- BURRO
       (6, 1), -- FARINA
       (6, 4), -- UOVA
       (6, 6), -- LATTE
       (6, 7), -- PANNA
       (6, 2), -- ZUCCHERO
       (6, 8), -- VANIGLIA
       (6, 5), -- CIOCCOLATO FONDENTE
       (6, 9); -- FRUTTA FRESCA


INSERT INTO product_ingredient (product_id, ingredient_id)
VALUES (7, 7), -- PANNA
       (7, 4), -- UOVA
       (7, 5), -- CIOCCOLATO FONDENTE
       (7, 6), -- LATTE
       (7, 2), -- ZUCCHERO
       (7, 10); -- GELATINA NEUTRA


INSERT INTO product_ingredient (product_id, ingredient_id)
VALUES (8, 7), -- PANNA
       (8, 6), -- LATTE
       (8, 2), -- ZUCCHERO
       (8, 5), -- CIOCCOLATO FONDENTE
       (8, 3), -- BURRO
       (8, 8); -- VANIGLIA


INSERT INTO user (email, name, pswHash, surname, number, admin, verified, notification)
VALUES
    ('giacomoaceti@example.com', 'Giacomo', 'Th2k+LcqDk9gubKgGdEWtPA8lz9x2iMvrYB32Z4up8eHf77asrF4xPyKoQcztUmKFKiJNlMu5F6+JJ8NijTLcQ', 'Aceti', '+391111111111', true, true, true),
    ('mariorossi@example.com', 'Mario', '5aI76V3D/57XgUAm+ujOeSAZ7Ps+4TGrj4xwWW4eXd67Zns6o6SMQvbMX8BbKoEeBiRgmo1STaM4RLl7VIl6qg', 'Rossi', '+391111111112', false, true, true);