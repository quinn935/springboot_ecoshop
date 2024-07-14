package com.ecommerce.sbecom.respositories;

import com.ecommerce.sbecom.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
