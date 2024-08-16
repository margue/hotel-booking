package persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvoiceRepository {

    private final Map<String, List<Invoice>> invoices = new HashMap<>();

    public List<Invoice> load(GuestName guestName){
        List<Invoice> guestInvoices = invoices.get(guestName.guestName());
        return guestInvoices == null ? new ArrayList<>() : guestInvoices;
    }

    public void save(Invoice invoice){
        List<Invoice> guestInvoices = invoices.getOrDefault(invoice.getGuestName().guestName(), new ArrayList<>());
        guestInvoices.add(invoice);
        invoices.put(invoice.getGuestName().guestName(), guestInvoices);
    }
}
