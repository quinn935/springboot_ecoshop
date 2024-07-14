package com.ecommerce.sbecom.respositories;

import com.ecommerce.sbecom.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
