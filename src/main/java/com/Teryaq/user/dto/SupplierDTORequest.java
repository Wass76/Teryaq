package com.Teryaq.user.dto;

import com.Teryaq.user.Enum.Currency;
import lombok.Data;

@Data
public class SupplierDTORequest {
    private String name;
    private String phone;
    private String address;
    private Currency preferredCurrency;
} 