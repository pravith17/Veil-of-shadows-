package Hackathon;

import java.util.ArrayList;
import java.util.Scanner;

class User {
    private String name;
    private String email;
    private String address;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDetails() {
        return "User: " + name + " | Email: " + email + (address != null ? " | Address: " + address : "");
    }
}

class Product {
    String name;
    double price;
    String category;

    public Product(String name, double price, String category) {
        this.name = name;
        this.price = price;
        this.category = category;
    }

    @Override
    public String toString() {
        return name + " | Price: ₹" + String.format("%.2f", price);
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

    public void displayCart() {
        if (cartItems.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        double subtotal = 0;
        System.out.println("\n=== Your Cart ===");
        for (CartItem item : cartItems) {
            double itemTotal = item.getSubtotal();
            System.out.printf("%s | Qty: %d | Subtotal: ₹%.2f%n", item.product, item.quantity, itemTotal);
            subtotal += itemTotal;
        }
        System.out.printf("Subtotal: ₹%.2f%n", subtotal);
        System.out.printf("Delivery Charges: ₹%.2f%n", (subtotal > 1000 ? 0.00 : DELIVERY_CHARGES));
        System.out.printf("Total Amount: ₹%.2f%n", (subtotal + (subtotal > 1000 ? 0.00 : DELIVERY_CHARGES)));
    }

    public double getSubtotal() {
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getSubtotal();
        }
        return subtotal;
    }

    public void checkout(Discount discount, String couponCode, User user, Scanner scanner) {
        if (cartItems.isEmpty()) {
            System.out.println("Cart is empty. Add items before checkout!");
            return;
        }

        double subtotal = getSubtotal();
        double deliveryCharges = DELIVERY_CHARGES;
        double discountAmount = discount.applyDiscount(subtotal);
        double finalTotal = subtotal;

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
                default:
                    System.out.println("Invalid coupon code!");
            }
        }

        finalTotal = subtotal - discountAmount + deliveryCharges;

        displayCart();
        System.out.printf("After Discount: ₹%.2f%n", (subtotal - discountAmount));
        System.out.printf("Final Total (with delivery): ₹%.2f%n", finalTotal);
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
        System.out.print("Enter your choice (1-3): ");
        int paymentChoice = scanner.nextInt();
        scanner.nextLine();

        String paymentMethod = "";
        boolean paymentConfirmed = false;

        switch (paymentChoice) {
            case 1:
                paymentMethod = "Cash on Delivery";
                paymentConfirmed = true;
                break;

            case 2:
                paymentMethod = "UPI";
                System.out.print("Enter your UPI ID (e.g., name@bank): ");
                String upiId = scanner.nextLine();
                System.out.printf("Please complete payment of ₹%.2f to %s%n", finalTotal, upiId);
                System.out.print("Confirm payment completed? (yes/no): ");
                paymentConfirmed = scanner.nextLine().trim().toLowerCase().equals("yes");
                break;

            case 3:
                paymentMethod = "Credit/Debit Card";
                System.out.print("Enter cardholder name: ");
                String cardHolderName = scanner.nextLine();
                System.out.print("Enter 16-digit card number: ");
                String cardNumber = scanner.nextLine();
                System.out.print("Enter CVV (3 digits): ");
                String cvv = scanner.nextLine();
                System.out.print("Enter expiry date (MM/YY): ");
                String expiry = scanner.nextLine();
                System.out.printf("Please complete payment of ₹%.2f using card ending in %s%n", 
                    finalTotal, cardNumber.substring(cardNumber.length() - 4));
                System.out.print("Confirm payment completed? (yes/no): ");
                paymentConfirmed = scanner.nextLine().trim().toLowerCase().equals("yes");
                break;

            default:
                System.out.println("Invalid payment method selected!");
                return;
        }

        if (paymentConfirmed) {
            generateInvoice(user, discountAmount, deliveryCharges, finalTotal, paymentMethod);
            cartItems.clear();
        } else {
            System.out.println("Payment not confirmed. Order cancelled.");
        }
    }

    private void generateInvoice(User user, double discountAmount, double deliveryCharges, double finalTotal, String paymentMethod) {
        System.out.println("\n=== Invoice ===");
        System.out.println(user.getDetails());
        System.out.println("Payment Method: " + paymentMethod);
        System.out.println("\nItems Purchased:");
        for (CartItem item : cartItems) {
            System.out.printf("%s | Qty: %d | Subtotal: ₹%.2f%n", item.product, item.quantity, item.getSubtotal());
        }
        System.out.printf("\nSubtotal: ₹%.2f%n", getSubtotal());
        System.out.printf("Discount: ₹%.2f%n", discountAmount);
        System.out.printf("Delivery Charges: ₹%.2f%n", deliveryCharges);
        System.out.printf("Final Total: ₹%.2f%n", finalTotal);
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

        System.out.print("Enter your Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter your Email: ");
        String email = scanner.nextLine();
        User user = new User(name, email);

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
                System.out.println((i + 1) + ") " + filteredItems.get(i));
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

            cart.displayCart();
            System.out.print("\nContinue shopping? (yes/no): ");
            shopping = scanner.nextLine().trim().toLowerCase().equals("yes");
        }

        System.out.print("Enter coupon code (or press Enter to skip): ");
        String couponCode = scanner.nextLine().trim();
        if (couponCode.isEmpty()) couponCode = null;

        cart.checkout(new CategoryDiscount(), couponCode, user, scanner);
        System.out.println("\nThank you for shopping with us!");
        scanner.close();
    }
}
