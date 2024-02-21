package service;

import persistence.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RoomRepository roomRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
        this.roomRepository = new RoomRepository();
    }

    public PaymentService(PaymentRepository paymentRepository, RoomRepository roomRepository) {
        this.paymentRepository = paymentRepository;
        this.roomRepository = roomRepository;
    }

    public void payAmount(String customerName, double amount){
        List<Payment> customerPayments = paymentRepository.load(customerName);
        customerPayments.add(new Payment(customerName, amount));
        paymentRepository.save(customerName, customerPayments);
    }

    private double remainingCredit(String customerName){
        return paymentRepository.load(customerName).stream()
                .mapToDouble(payment -> payment.getPaidAmount() - payment.getUsedAmount())
                .sum();
    }

    public Invoice produceInvoice(String customerName, LocalDate endDate, List<String> roomNumbers) {
        List<Room> bookedRooms = roomRepository.findAllRoomsWithBookingIntervalsByCustomerName(customerName);
        Map<String, List<BookingInterval>> bookingsForRooms = new HashMap<>();
        bookedRooms.forEach(room -> {
            List<BookingInterval> applicableBookings = room.getBookings().stream()
                    .filter(booking -> Objects.equals(booking.getCustomerName(), customerName))
                    .filter(booking -> !booking.getEndDate().isAfter(endDate))
                    .filter(BookingInterval::getIsCheckedIn).collect(Collectors.toList());
            if(applicableBookings.size() > 0 ){
                bookingsForRooms.put(room.getRoomNumber(), applicableBookings);
            }
        });
        double totalAmount =
                bookingsForRooms.values().stream()
                        .mapToDouble(bookingsForRoom -> bookingsForRoom.stream()
                                .mapToDouble(booking -> 100.0 * booking.dates().size())
                                .sum())
                        .sum();
        double credit = remainingCredit(customerName);
        if(totalAmount > credit){
            throw new IllegalStateException("Payment insufficient. Necessary payment: " + (totalAmount - credit));
        }

        return new Invoice(customerName, bookingsForRooms, totalAmount);
    }
}