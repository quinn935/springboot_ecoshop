package com.ecommerce.sbecom.service;

import com.ecommerce.sbecom.exceptions.ResourceNotFoundException;
import com.ecommerce.sbecom.model.Address;
import com.ecommerce.sbecom.model.User;
import com.ecommerce.sbecom.payload.AddressDTO;
import com.ecommerce.sbecom.respositories.AddressRepository;
import com.ecommerce.sbecom.respositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AddressServiceImpl implements AddressService{
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        Address address = modelMapper.map(addressDTO, Address.class);

        List<Address> addressList = user.getAddresses();
        addressList.add(address);
        user.setAddresses(addressList);

        address.setUser(user);
        Address savedAddress = addressRepository.save(address);
        return modelMapper.map(savedAddress, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAddresses() {
        List<Address> addresses = addressRepository.findAll();
        return addresses.stream().map(address -> {
            return modelMapper.map(address, AddressDTO.class);
        }).toList();
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(()->new ResourceNotFoundException("Address", "addressId", addressId));
        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getUserAddresses(User user) {
        List<Address> addresses = user.getAddresses();
        List<AddressDTO> addressDTOs = addresses.stream().map(address -> modelMapper.map(address, AddressDTO.class)).toList();
        return addressDTOs;
    }

    @Override
    public AddressDTO updateAddressById(Long addressId, AddressDTO addressDTO) {
        Address addressFromDb = addressRepository.findById(addressId)
                .orElseThrow(()->new ResourceNotFoundException("Address", "addressId", addressId));

        addressFromDb.setCity(addressDTO.getCity());
        addressFromDb.setPincode(addressDTO.getPincode());
        addressFromDb.setCountry(addressDTO.getCountry());
        addressFromDb.setState(addressDTO.getState());
        addressFromDb.setBuildingName(addressDTO.getBuildingName());

        Address updatedAddress = addressRepository.save(addressFromDb);

        User user = addressFromDb.getUser();
        user.getAddresses().removeIf(address -> address.getAddressId().equals(addressId));
        user.getAddresses().add(updatedAddress);
        userRepository.save(user);

        return modelMapper.map(updatedAddress, AddressDTO.class);

    }

    @Override
    public String deleteById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(()->new ResourceNotFoundException("Address", "addressId", addressId));
        User user = address.getUser();
        user.getAddresses().removeIf(userAddress -> userAddress.getAddressId().equals(addressId));
        userRepository.save(user);
        addressRepository.delete(address);

        return "Address with id " + addressId + " has been deleted successfully";
    }
}
