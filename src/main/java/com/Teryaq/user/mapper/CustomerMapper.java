package com.Teryaq.user.mapper;

import org.springframework.stereotype.Component;

import com.Teryaq.user.dto.CustomerDTORequest;
import com.Teryaq.user.dto.CustomerDTOResponse;
import com.Teryaq.user.entity.Customer;

@Component
public class CustomerMapper {

    public CustomerDTOResponse toResponse(Customer customer){

        if(customer == null) return null;
        CustomerDTOResponse response = new CustomerDTOResponse();

        response.setId(customer.getId());
        response.setName(customer.getName());

        // boolean isDebit = customer.isDebit();
        // response.setDebit(isDebit);

        // if (isDebit) {
        //     response.setDebt(customer.getDebt());
        //     response.setPhoneNumber(customer.getPhoneNumber());
        //     response.setAddress(customer.getAddress());
        // }

        return response;
    }

    public Customer toEntity(CustomerDTORequest dto){
      //  Float debt = dto.getDebt() != null ? dto.getDebt() : 0;
        // إذا الاسم فارغ أو null، عيّن القيمة الافتراضية
        String name = (dto.getName() == null || dto.getName().isBlank()) ? "cash customer" : dto.getName();
        Customer customer = new Customer();
        customer.setName(name);
      //  customer.setDebt(debt);
        // if (debt > 0) {
        //     if (dto.getPhoneNumber() == null || dto.getPhoneNumber().isEmpty()) {
        //         throw new IllegalArgumentException("Phone Number is required for debit customers.");
        //     }
        //     if (dto.getAddress() == null || dto.getAddress().isEmpty()) {
        //         throw new IllegalArgumentException("Address is required for debit customers.");
        //     }
        //     customer.setPhoneNumber(dto.getPhoneNumber());
        //     customer.setAddress(dto.getAddress());  
        // }
        return customer;
    }

    public void updateEntityFromDto(Customer customer, CustomerDTORequest dto) {
      //  Float debt = dto.getDebt() != null ? dto.getDebt() : 0;
        String name = (dto.getName() == null || dto.getName().isBlank()) ? "cash customer" : dto.getName();
        customer.setName(name);
       // customer.setDebt(debt);
        // if (debt > 0) {
        //     if (dto.getPhoneNumber() == null || dto.getPhoneNumber().isEmpty()) {
        //         throw new IllegalArgumentException("Phone Number is required for debit customers.");
        //     }
        //     if (dto.getAddress() == null || dto.getAddress().isEmpty()) {
        //         throw new IllegalArgumentException("Address is required for debit customers.");
        //     }
        //     customer.setPhoneNumber(dto.getPhoneNumber());
        //     customer.setAddress(dto.getAddress());
        // }
        // إذا لم يكن آجل، لا يتم تحديث الرقم والعنوان
    }
}
