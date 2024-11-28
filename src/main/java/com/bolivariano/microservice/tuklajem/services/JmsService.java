package com.bolivariano.microservice.tuklajem.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import com.bolivariano.microservice.tuklajem.dtos.MessageOutputProcessDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.jms.TextMessage;

@Service
public class JmsService {
    
    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void testMessage(String destination, String correlationId) {

        String body = "{\"codigo\":\"0\",\"mensajeUsuario\":\"Consulta ejecutada\",\"estado\":\"OK\",\"mensajeSalidaEjecutarPago\":null,\"mensajeSalidaConsultarDeuda\":{\"codigoError\":\"03\",\"datosAdicionales\":{\"datoAdicional\":[]},\"fechaVencimiento\":null,\"formaPago\":null,\"formaPagoRecibos\":null,\"identificadorDeuda\":null,\"limiteMontoMaximo\":null,\"limiteMontoMinimo\":null,\"mensajeUsuario\":\"El contrato se encuentra al dia \",\"mensajeSistema\":null,\"montoMinimo\":null,\"montoTotal\":null,\"nombreCliente\":null,\"recibos\":null,\"textoAyuda\":null}}\n";

        System.out.print("TEST MESSAGE SALIDA");
        jmsTemplate.send(destination, session -> {
            TextMessage message = session.createTextMessage(body); // Crear un mensaje de texto
            message.setJMSCorrelationID(correlationId); // Establecer el Correlation ID
            return message;
        });

    }

    public void sendResponseMessage(String destination, MessageOutputProcessDTO messageContent, String correlationId) throws JsonProcessingException {

        String messageSerialized = this.objectMapper.writeValueAsString(messageContent); // Serialización
        
        jmsTemplate.send(destination, session -> {
            TextMessage message = session.createTextMessage(messageSerialized); // Crear un mensaje de texto
            message.setJMSCorrelationID(correlationId); // Establecer el Correlation ID
            return message;
        });
    }

    public void sendRequestMessage(String destination, String messageContent, String correlationId){

        jmsTemplate.send(destination, session -> {
            TextMessage message = session.createTextMessage(messageContent); // Crear un mensaje de texto
            message.setJMSCorrelationID(correlationId); // Establecer el Correlation ID
            return message;
        });
    }
}
