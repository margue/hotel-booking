package persistence;

import service.CustomerName;

import java.util.List;
import java.util.Map;

public class Invoice {

    private CustomerName customerName;
    private Map<String, List<BookingInterval>> bookingsForRooms;
    private double totalAmount;

    public Invoice(CustomerName customerName, Map<String, List<BookingInterval>> bookingsForRooms, double totalAmount) {
        this.customerName = customerName;
        this.bookingsForRooms = bookingsForRooms;
        this.totalAmount = totalAmount;
    }

    public CustomerName getCustomerName() {
        return customerName;
    }

    public Map<String, List<BookingInterval>> getBookingsForRooms() {
        return bookingsForRooms;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}
