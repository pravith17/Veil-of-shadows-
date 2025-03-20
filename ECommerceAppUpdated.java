package Hackathon5;

import java.util.ArrayList;
import java.util.Scanner;

class User {
    private String name;
    private String email;
    private String phoneNumber;
    private String password;
    private String address;
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
    String subcategory;
    int stock;
    
    public Product(String name, double price, String category, String subcategory, int stock) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.subcategory = subcategory;
        this.stock = stock;
    }

    public String toString(String currency, double exchangeRate) {
        String symbol = currency.equals("USD") ? "$" : currency.equals("EUR") ? "€" : "₹";
        return name + " | Price: " + symbol + String.format("%.2f", price * exchangeRate) + " | Stock: " + stock;
    }

    public boolean reduceStock(int quantity) {
        if (stock >= quantity) {
            stock -= quantity;
            return true;
        }
        return false;
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
        if (!product.reduceStock(quantity)) {
            System.out.println("Insufficient stock for " + product.name + "! Available: " + product.stock);
            return;
        }
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

    public void removeItem(int index) {
        if (index >= 0 && index < cartItems.size()) {
            CartItem item = cartItems.get(index);
            item.product.stock += item.quantity;
            cartItems.remove(index);
            System.out.println(item.product.name + " removed from the cart!");
        } else {
            System.out.println("Invalid item number! Try again.");
        }
    }

    public void updateQuantity(int index, int newQuantity) {
        if (index >= 0 && index < cartItems.size()) {
            CartItem item = cartItems.get(index);
            int diff = newQuantity - item.quantity;
            if (diff > 0 && !item.product.reduceStock(diff)) {
                System.out.println("Insufficient stock to increase quantity! Available: " + item.product.stock);
                return;
            }
            if (diff < 0) {
                item.product.stock -= diff;
            }
            if (newQuantity <= 0) {
                removeItem(index);
            } else {
                item.quantity = newQuantity;
                System.out.println("Quantity updated for " + item.product.name + " to " + newQuantity);
            }
        } else {
            System.out.println("Invalid item number! Try again.");
        }
    }

    public void displayCart(String currency, double exchangeRate) {
        if (cartItems.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        double subtotal = 0;
        String symbol = currency.equals("USD") ? "$" : currency.equals("EUR") ? "€" : "₹";
        System.out.println("\n=== Your Cart ===");
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            double itemTotal = item.getSubtotal();
            System.out.printf("%d) %s | Qty: %d | Subtotal: %s%.2f%n", 
                i + 1, item.product.toString(currency, exchangeRate), item.quantity, symbol, itemTotal * exchangeRate);
            subtotal += itemTotal;
        }
        System.out.printf("Subtotal: %s%.2f%n", symbol, subtotal * exchangeRate);
        double deliveryCharges = subtotal > 1000 ? 0.00 : DELIVERY_CHARGES;
        System.out.printf("Delivery Charges: %s%.2f%n", symbol, deliveryCharges * exchangeRate);
        System.out.printf("Total Amount: %s%.2f%n", symbol, (subtotal + deliveryCharges) * exchangeRate);
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
            if (choice >= 1 && choice <= 3) {
                if (choice == 3) break;
                System.out.print("Enter item number (1-" + cartItems.size() + "): ");
                int itemNumber = scanner.nextInt() - 1;
                scanner.nextLine();
                if (choice == 1) {
                    System.out.print("Enter new quantity: ");
                    int newQty = scanner.nextInt();
                    scanner.nextLine();
                    if (newQty >= 0) {
                        updateQuantity(itemNumber, newQty);
                    } else {
                        System.out.println("Invalid quantity! Try again.");
                    }
                } else if (choice == 2) {
                    removeItem(itemNumber);
                }
            } else {
                System.out.println("Invalid input! Try again.");
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

            if (choice >= 1 && choice <= 3) {
                if (choice == 3) {
                    modifyingCart = false;
                } else {
                    System.out.print("Enter item number (1-" + cartItems.size() + "): ");
                    int itemNumber = scanner.nextInt() - 1;
                    scanner.nextLine();
                    if (choice == 1) {
                        System.out.print("Enter new quantity: ");
                        int newQty = scanner.nextInt();
                        scanner.nextLine();
                        if (newQty >= 0) {
                            updateQuantity(itemNumber, newQty);
                            subtotal = getSubtotal();
                            discountAmount = discount.applyDiscount(subtotal);
                            deliveryCharges = subtotal > 1000 ? 0 : DELIVERY_CHARGES;
                            finalTotal = subtotal - discountAmount + deliveryCharges;
                        } else {
                            System.out.println("Invalid quantity! Try again.");
                        }
                    } else if (choice == 2) {
                        removeItem(itemNumber);
                        subtotal = getSubtotal();
                        discountAmount = discount.applyDiscount(subtotal);
                        deliveryCharges = subtotal > 1000 ? 0 : DELIVERY_CHARGES;
                        finalTotal = subtotal - discountAmount + deliveryCharges;
                    }
                }
            } else {
                System.out.println("Invalid input! Try again.");
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
                System.out.println("Invalid input! Try again.");
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
                        System.out.println("Invalid UPI ID format! Try again.");
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
                        System.out.println("Invalid card number! Try again.");
                    }
                } while (!cardNumber.matches("\\d{16}"));
                System.out.print("Enter cardholder name: ");
                String cardHolderName = scanner.nextLine();
                do {
                    System.out.print("Enter CVV (3 digits): ");
                    cvv = scanner.nextLine();
                    if (!cvv.matches("\\d{3}")) {
                        System.out.println("Invalid CVV! Try again.");
                    }
                } while (!cvv.matches("\\d{3}"));
                do {
                    System.out.print("Enter expiry date (MM/YY): ");
                    expiry = scanner.nextLine();
                    if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                        System.out.println("Invalid expiry date! Try again.");
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
        int currencyChoice;
        String currency;
        double exchangeRate;
        do {
            System.out.print("Enter your choice (1-3): ");
            currencyChoice = scanner.nextInt();
            scanner.nextLine();
            switch (currencyChoice) {
                case 1:
                    currency = "INR";
                    exchangeRate = 1.0;
                    break;
                case 2:
                    currency = "USD";
                    exchangeRate = 1.0 / 87.00;
                    break;
                case 3:
                    currency = "EUR";
                    exchangeRate = 1.0 / 94.50;
                    break;
                default:
                    System.out.println("Invalid input! Try again.");
                    currency = null;
                    exchangeRate = 0.0;
            }
        } while (currency == null);

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
        products.add(new Product("iPhone 15 (128GB)", 79900.0, "Electronics", "Mobiles", 50));
        products.add(new Product("Samsung Galaxy S24 (256GB)", 84999.0, "Electronics", "Mobiles", 30));
        products.add(new Product("OnePlus 12 (256GB)", 69999.0, "Electronics", "Mobiles", 40));
        products.add(new Product("Google Pixel 8 (128GB)", 75999.0, "Electronics", "Mobiles", 25));
        products.add(new Product("Xiaomi 14 (512GB)", 59999.0, "Electronics", "Mobiles", 60));
        products.add(new Product("MacBook Air M2 (256GB)", 119900.0, "Electronics", "Laptops", 20));
        products.add(new Product("Dell XPS 13 (512GB)", 139900.0, "Electronics", "Laptops", 15));
        products.add(new Product("HP Spectre x360 (1TB)", 159900.0, "Electronics", "Laptops", 10));
        products.add(new Product("Lenovo Legion 5 (512GB)", 129900.0, "Electronics", "Laptops", 25));
        products.add(new Product("Asus ROG Zephyrus G14", 149900.0, "Electronics", "Laptops", 12));
        products.add(new Product("Samsung 55-inch QLED TV", 84900.0, "Electronics", "Televisions", 30));
        products.add(new Product("LG 65-inch OLED TV", 189900.0, "Electronics", "Televisions", 15));
        products.add(new Product("Sony Bravia 50-inch 4K", 69900.0, "Electronics", "Televisions", 20));
        products.add(new Product("TCL 43-inch Smart TV", 32900.0, "Electronics", "Televisions", 50));
        products.add(new Product("Mi LED TV 55-inch", 54900.0, "Electronics", "Televisions", 40));
        products.add(new Product("Canon EOS R50", 64900.0, "Electronics", "Cameras", 25));
        products.add(new Product("Sony Alpha 7 IV", 249900.0, "Electronics", "Cameras", 5));
        products.add(new Product("Nikon Z6 II", 179900.0, "Electronics", "Cameras", 8));
        products.add(new Product("Fujifilm X-T5", 169900.0, "Electronics", "Cameras", 10));
        products.add(new Product("GoPro HERO12", 39900.0, "Electronics", "Cameras", 30));
        products.add(new Product("Boat Airdopes 141", 1299.0, "Electronics", "Accessories", 100));
        products.add(new Product("JBL Flip 6 Speaker", 11999.0, "Electronics", "Accessories", 50));
        products.add(new Product("Apple AirPods Pro 2", 24900.0, "Electronics", "Accessories", 20));
        products.add(new Product("Samsung Galaxy Buds 2", 9990.0, "Electronics", "Accessories", 40));
        products.add(new Product("Anker PowerBank 20000mAh", 3499.0, "Electronics", "Accessories", 60));
        products.add(new Product("Levi's Men's Slim Jeans", 3199.0, "Fashion", "Men's Clothing", 80));
        products.add(new Product("U.S. Polo Shirt", 2499.0, "Fashion", "Men's Clothing", 100));
        products.add(new Product("Woodland Leather Jacket", 7999.0, "Fashion", "Men's Clothing", 30));
        products.add(new Product("Raymond Formal Trousers", 3499.0, "Fashion", "Men's Clothing", 60));
        products.add(new Product("Nike Track Pants", 2799.0, "Fashion", "Men's Clothing", 70));
        products.add(new Product("Biba Anarkali Suit", 4999.0, "Fashion", "Women's Clothing", 50));
        products.add(new Product("H&M Maxi Dress", 2999.0, "Fashion", "Women's Clothing", 80));
        products.add(new Product("Zara Blazer", 5999.0, "Fashion", "Women's Clothing", 40));
        products.add(new Product("FabIndia Cotton Saree", 3499.0, "Fashion", "Women's Clothing", 60));
        products.add(new Product("Adidas Leggings", 3999.0, "Fashion", "Women's Clothing", 70));
        products.add(new Product("Puma Running Shoes (Men)", 6999.0, "Fashion", "Footwear", 50));
        products.add(new Product("Bata Formal Shoes", 2499.0, "Fashion", "Footwear", 80));
        products.add(new Product("Nike Air Max (Women)", 8999.0, "Fashion", "Footwear", 40));
        products.add(new Product("Skechers Sneakers", 5499.0, "Fashion", "Footwear", 60));
        products.add(new Product("Crocs Classic Clogs", 3499.0, "Fashion", "Footwear", 100));
        products.add(new Product("Titan Men's Watch", 7995.0, "Fashion", "Accessories", 30));
        products.add(new Product("Fastrack Sunglasses", 1999.0, "Fashion", "Accessories", 80));
        products.add(new Product("Daniel Wellington Women's Watch", 12999.0, "Fashion", "Accessories", 20));
        products.add(new Product("Ray-Ban Aviators", 8990.0, "Fashion", "Accessories", 40));
        products.add(new Product("Voyage Leather Belt", 1499.0, "Fashion", "Accessories", 100));
        products.add(new Product("Mamaearth Face Wash", 299.0, "Beauty", "Skincare", 200));
        products.add(new Product("The Ordinary Niacinamide Serum", 750.0, "Beauty", "Skincare", 150));
        products.add(new Product("Neutrogena Sunscreen SPF 50", 899.0, "Beauty", "Skincare", 120));
        products.add(new Product("L'Oréal Night Cream", 1299.0, "Beauty", "Skincare", 100));
        products.add(new Product("Cetaphil Moisturizer", 599.0, "Beauty", "Skincare", 180));
        products.add(new Product("Maybelline Fit Me Foundation", 699.0, "Beauty", "Makeup", 150));
        products.add(new Product("Lakmé Lipstick", 499.0, "Beauty", "Makeup", 200));
        products.add(new Product("NYKAA Eyeliner", 399.0, "Beauty", "Makeup", 180));
        products.add(new Product("Huda Beauty Mascara", 1499.0, "Beauty", "Makeup", 80));
        products.add(new Product("MAC Blush", 2990.0, "Beauty", "Makeup", 50));
        products.add(new Product("Yves Saint Laurent Libre", 9990.0, "Beauty", "Fragrances", 30));
        products.add(new Product("Chanel Coco Mademoiselle", 12900.0, "Beauty", "Fragrances", 20));
        products.add(new Product(" Creed Aventus", 28900.0, "Beauty", "Fragrances", 10));
        products.add(new Product("Jo Malone Peony & Blush", 11900.0, "Beauty", "Fragrances", 15));
        products.add(new Product("Paco Rabanne 1 Million", 6990.0, "Beauty", "Fragrances", 40));
        products.add(new Product("Philips Hair Dryer", 2499.0, "Beauty", "Haircare", 60));
        products.add(new Product("L'Oréal Shampoo", 399.0, "Beauty", "Haircare", 200));
        products.add(new Product("Moroccanoil Treatment", 3490.0, "Beauty", "Haircare", 40));
        products.add(new Product("Dyson Airwrap", 49900.0, "Beauty", "Haircare", 5));
        products.add(new Product("TRESemmé Conditioner", 499.0, "Beauty", "Haircare", 150));
        products.add(new Product("MuscleBlaze Whey Protein (1kg)", 2999.0, "Health", "Supplements", 80));
        products.add(new Product("HealthKart Multivitamin", 599.0, "Health", "Supplements", 150));
        products.add(new Product("Optimum Nutrition BCAA", 1999.0, "Health", "Supplements", 60));
        products.add(new Product("MyProtein Creatine (250g)", 1499.0, "Health", "Supplements", 100));
        products.add(new Product("Himalaya Ashwagandha", 299.0, "Health", "Supplements", 200));
        products.add(new Product("Decathlon Yoga Mat", 999.0, "Health", "Fitness Gear", 100));
        products.add(new Product("Nivia Dumbbells 10kg", 2499.0, "Health", "Fitness Gear", 50));
        products.add(new Product("Fitbit Charge 5", 14999.0, "Health", "Fitness Gear", 30));
        products.add(new Product("Cosco Treadmill", 39900.0, "Health", "Fitness Gear", 10));
        products.add(new Product("Strauss Resistance Bands", 799.0, "Health", "Fitness Gear", 120));
        products.add(new Product("Omron BP Monitor", 2499.0, "Health", "Medical Devices", 40));
        products.add(new Product("Accu-Chek Glucometer", 1499.0, "Health", "Medical Devices", 60));
        products.add(new Product("Dr. Morepen Pulse Oximeter", 999.0, "Health", "Medical Devices", 80));
        products.add(new Product("Vicks Vaporizer", 499.0, "Health", "Medical Devices", 100));
        products.add(new Product("Philips Nebulizer", 2999.0, "Health", "Medical Devices", 30));
        products.add(new Product("Durex Condoms (10s)", 399.0, "Health", "Personal Care", 200));
        products.add(new Product("Stayfree Sanitary Pads (XL)", 349.0, "Health", "Personal Care", 150));
        products.add(new Product("Himalaya Baby Powder", 199.0, "Health", "Personal Care", 180));
        products.add(new Product("Nivea Body Lotion", 499.0, "Health", "Personal Care", 120));
        products.add(new Product("Gillette Razor Pack", 299.0, "Health", "Personal Care", 200));

        Cart cart = new Cart();
        boolean shopping = true;

        while (shopping) {
            System.out.println("\nOptions:");
            System.out.println("1) Shop by Category\n2) Manage Cart\n3) Checkout");
            System.out.print("Select an option (1-3): ");
            int mainChoice = scanner.nextInt();
            scanner.nextLine();

            if (mainChoice >= 1 && mainChoice <= 3) {
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
                        default -> null;
                    };

                    if (selectedCategory == null) {
                        System.out.println("Invalid input! Try again.");
                        continue;
                    }

                    System.out.println("\nSubcategories for " + selectedCategory + ":");
                    ArrayList<String> subcategories = new ArrayList<>();
                    if (selectedCategory.equals("Electronics")) {
                        subcategories.add("Mobiles");
                        subcategories.add("Laptops");
                        subcategories.add("Televisions");
                        subcategories.add("Cameras");
                        subcategories.add("Accessories");
                    } else if (selectedCategory.equals("Fashion")) {
                        subcategories.add("Men's Clothing");
                        subcategories.add("Women's Clothing");
                        subcategories.add("Footwear");
                        subcategories.add("Accessories");
                    } else if (selectedCategory.equals("Beauty")) {
                        subcategories.add("Skincare");
                        subcategories.add("Makeup");
                        subcategories.add("Fragrances");
                        subcategories.add("Haircare");
                    } else if (selectedCategory.equals("Health")) {
                        subcategories.add("Supplements");
                        subcategories.add("Fitness Gear");
                        subcategories.add("Medical Devices");
                        subcategories.add("Personal Care");
                    }

                    for (int i = 0; i < subcategories.size(); i++) {
                        System.out.println((i + 1) + ") " + subcategories.get(i));
                    }
                    System.out.print("Select subcategory (1-" + subcategories.size() + "): ");
                    int subcategoryChoice = scanner.nextInt();
                    scanner.nextLine();

                    if (subcategoryChoice < 1 || subcategoryChoice > subcategories.size()) {
                        System.out.println("Invalid input! Try again.");
                        continue;
                    }

                    String selectedSubcategory = subcategories.get(subcategoryChoice - 1);

                    System.out.println("\n=== " + selectedSubcategory + " Products ===");
                    ArrayList<Product> filteredItems = new ArrayList<>();
                    for (Product product : products) {
                        if (product.category.equalsIgnoreCase(selectedCategory) && 
                            product.subcategory.equalsIgnoreCase(selectedSubcategory)) {
                            filteredItems.add(product);
                        }
                    }

                    if (filteredItems.isEmpty()) {
                        System.out.println("No items available in this subcategory.");
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
                            System.out.println("Invalid quantity! Try again.");
                        }
                    } else if (itemChoice != 0) {
                        System.out.println("Invalid input! Try again.");
                    }
                    scanner.nextLine();

                    cart.displayCart(currency, exchangeRate);
                }
            } else {
                System.out.println("Invalid input! Try again.");
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
