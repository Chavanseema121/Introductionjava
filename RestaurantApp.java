import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import javaproject.GetFileData;

class MenuItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private int menuId;
    private String name;
    private double price;

    public MenuItem(int menuId, String name, double price) {
        this.menuId = menuId;
        this.name = name;
        this.price = price;
    }

    @Override
    public String toString() {
        return "Menu Item [ID: " + menuId + ", Name: " + name + ", Price: $" + price + "]";
    }

    public int getMenuId() {
        return menuId;
    }

    public double getPrice() {
        return price;
    }
}

class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    private static int orderIdCounter = 1;

    private int orderId;
    private List<MenuItem> items;
    private Date orderDate;
    private double totalAmount;
    private String status;

    public Order(List<MenuItem> items) {
        this.orderId = orderIdCounter++;
        this.items = items;
        this.orderDate = new Date();
        this.totalAmount = calculateTotalAmount();
        this.status = "Active";
    }

    private double calculateTotalAmount() {
        return items.stream().mapToDouble(MenuItem::getPrice).sum();
    }

    public void cancelOrder() {
        this.status = "Cancelled";
    }

    public int getOrderId() {
        return orderId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    @Override
    public String toString() {
        return "Order [ID: " + orderId + ", Date: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(orderDate) + ", Total Amount: $" + totalAmount + ", Status: " + status + "]";
    }
}

class CollectionReport implements Serializable {
    private static final long serialVersionUID = 1L;

    private Date date;
    private double totalCollection;

    public CollectionReport(Date date, double totalCollection) {
        this.date = date;
        this.totalCollection = totalCollection;
    }

    public Date getDate() {
        return date;
    }

    public double getTotalCollection() {
        return totalCollection;
    }

    @Override
    public String toString() {
        return "Collection Report [Date: " + new SimpleDateFormat("yyyy-MM-dd").format(date) + ", Total Collection: $" + totalCollection + "]";
    }
}

public class RestaurantApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static final List<MenuItem> menu = new ArrayList<>();
    private static final List<Order> orders = new ArrayList<>();
    private static final List<CollectionReport> collections = new ArrayList<>();

    public static void main(String[] args) {
        loadMenuData();
        loadOrderData();
        loadCollectionData();

        while (true) {
            System.out.println("1. Place Order");
            System.out.println("2. Cancel Order");
            System.out.println("3. View Daily Collection Report");
            System.out.println("4. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    placeOrder();
                    break;
                case 2:
                    cancelOrder();
                    break;
                case 3:
                    viewDailyCollectionReport();
                    break;
                case 4:
                    saveData();
                    System.out.println("Exiting the application. Thank you!");
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }

    private static void loadMenuData() {
        try (Scanner fileScanner = new Scanner(new File("menu.txt"))) {
            while (fileScanner.hasNextLine()) {
                String[] parts = fileScanner.nextLine().split(",");
                menu.add(new MenuItem(Integer.parseInt(parts[0]), parts[1], Double.parseDouble(parts[2])));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void loadOrderData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("orders.ser"))) {
            orders.addAll((List<Order>) ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void loadCollectionData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("collections.ser"))) {
            collections.addAll((List<CollectionReport>) ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    

public class GetFileData {
    private static final String MENU_FILE_PATH = "menu.txt";
    private static final String ORDER_FILE_PATH = "orders.ser";
    private static final String COLLECTION_FILE_PATH = "collections.ser";

    // ... (previous methods remain unchanged)

    public static void saveOrderData(List<Order> orders) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ORDER_FILE_PATH))) {
            oos.writeObject(orders);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error saving order data: " + e.getMessage());
        }
    }

    // ... (remaining methods remain unchanged)
}

    private static void placeOrder() {
        System.out.println("Menu:");
        menu.forEach(System.out::println);

        System.out.println("Enter the menu item IDs (comma-separated) for the order:");
        String[] menuItemIds = scanner.nextLine().split(",");

        List<MenuItem> selectedItems = new ArrayList<>();
        for (String menuItemId : menuItemIds) {
            int id = Integer.parseInt(menuItemId.trim());
            Optional<MenuItem> menuItem = menu.stream().filter(item -> item.getMenuId() == id).findFirst();
            menuItem.ifPresent(selectedItems::add);
        }

        Order order = new Order(selectedItems);
        orders.add(order);

        System.out.println("Order placed successfully!");
        System.out.println(order);

        GetFileData.saveOrderData(orders);
    }

    private static void cancelOrder() {
        System.out.println("Enter the order ID to cancel:");
        int orderId = scanner.nextInt();
        Optional<Order> orderToCancel = orders.stream().filter(order -> order.getOrderId() == orderId).findFirst();

        if (orderToCancel.isPresent()) {
            orderToCancel.get().cancelOrder();
            System.out.println("Order cancelled successfully!");
            GetFileData.saveOrderData(orders);
        } else {
            System.out.println("Order not found.");
        }
    }

    private static void viewDailyCollectionReport() {
        System.out.println("Enter the date to view the collection report (yyyy-MM-dd):");
        String dateString = scanner.nextLine();

        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
            Optional<CollectionReport> collectionReport = collections.stream()
                    .filter(report -> report.getDate().equals(date))
                    .findFirst();

            if (collectionReport.isPresent()) {
                System.out.println(collectionReport.get());
            } else {
                System.out.println("No collection report available for the specified date.");
            }
        } catch (java.text.ParseException e) {
            System.out.println("Invalid date format. Please enter the date in yyyy-MM-dd format.");
        }
    }

    private static void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("orders.ser"))) {
            oos.writeObject(orders);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("collections.ser"))) {
            oos.writeObject(collections);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

