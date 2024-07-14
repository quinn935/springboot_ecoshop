package com.ecommerce.sbecom.service;

import com.ecommerce.sbecom.model.User;
import com.ecommerce.sbecom.payload.AddressDTO;

import java.util.List;

public interface AddressService {
    AddressDTO createAddress(AddressDTO addressDTO, User user);

    List<AddressDTO> getAddresses();

    AddressDTO getAddressById(Long addressId);

    List<AddressDTO> getUserAddresses(User user);

    AddressDTO updateAddressById(Long addressId, AddressDTO addressDTO);

    String deleteById(Long addressId);
}
