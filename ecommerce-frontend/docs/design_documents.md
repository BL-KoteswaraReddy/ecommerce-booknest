# BookNest E-Commerce Platform - Design Documents

## 1. Actors & Functionalities

### Actors
* **Customer:** A registered or guest user who browses books, manages their cart, maintains a wishlist, and places orders.
* **Admin:** A privileged user responsible for managing the book catalog, viewing all orders, and managing user accounts.
* **System (Automated Tasks):** Background processes for inventory management, wallet deductions, and order status updates.

### Core Functionalities
* **Authentication & Authorization:** User registration, login, role-based access control (Admin vs Customer).
* **Catalog Management:** Browse books, search, filter, and view details (Admin can Add/Edit/Delete books).
* **Cart Management:** Add/Remove items, update quantities, calculate subtotals.
* **Wishlist Management:** Save books for later, move to cart.
* **Wallet System:** Check balance, top-up wallet, view transaction history, process payments.
* **Order Management:** Place orders (Cash on Delivery or Wallet), view order history, cancel orders (Admin can update order status).

---

## 2. Architectural Diagram

```mermaid
graph TD
    Client["Angular Frontend (Web App)"]

    subgraph "Microservices Ecosystem"
        Gateway["API Gateway / Routing"]
        
        AuthService["Auth/User Service"]
        CatalogService["Book Catalog Service"]
        CartService["Cart Service"]
        WishlistService["Wishlist Service"]
        OrderService["Order Service"]
        WalletService["Wallet/Payment Service"]
    end

    subgraph "Databases"
        DB_Auth[(User DB)]
        DB_Catalog[(Catalog DB)]
        DB_Cart[(Cart DB)]
        DB_Wishlist[(Wishlist DB)]
        DB_Order[(Order DB)]
        DB_Wallet[(Wallet DB)]
    end

    Client --> Gateway
    Gateway --> AuthService
    Gateway --> CatalogService
    Gateway --> CartService
    Gateway --> WishlistService
    Gateway --> OrderService
    Gateway --> WalletService

    AuthService --> DB_Auth
    CatalogService --> DB_Catalog
    CartService --> DB_Cart
    WishlistService --> DB_Wishlist
    OrderService --> DB_Order
    WalletService --> DB_Wallet
```

---

## 3. Database Entity-Relationship (ER) Diagram

```mermaid
erDiagram
    USER ||--o{ ORDER : places
    USER ||--|| CART : owns
    USER ||--|| WISHLIST : owns
    USER ||--|| WALLET : owns

    CART ||--o{ CART_ITEM : contains
    WISHLIST ||--o{ WISHLIST_ITEM : contains
    ORDER ||--o{ ORDER_ITEM : contains
    
    BOOK ||--o{ CART_ITEM : added_as
    BOOK ||--o{ WISHLIST_ITEM : added_as
    BOOK ||--o{ ORDER_ITEM : ordered_as
    
    WALLET ||--o{ TRANSACTION : has

    USER {
        int id
        string name
        string email
        string password
        string role
    }

    BOOK {
        int id
        string title
        string author
        float price
        int stock
        string category
    }

    ORDER {
        int id
        int user_id
        float total_amount
        string status
        string payment_mode
        datetime created_at
    }

    WALLET {
        int id
        int user_id
        float balance
    }

    TRANSACTION {
        int id
        int wallet_id
        float amount
        string type
        string status
    }
```

---

## 4. Sequence Diagram: Checkout Flow (Wallet Payment)

```mermaid
sequenceDiagram
    actor Customer
    participant Frontend as Angular UI
    participant OrderSvc as Order Service
    participant CartSvc as Cart Service
    participant WalletSvc as Wallet Service

    Customer->>Frontend: Click "Place Order" (Wallet)
    Frontend->>CartSvc: Get Cart Details
    CartSvc-->>Frontend: Returns Cart (Total: $X)
    
    Frontend->>WalletSvc: Check Wallet Balance
    WalletSvc-->>Frontend: Returns Balance
    
    alt Balance < Total
        Frontend-->>Customer: Display "Insufficient Balance" Error
    else Balance >= Total
        Frontend->>OrderSvc: Create Order (Status: PENDING)
        OrderSvc-->>Frontend: Order Created (OrderID)
        
        Frontend->>WalletSvc: Process Payment (Amount, OrderID)
        WalletSvc->>WalletSvc: Deduct Balance & Create Transaction
        WalletSvc-->>Frontend: Payment Success (New Balance)
        
        Frontend->>CartSvc: Clear Cart
        CartSvc-->>Frontend: Cart Cleared
        
        Frontend-->>Customer: Display Order Success Page
    end
```

---

## 5. High-Level Class Diagram

```mermaid
classDiagram
    class User {
        +Long id
        +String email
        +String fullName
        +String role
        +login()
        +register()
    }

    class Book {
        +Long id
        +String title
        +String author
        +Double price
        +Integer stock
        +Boolean available
        +updateStock(quantity)
    }

    class Cart {
        +Long id
        +Long userId
        +Double totalPrice
        +List~CartItem~ items
        +addItem(book, qty)
        +removeItem(itemId)
        +clear()
    }

    class Order {
        +Long id
        +Long userId
        +Double totalAmount
        +OrderStatus status
        +PaymentMode paymentMode
        +List~OrderItem~ items
        +Address shippingAddress
        +cancel()
        +updateStatus()
    }

    class Wallet {
        +Long id
        +Long userId
        +Double currentBalance
        +List~Transaction~ transactions
        +addMoney(amount)
        +deductMoney(amount)
    }

    User "1" *-- "1" Cart
    User "1" *-- "1" Wallet
    User "1" *-- "*" Order
    Order "1" *-- "*" OrderItem
    Cart "1" *-- "*" CartItem
    Book "1" o-- "*" CartItem
    Book "1" o-- "*" OrderItem
    Wallet "1" *-- "*" Transaction
```