SELECT i.name

FROM ingredient i

         JOIN product_ingredient pi ON i.id = pi.ingredient_id

         JOIN product p ON pi.product_id = p.id

WHERE p.productName = 'Torta Margherita';

