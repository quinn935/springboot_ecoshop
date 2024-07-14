package com.ecommerce.sbecom.respositories;

import com.ecommerce.sbecom.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
