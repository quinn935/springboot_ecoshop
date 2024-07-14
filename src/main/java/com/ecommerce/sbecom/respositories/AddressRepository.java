package com.ecommerce.sbecom.respositories;

import com.ecommerce.sbecom.model.Address;
import com.ecommerce.sbecom.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {

}