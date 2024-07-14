package com.ecommerce.sbecom.controller;

import com.ecommerce.sbecom.model.User;
import com.ecommerce.sbecom.payload.AddressDTO;
import com.ecommerce.sbecom.service.AddressService;
import com.ecommerce.sbecom.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private AuthUtil authUtil;

    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO){
        User user = authUtil.loggedInUser();
        AddressDTO savedAddressDTO = addressService.createAddress(addressDTO, user);

        return new ResponseEntity<>(savedAddressDTO, HttpStatus.CREATED);
    }

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> getAddresses(){
        List<AddressDTO> addressDTOs= addressService.getAddresses();

        return new ResponseEntity<>(addressDTOs, HttpStatus.OK);
    }

    @GetMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> getAddressById(@PathVariable Long addressId){
        AddressDTO addressDTO = addressService.getAddressById(addressId);

        return new ResponseEntity<>(addressDTO, HttpStatus.OK);
    }

    @GetMapping("/users/addresses")
    public ResponseEntity<List<AddressDTO>> getUserAddresses(){
        User user = authUtil.loggedInUser();
        List<AddressDTO> addressDTOs= addressService.getUserAddresses(user);

        return new ResponseEntity<>(addressDTOs, HttpStatus.OK);
    }

    @PutMapping("/addresses/{addressId}")
    public ResponseEntity<AddressDTO> updateAddressById(@PathVariable Long addressId, @Valid @RequestBody AddressDTO addressDTO){
        AddressDTO updatedAddressDTO = addressService.updateAddressById(addressId, addressDTO);

        return new ResponseEntity<>(updatedAddressDTO, HttpStatus.OK);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<String> deleteAddressById(@PathVariable Long addressId){
        String status = addressService.deleteById(addressId);

        return new ResponseEntity<>(status, HttpStatus.OK);
    }

}
