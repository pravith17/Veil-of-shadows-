package Hackathon3;

import java.util.ArrayList;
import java.util.Scanner;

class User {
    private String name;
    private String email;
    private String phoneNumber;
    private String password;
    private String address;
    int i

    public User(String name, String email, String phoneNumber, String password) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDetails() {
        return "User: " + name + "\nEmail: " + email + "\nPhone: " + phoneNumber + 
               (address != null ? "\nAddress: " + address : "");
    }
}

class Product {
    String name;
    double price;
    String category;
    int i;

    public Product(String name, double price, String category) {
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public String toString(String currency, double exchangeRate) {
        String symbol = currency.equals("USD") ? "$" : currency.equals("EUR") ? "€" : "₹";
        return name + " | Price: " + symbol + String.format("%.2f", price * exchangeRate);
    }
}

class CartItem {
    Product product;
    int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public double getSubtotal() {
        return product.price * quantity;
    }
}

class Cart {
    private ArrayList<CartItem> cartItems = new ArrayList<>();
    private static final double DELIVERY_CHARGES = 78.0;

    public void addItem(Product product, int quantity) {
        for (CartItem item : cartItems) {
            if (item.product.name.equalsIgnoreCase(product.name)) {
                item.quantity += quantity;
                System.out.println(quantity + " more " + product.name + " added to the cart!");
                return;
            }
        }
        cartItems.add(new CartItem(product, quantity));
        System.out.println(quantity + " " + product.name + " added to the cart!");
    }

    public void removeItem(String productName) {
        for (CartItem item : cartItems) {
            if (item.product.name.equalsIgnoreCase(productName)) {
                cartItems.remove(item);
                System.out.println(item.product.name + " removed from the cart!");
                return;
            }
        }
        System.out.println("Item not found in cart.");
    }

    public void updateQuantity(String productName, int newQuantity) {
        for (CartItem item : cartItems) {
            if (item.product.name.equalsIgnoreCase(productName)) {
                if (newQuantity <= 0) {
                    removeItem(productName);
                } else {
                    item.quantity = newQuantity;
                    System.out.println("Quantity updated for " + productName + " to " + newQuantity);
                }
                return;
            }
        }
        System.out.println("Item not found in cart.");
    }

    public void displayCart(String currency, double exchangeRate) {
        if (cartItems.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        double subtotal = 0;
        String symbol = currency.equals("USD") ? "$" : currency.equals("EUR") ? "€" : "₹";
        System.out.println("\n=== Your Cart ===");
        for (CartItem item : cartItems) {
            double itemTotal = item.getSubtotal();
            System.out.printf("%s | Qty: %d | Subtotal: %s%.2f%n", 
                item.product.toString(currency, exchangeRate), item.quantity, symbol, itemTotal * exchangeRate);
            subtotal += itemTotal;
        }
        System.out.printf("Subtotal: %s%.2f%n", symbol, subtotal * exchangeRate);
        System.out.printf("Delivery Charges: %s%.2f%n", symbol, 
            (subtotal > 1000 ? 0.00 : DELIVERY_CHARGES) * exchangeRate);
        System.out.printf("Total Amount: %s%.2f%n", symbol, 
            (subtotal + (subtotal > 1000 ? 0.00 : DELIVERY_CHARGES)) * exchangeRate);
    }

    public double getSubtotal() {
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getSubtotal();
        }
        return subtotal;
    }

    public void manageCart(Scanner scanner) {
        while (true) {
            displayCart("INR", 1.0);
            if (cartItems.isEmpty()) break;
            System.out.println("\n1) Update Quantity\n2) Remove Item\n3) Back to Shopping");
            System.out.print("Choose an option (1-3): ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            if (choice == 3) break;
            System.out.print("Enter product name: ");
            String productName = scanner.nextLine();
            if (choice == 1) {
                System.out.print("Enter new quantity: ");
                int newQty = scanner.nextInt();
                scanner.nextLine();
                if (newQty >= 0) {
                    updateQuantity(productName, newQty);
                } else {
                    System.out.println("Invalid quantity!");
                }
            } else if (choice == 2) {
                removeItem(productName);
            } else {
                System.out.println("Invalid option!");
            }
        }
    }

    public void checkout(Discount discount, String couponCode, User user, Scanner scanner, String currency, double exchangeRate) {
        if (cartItems.isEmpty()) {
            System.out.println("Cart is empty. Add items before checkout!");
            return;
        }

        double subtotal = getSubtotal();
        double deliveryCharges = DELIVERY_CHARGES;
        double discountAmount = discount.applyDiscount(subtotal);
        double finalTotal = subtotal;
        String symbol = currency.equals("USD") ? "$" : currency.equals("EUR") ? "€" : "₹";

        if (subtotal > 1000) {
            deliveryCharges = 0;
            System.out.println("Free delivery applied for order above ₹1000!");
        }

        if (couponCode != null) {
            switch (couponCode.toUpperCase()) {
                case "FIRST10":
                    deliveryCharges = 0;
                    System.out.println("Coupon FIRST10 applied: Free Delivery!");
                    break;
                case "10ETC":
                    discountAmount = subtotal * 0.10;
                    System.out.println("Coupon 10ETC applied: 10% off!");
                    break;
                case "WELCOME20":
                    discountAmount = subtotal * 0.20;
                    System.out.println("Coupon WELCOME20 applied: 20% off!");
                    break;
                case "FLAT50":
                    discountAmount = 50.0;
                    System.out.println("Coupon FLAT50 applied: Flat ₹50 off!");
                    break;
                default:
                    System.out.println("Invalid coupon code!");
            }
        }

        finalTotal = subtotal - discountAmount + deliveryCharges;

        boolean modifyingCart = true;
        while (modifyingCart) {
            displayCart(currency, exchangeRate);
            System.out.printf("After Discount: %s%.2f%n", symbol, (subtotal - discountAmount) * exchangeRate);
            System.out.printf("Final Total (with delivery): %s%.2f%n", symbol, finalTotal * exchangeRate);

            System.out.println("\nOptions before confirming order:");
            System.out.println("1) Edit Quantity\n2) Remove Item\n3) Proceed to Checkout");
            System.out.print("Choose an option (1-3): ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter product name to edit quantity: ");
                    String editProduct = scanner.nextLine();
                    System.out.print("Enter new quantity: ");
                    int newQty = scanner.nextInt();
                    scanner.nextLine();
                    if (newQty >= 0) {
                        updateQuantity(editProduct, newQty);
                        subtotal = getSubtotal();
                        discountAmount = discount.applyDiscount(subtotal);
                        deliveryCharges = subtotal > 1000 ? 0 : DELIVERY_CHARGES;
                        finalTotal = subtotal - discountAmount + deliveryCharges;
                    } else {
                        System.out.println("Invalid quantity!");
                    }
                    break;
                case 2:
                    System.out.print("Enter product name to remove: ");
                    String removeProduct = scanner.nextLine();
                    removeItem(removeProduct);
                    subtotal = getSubtotal();
                    discountAmount = discount.applyDiscount(subtotal);
                    deliveryCharges = subtotal > 1000 ? 0 : DELIVERY_CHARGES;
                    finalTotal = subtotal - discountAmount + deliveryCharges;
                    break;
                case 3:
                    modifyingCart = false;
                    break;
                default:
                    System.out.println("Invalid option!");
            }
            if (cartItems.isEmpty()) {
                System.out.println("Cart is empty. Checkout cancelled.");
                return;
            }
        }

        System.out.print("\nConfirm order? (yes/no): ");
        if (!scanner.nextLine().trim().toLowerCase().equals("yes")) {
            System.out.println("Order cancelled.");
            return;
        }

        System.out.print("Enter your delivery address: ");
        String address = scanner.nextLine();
        user.setAddress(address);

        System.out.println("\nSelect Payment Method:");
        System.out.println("1) Cash on Delivery (COD)");
        System.out.println("2) UPI");
        System.out.println("3) Credit/Debit Card");
        int paymentChoice;
        do {
            System.out.print("Enter your choice (1-3): ");
            paymentChoice = scanner.nextInt();
            scanner.nextLine();
            if (paymentChoice < 1 || paymentChoice > 3) {
                System.out.println("Invalid choice! Please select 1, 2, or 3.");
            }
        } while (paymentChoice < 1 || paymentChoice > 3);

        String paymentMethod = "";
        boolean paymentConfirmed = false;

        switch (paymentChoice) {
            case 1:
                paymentMethod = "Cash on Delivery";
                paymentConfirmed = true;
                break;

            case 2:
                paymentMethod = "UPI";
                String upiId;
                do {
                    System.out.print("Enter your UPI ID (e.g., name@bank): ");
                    upiId = scanner.nextLine();
                    if (!upiId.matches("^[a-zA-Z0-9]+@[a-zA-Z]+$")) {
                        System.out.println("Invalid UPI ID format! Use name@bank format.");
                    }
                } while (!upiId.matches("^[a-zA-Z0-9]+@[a-zA-Z]+$"));
                paymentConfirmed = confirmPayment(scanner, finalTotal, "UPI to " + upiId, currency, exchangeRate);
                break;

            case 3:
                paymentMethod = "Credit/Debit Card";
                String cardNumber, cvv, expiry;
                do {
                    System.out.print("Enter 16-digit card number: ");
                    cardNumber = scanner.nextLine();
                    if (!cardNumber.matches("\\d{16}")) {
                        System.out.println("Invalid card number! Must be 16 digits.");
                    }
                } while (!cardNumber.matches("\\d{16}"));
                System.out.print("Enter cardholder name: ");
                String cardHolderName = scanner.nextLine();
                do {
                    System.out.print("Enter CVV (3 digits): ");
                    cvv = scanner.nextLine();
                    if (!cvv.matches("\\d{3}")) {
                        System.out.println("Invalid CVV! Must be 3 digits.");
                    }
                } while (!cvv.matches("\\d{3}"));
                do {
                    System.out.print("Enter expiry date (MM/YY): ");
                    expiry = scanner.nextLine();
                    if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                        System.out.println("Invalid expiry date! Use MM/YY format (e.g., 12/25).");
                    }
                } while (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}"));
                paymentConfirmed = confirmPayment(scanner, finalTotal, 
                    "card ending in " + cardNumber.substring(cardNumber.length() - 4), currency, exchangeRate);
                break;
        }

        if (paymentConfirmed) {
            generateInvoice(user, discountAmount, deliveryCharges, finalTotal, paymentMethod, currency, exchangeRate);
            cartItems.clear();
        } else {
            System.out.println("Payment not confirmed. Order cancelled.");
        }
    }

    private boolean confirmPayment(Scanner scanner, double amount, String method, String currency, double exchangeRate) {
        String symbol = currency.equals("USD") ? "$" : currency.equals("EUR") ? "€" : "₹";
        for (int attempt = 1; attempt <= 3; attempt++) {
            System.out.printf("Please complete payment of %s%.2f using %s%n", symbol, amount * exchangeRate, method);
            System.out.print("Confirm payment completed? (yes/no): ");
            String response = scanner.nextLine().trim().toLowerCase();
            if (response.equals("yes")) return true;
            System.out.println("Payment attempt " + attempt + " failed. " + (3 - attempt) + " attempts remaining.");
        }
        return false;
    }

    private void generateInvoice(User user, double discountAmount, double deliveryCharges, double finalTotal, 
            String paymentMethod, String currency, double exchangeRate) {
        String symbol = currency.equals("USD") ? "$" : currency.equals("EUR") ? "€" : "₹";
        System.out.println("\n=== Invoice ===");
        System.out.println(user.getDetails());
        System.out.println("Payment Method: " + paymentMethod);
        System.out.println("\nItems Purchased:");
        for (CartItem item : cartItems) {
            System.out.printf("%s | Qty: %d | Subtotal: %s%.2f%n", 
                item.product.toString(currency, exchangeRate), item.quantity, symbol, item.getSubtotal() * exchangeRate);
        }
        System.out.printf("\nSubtotal: %s%.2f%n", symbol, getSubtotal() * exchangeRate);
        System.out.printf("Discount: %s%.2f%n", symbol, discountAmount * exchangeRate);
        System.out.printf("Delivery Charges: %s%.2f%n", symbol, deliveryCharges * exchangeRate);
        System.out.printf("Final Total: %s%.2f%n", symbol, finalTotal * exchangeRate);
        System.out.println("\nThank you for your purchase!");
    }
}

abstract class Discount {
    public abstract double applyDiscount(double total);
}

class CategoryDiscount extends Discount {
    @Override
    public double applyDiscount(double total) {
        if (total > 5000) {
            System.out.println("Applying 10% discount for orders above ₹5000!");
            return total * 0.10;
        }
        return 0;
    }
}

public class ECommerceApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Select Currency:");
        System.out.println("1) INR (₹)\n2) USD ($)\n3) EUR (€)");
        System.out.print("Enter your choice (1-3): ");
        int currencyChoice = scanner.nextInt();
        scanner.nextLine();
        String currency;
        double exchangeRate;
        switch (currencyChoice) {
            case 2:
                currency = "USD";
                exchangeRate = 0.012;
                break;
            case 3:
                currency = "EUR";
                exchangeRate = 0.011;
                break;
            default:
                currency = "INR";
                exchangeRate = 1.0;
                break;
        }

        System.out.print("Enter your Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter your Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter your Phone Number: ");
        String phoneNumber = scanner.nextLine();
        System.out.print("Enter your Password: ");
        String password = scanner.nextLine();
        User user = new User(name, email, phoneNumber, password);

        ArrayList<Product> products = new ArrayList<>();
        products.add(new Product("iPhone 16 Pro Max 256 GB", 135999.9, "Electronics"));
        products.add(new Product("Samsung Galaxy S25 Ultra", 141900.9, "Electronics"));
        products.add(new Product("Samsung 75-inch 4K TV", 95999.0, "Electronics"));
        products.add(new Product("Sony Alpha ILCE-7SM3 Camera", 296980.98, "Electronics"));
        products.add(new Product("IFB 9 Kg AI Washing Machine", 37800.98, "Electronics"));

        products.add(new Product("White Shirt", 1200.0, "Fashion"));
        products.add(new Product("Black Kurta", 4200.0, "Fashion"));
        products.add(new Product("White Cropped Vest Top", 990.0, "Fashion"));
        products.add(new Product("Light-Support Sports Bra", 1870.98, "Fashion"));
        products.add(new Product("Floral Print Fit & Flare Dress", 750.0, "Fashion"));

        products.add(new Product("Cetaphil Gentle Face Wash", 377.0, "Beauty"));
        products.add(new Product("Vitamin C 16% Face Serum", 569.0, "Beauty"));
        products.add(new Product("Laneige Lip Glowy Balm", 789.0, "Beauty"));
        products.add(new Product("COCO MADEMOISELLE Perfume", 27900.0, "Beauty"));
        products.add(new Product("ROJA De La Nuit 2 Perfume", 109200.0, "Beauty"));

        products.add(new Product("ON Whey Protein Powder", 7900.0, "Health"));
        products.add(new Product("Probiotics Supplement", 900.0, "Health"));
        products.add(new Product("Menstrual Cup for Women", 310.0, "Health"));
        products.add(new Product("Stayfree Secure Nights XXL", 270.0, "Health"));
        products.add(new Product("Durex Extra Time Condoms", 575.0, "Health"));

        Cart cart = new Cart();
        boolean shopping = true;

        while (shopping) {
            System.out.println("\nOptions:");
            System.out.println("1) Shop by Category\n2) Manage Cart\n3) Checkout");
            System.out.print("Select an option (1-3): ");
            int mainChoice = scanner.nextInt();
            scanner.nextLine();

            if (mainChoice == 2) {
                cart.manageCart(scanner);
            } else if (mainChoice == 3) {
                shopping = false;
            } else if (mainChoice == 1) {
                System.out.println("\nCategories:");
                System.out.println("1) Electronics\n2) Fashion\n3) Beauty\n4) Health");
                System.out.print("Select category (1-4): ");
                int categoryChoice = scanner.nextInt();
                scanner.nextLine();

                String selectedCategory = switch (categoryChoice) {
                    case 1 -> "Electronics";
                    case 2 -> "Fashion";
                    case 3 -> "Beauty";
                    case 4 -> "Health";
                    default -> {
                        System.out.println("Invalid choice! Try again.");
                        yield null;
                    }
                };

                if (selectedCategory == null) continue;

                System.out.println("\n=== " + selectedCategory + " Products ===");
                ArrayList<Product> filteredItems = new ArrayList<>();
                for (Product product : products) {
                    if (product.category.equalsIgnoreCase(selectedCategory)) {
                        filteredItems.add(product);
                    }
                }

                if (filteredItems.isEmpty()) {
                    System.out.println("No items available in this category.");
                    continue;
                }

                for (int i = 0; i < filteredItems.size(); i++) {
                    System.out.println((i + 1) + ") " + filteredItems.get(i).toString(currency, exchangeRate));
                }

                System.out.print("\nEnter the number of the item to add (or 0 to skip): ");
                int itemChoice = scanner.nextInt();
                if (itemChoice > 0 && itemChoice <= filteredItems.size()) {
                    System.out.print("Enter quantity: ");
                    int quantity = scanner.nextInt();
                    if (quantity > 0) {
                        cart.addItem(filteredItems.get(itemChoice - 1), quantity);
                    } else {
                        System.out.println("Invalid quantity!");
                    }
                } else if (itemChoice != 0) {
                    System.out.println("Invalid selection!");
                }
                scanner.nextLine();

                cart.displayCart(currency, exchangeRate);
            } else {
                System.out.println("Invalid option!");
            }

            if (mainChoice != 3) {
                System.out.print("\nContinue shopping? (yes/no): ");
                shopping = scanner.nextLine().trim().toLowerCase().equals("yes");
            }
        }

        System.out.print("Enter coupon code (or press Enter to skip): ");
        String couponCode = scanner.nextLine().trim();
        if (couponCode.isEmpty()) couponCode = null;

        cart.checkout(new CategoryDiscount(), couponCode, user, scanner, currency, exchangeRate);
        System.out.println("\nThank you for shopping with us!");
    }
}
