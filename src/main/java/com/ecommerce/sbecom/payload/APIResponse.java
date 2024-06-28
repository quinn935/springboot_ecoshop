package com.ecommerce.sbecom.payload;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
//api response for exceptions
public class APIResponse {
    public String message;
    private boolean status;
}
