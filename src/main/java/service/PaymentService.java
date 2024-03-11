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

    public void payAmount(CustomerName customerName, double amount){
        List<Payment> customerPayments = paymentRepository.load(new CustomerName(customerName.customerName()));
        customerPayments.add(new Payment(new CustomerName(customerName.customerName()), amount));
        paymentRepository.save(new CustomerName(customerName.customerName()), customerPayments);
    }

    public double remainingCredit(CustomerName customerName){
        return paymentRepository.load(new CustomerName(customerName.customerName())).stream()
                .mapToDouble(payment -> payment.getPaidAmount() - payment.getUsedAmount())
                .sum();
    }

    public Invoice produceInvoice(CustomerName customerName, LocalDate endDate, List<String> roomNumbers) {
        List<Room> bookedRooms = roomRepository.findAllRoomsWithBookingIntervalsByCustomerName(new CustomerName(customerName.customerName()))
                .stream().filter(r -> roomNumbers.contains(r.getRoomNumber())).collect(Collectors.toList());
        Map<String, List<BookingInterval>> bookingsForRooms = new HashMap<>();
        bookedRooms.forEach(room -> {
            List<BookingInterval> applicableBookings = room.getBookings().stream()
                    .filter(booking -> Objects.equals(booking.getCustomerName(), customerName.customerName()))
                    .filter(booking -> !booking.getEndDate().isAfter(endDate))
                    .filter(BookingInterval::isCheckedIn).collect(Collectors.toList());
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
        double credit = remainingCredit(new CustomerName(customerName.customerName()));
        if(totalAmount > credit){
            throw new IllegalStateException("Payment insufficient. Necessary payment: " + (totalAmount - credit));
        }

        List<Payment> payments = paymentRepository.load(new CustomerName(customerName.customerName()));
        payments.sort((o1, o2) -> o1.getPaymentDate().isEqual(o2.getPaymentDate()) ? 0 :
                        o1.getPaymentDate().isBefore(o2.getPaymentDate()) ? -1 : 1);
        double remainingTotalAmount = totalAmount;
        for (Payment payment: payments){
            if(remainingTotalAmount > 0.0){
                double remainingCreditForPayment = payment.getPaidAmount() - payment.getUsedAmount();
                if(remainingCreditForPayment >= remainingTotalAmount){
                    payment.reduceCreditBy(remainingTotalAmount);
                    remainingTotalAmount = 0.0;
                    break;
                } else {
                    payment.reduceCreditBy(remainingCreditForPayment);
                    remainingTotalAmount -= remainingCreditForPayment;
                }
            } else {
                break;
            }
        }
        paymentRepository.save(new CustomerName(customerName.customerName()), payments);

        roomRepository.markBookingsAsInvoiced(bookingsForRooms);

        return new Invoice(new CustomerName(customerName.customerName()), bookingsForRooms, totalAmount);
    }
}
