package com.fuelup.tienda.dto.request;

import lombok.Data;

@Data
public class OrderRequest {
    private String couponCode;
    private String notes;
    // Dirección de envío (si difiere de la del usuario)
    private String shippingStreet;
    private String shippingCity;
    private String shippingProvince;
    private String shippingPostalCode;
    private String shippingCountry;
}
