import java.io.*;
import java.util.*;

class Room implements Serializable {
    private static final long serialVersionUID = 1L;
    private int roomNumber;
    private String category; // Standard, Deluxe, Suite
    private double pricePerNight;
    private boolean isAvailable;

    public Room(int roomNumber, String category, double pricePerNight) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.pricePerNight = pricePerNight;
        this.isAvailable = true;
    }

    public int getRoomNumber() { return roomNumber; }
    public String getCategory() { return category; }
    public double getPricePerNight() { return pricePerNight; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
}

class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;
    private String reservationId;
    private String guestName;
    private int roomNumber;
    private int nights;
    private double totalAmount;
    private boolean isPaid;

    public Reservation(String guestName, int roomNumber, int nights, double totalAmount) {
        this.reservationId = "RES-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.guestName = guestName;
        this.roomNumber = roomNumber;
        this.nights = nights;
        this.totalAmount = totalAmount;
        this.isPaid = true; // Payment simulated upon confirmation
    }

    public String getReservationId() { return reservationId; }
    public String getGuestName() { return guestName; }
    public int getRoomNumber() { return roomNumber; }
    public int getNights() { return nights; }
    public double getTotalAmount() { return totalAmount; }
    public boolean isPaid() { return isPaid; }
}

public class HotelReservationSystem {
    private static final String DATA_FILE = "hotel_data.ser";
    private static List<Room> rooms = new ArrayList<>();
    private static List<Reservation> reservations = new ArrayList<>();

    public static void main(String[] args) {
        loadData();
        Scanner scanner = new Scanner(System.in);
        boolean active = true;

        while (active) {
            System.out.println("\n=== Hotel Reservation System ===");
            System.out.println("1. View Available Rooms");
            System.out.println("2. Book a Room");
            System.out.println("3. Cancel a Reservation");
            System.out.println("4. View All Bookings");
            System.out.println("5. Save & Exit");
            System.out.print("Select an option (1-5): ");

            String option = scanner.nextLine().trim();
            switch (option) {
                case "1":
                    displayAvailableRooms();
                    break;
                case "2":
                    bookRoom(scanner);
                    break;
                case "3":
                    cancelBooking(scanner);
                    break;
                case "4":
                    displayBookings();
                    break;
                case "5":
                    saveData();
                    active = false;
                    System.out.println("Data saved. System exited.");
                    break;
                default:
                    System.out.println("Invalid selection. Try again.");
            }
        }
        scanner.close();
    }

    private static void displayAvailableRooms() {
        System.out.println("\n--- Available Rooms ---");
        System.out.printf("%-10s | %-12s | %-12s\n", "Room No", "Category", "Rate/Night ($)");
        System.out.println("---------------------------------------");
        boolean found = false;
        for (Room r : rooms) {
            if (r.isAvailable()) {
                System.out.printf("%-10d | %-12s | %-12.2f\n", r.getRoomNumber(), r.getCategory(), r.getPricePerNight());
                found = true;
            }
        }
        if (!found) System.out.println("No rooms currently available.");
    }

    private static void bookRoom(Scanner sc) {
        displayAvailableRooms();
        System.out.print("\nEnter Room Number to book: ");
        try {
            int roomNum = Integer.parseInt(sc.nextLine().trim());
            Room targetRoom = null;
            for (Room r : rooms) {
                if (r.getRoomNumber() == roomNum && r.isAvailable()) {
                    targetRoom = r;
                    break;
                }
            }

            if (targetRoom == null) {
                System.out.println("Room not available or does not exist.");
                return;
            }

            System.out.print("Enter Guest Name: ");
            String guestName = sc.nextLine().trim();

            System.out.print("Enter Number of Nights: ");
            int nights = Integer.parseInt(sc.nextLine().trim());
            if (nights <= 0) {
                System.out.println("Invalid number of nights.");
                return;
            }

            double total = nights * targetRoom.getPricePerNight();
            System.out.printf("Total charge: $%.2f. Processing payment simulation...\n", total);
            System.out.println("Payment Approved!");

            targetRoom.setAvailable(false);
            Reservation res = new Reservation(guestName, roomNum, nights, total);
            reservations.add(res);

            System.out.println("\nBooking Confirmed!");
            System.out.println("Booking ID : " + res.getReservationId());
            System.out.println("Guest Name : " + res.getGuestName());
            System.out.println("Room No    : " + res.getRoomNumber());
            System.out.println("Total Paid : $" + res.getTotalAmount());

        } catch (NumberFormatException e) {
            System.out.println("Invalid input format.");
        }
    }

    private static void cancelBooking(Scanner sc) {
        System.out.print("Enter Reservation ID to cancel: ");
        String resId = sc.nextLine().trim();

        Reservation targetRes = null;
        for (Reservation r : reservations) {
            if (r.getReservationId().equalsIgnoreCase(resId)) {
                targetRes = r;
                break;
            }
        }

        if (targetRes == null) {
            System.out.println("Reservation ID not found.");
            return;
        }

        for (Room room : rooms) {
            if (room.getRoomNumber() == targetRes.getRoomNumber()) {
                room.setAvailable(true);
                break;
            }
        }

        reservations.remove(targetRes);
        System.out.printf("Reservation %s has been cancelled. Refund of $%.2f issued.\n", resId, targetRes.getTotalAmount());
    }

    private static void displayBookings() {
        System.out.println("\n--- Confirmed Reservations ---");
        if (reservations.isEmpty()) {
            System.out.println("No active reservations.");
            return;
        }
        System.out.printf("%-12s | %-16s | %-8s | %-8s | %-10s\n", "Booking ID", "Guest Name", "Room", "Nights", "Paid ($)");
        System.out.println("------------------------------------------------------------------");
        for (Reservation r : reservations) {
            System.out.printf("%-12s | %-16s | %-8d | %-8d | %-10.2f\n",
                    r.getReservationId(), r.getGuestName(), r.getRoomNumber(), r.getNights(), r.getTotalAmount());
        }
    }

    @SuppressWarnings("unchecked")
    private static void loadData() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                rooms = (List<Room>) ois.readObject();
                reservations = (List<Reservation>) ois.readObject();
                return;
            } catch (Exception ignored) {}
        }
        // Initialize default rooms if no saved state exists
        rooms.add(new Room(101, "Standard", 75.0));
        rooms.add(new Room(102, "Standard", 75.0));
        rooms.add(new Room(201, "Deluxe", 120.0));
        rooms.add(new Room(202, "Deluxe", 120.0));
        rooms.add(new Room(301, "Suite", 250.0));
    }

    private static void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(rooms);
            oos.writeObject(reservations);
        } catch (IOException e) {
            System.out.println("Failed to persist hotel data: " + e.getMessage());
        }
    }
}
