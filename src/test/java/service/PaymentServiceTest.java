package service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import persistence.PaymentRepository;

class PaymentServiceTest {
    String customer1 = "Peter Meier";
    String customer2 = "Lisa Müller";

    public PaymentService setupPaymentService(PaymentRepository paymentRepository){
        return new PaymentService(paymentRepository);
    }

    @Test
    public void payAmount_customerPaidForTheFirstTime() {
        // GIVEN
        PaymentRepository paymentRepository = new PaymentRepository();
        PaymentService service = setupPaymentService(paymentRepository);

        // WHEN
        service.payAmount(customer1, 42.0);

        // THEN
        Assertions.assertThat(paymentRepository.load(customer1)).hasSize(1);
        Assertions.assertThat(paymentRepository.load(customer1).get(0).getPaidAmount()).isEqualTo(42.0);
    }

    @Test
    public void payAmount_customerPaidForTheSecondTime() {
        // GIVEN
        PaymentRepository paymentRepository = new PaymentRepository();
        PaymentService service = setupPaymentService(paymentRepository);
        service.payAmount(customer1, 42.0);

        // WHEN
        service.payAmount(customer1, 120.0);

        // THEN
        Assertions.assertThat(paymentRepository.load(customer1)).hasSize(2);
        Assertions.assertThat(paymentRepository.load(customer1).get(0).getPaidAmount()).isEqualTo(42.0);
        Assertions.assertThat(paymentRepository.load(customer1).get(1).getPaidAmount()).isEqualTo(120.0);
    }

    @Test
    public void payAmount_secondCustomerPaidForTheFirstTime() {
        // GIVEN
        PaymentRepository paymentRepository = new PaymentRepository();
        PaymentService service = setupPaymentService(paymentRepository);
        service.payAmount(customer1, 42.0);

        // WHEN
        service.payAmount(customer2, 120.0);

        // THEN
        Assertions.assertThat(paymentRepository.load(customer1)).hasSize(1);
        Assertions.assertThat(paymentRepository.load(customer2)).hasSize(1);
        Assertions.assertThat(paymentRepository.load(customer1).get(0).getPaidAmount()).isEqualTo(42.0);
        Assertions.assertThat(paymentRepository.load(customer2).get(0).getPaidAmount()).isEqualTo(120.0);
    }
}