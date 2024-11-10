# PW4 Group 1 - introduction

This project is a API for managing users, orders, and products. It is built using Java and Quarkus, and it interacts with both MongoDB and a MySQL.

## Getting Started

To get started with this project, you need to have Java and Maven installed on your machine. Follow the steps below to set up and run the project:

1. Clone the repository:
    ```sh
    git clone https://github.com/SamuD03/PW4-back-end.git
    cd PW4-back-end
    ```

2. Build the project:
    ```sh
    mvn clean install
    ```

3. Run the application:
    ```sh
    mvn quarkus:dev
    ```

### Endpoints

- **`http://localhost:8080/auth/register`**
  - **Method:** `POST`
  - **Description:** Registers a new user.

- **`http://localhost:8080/auth/login`**
  - **Method:** `POST`
  - **Description:** Logs in a user.

- **`http://localhost:8080/auth/logout`**
  - **Method:** `POST`
  - **Description:** Logs out the current user.

- **`http://localhost:8080/auth/confirm?token=your_verification_token`**
  - **Method:** `GET`
  - **Description:** Confirms a user's email using a verification token.

- **`http://localhost:8080/auth/verify-phone`**
  - **Method:** `POST`
  - **Description:** Verifies a user's phone number.

- **`http://localhost:8080/admin/product/{id}/update`**
  - **Method:** `PUT`
  - **Description:** Updates a product with the specified ID.
  - **Path Parameter:** `{id}` - The ID of the product to update.

- **`http://localhost:8080/admin/product/create`**
  - **Method:** `POST`
  - **Description:** Creates a new product.

- **`http://localhost:8080/admin/{id}/delete`**
  - **Method:** `DELETE`
  - **Description:** Deletes an admin with the specified ID.
  - **Path Parameter:** `{id}` - The ID of the admin to delete.

- **`http://localhost:8080/admin/ingredient/create`**
  - **Method:** `POST`
  - **Description:** Creates a new ingredient.

- **`http://localhost:8080/admin/ingredient/{id}/update`**
  - **Method:** `PUT`
  - **Description:** Updates an ingredient with the specified ID.
  - **Path Parameter:** `{id}` - The ID of the ingredient to update.

- **`http://localhost:8080/orders`**
  - **Method:** `GET`
  - **Description:** Retrieves all orders.

- **`http://localhost:8080/orders/{orderId}/status`**
  - **Method:** `PUT`
  - **Description:** Updates the status of an order with the specified ID.
  - **Path Parameter:** `{orderId}` - The ID of the order to update.

- **`http://localhost:8080/orders/user`**
  - **Method:** `GET`
  - **Description:** Retrieves orders for the currently logged-in user.

- **`http://localhost:8080/orders/admin`**
  - **Method:** `GET`
  - **Description:** Retrieves all orders for admin users.

- **`http://localhost:8080/user/notifications`**
  - **Method:** `PUT`
  - **Description:** Updates the notification preferences for the logged-in user.

- **`http://localhost:8080/product`**
  - **Method:** `GET`
  - **Description:** Retrieves all products.

- **`http://localhost:8080/user/profile`**
  - **Method:** `GET`
  - **Description:** Retrieves the profile of the currently logged-in user.

- **`http://localhost:8080/user/{id}`**
  - **Method:** `DELETE`
  - **Description:** Deletes a user with the specified ID.
  - **Path Parameter:** `{id}` - The ID of the user to delete.

- **`http://localhost:8080/user/{id}`**
  - **Method:** `PUT`
  - **Description:** Updates the admin and verification status of a user with the specified ID.
  - **Path Parameter:** `{id}` - The ID of the user to update.