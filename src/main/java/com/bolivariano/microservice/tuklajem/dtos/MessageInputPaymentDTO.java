package com.bolivariano.microservice.tuklajem.dtos;

import javax.xml.datatype.XMLGregorianCalendar;

import com.bolivariano.microservice.tuklajem.enums.Channel;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MessageInputPaymentDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss",timezone = "America/Guayaquil")
    private XMLGregorianCalendar fecha;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd",timezone = "America/Guayaquil")
    private XMLGregorianCalendar fechaPago;


    private Channel canal;
    private String cuenta;
    private String depuracion;
    private String esquemaFirma;
    private String moneda;
    private String nombreCliente;
    private String oficina;
    private String secuencial;
    private String tipoCuenta;
    private String transaccion;
    private String usuario;
    private Double valorComision;
    private Double valorPago;
    
    private MessageProcessReceiptDTO recibos;
    private MessageProcessServiceDTO servicio;
    private MessageProcessAditionalDataDTO datosAdicionales;
}