package persistence;

import service.CustomerName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentRepository {

    private Map<String, List<Payment>> payments = new HashMap<>();

    public List<Payment> load(CustomerName customerName){
        List<Payment> customerPayments = payments.get(customerName.customerName());
        return customerPayments == null ? new ArrayList<>() : customerPayments;
    }

    public void save(CustomerName customerName, List<Payment> customerPayments){
        payments.put(customerName.customerName(), customerPayments);
    }
}
