package service;

import persistence.Payment;
import persistence.PaymentRepository;

import java.util.List;

public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public void payAmount(String customerName, double amount){
        List<Payment> customerPayments = paymentRepository.load(customerName);
        customerPayments.add(new Payment(customerName, amount));
        paymentRepository.save(customerName, customerPayments);
    }
}
