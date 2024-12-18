package com.bolivariano.microservice.tuklajem.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.bolivariano.microservice.tuklajem.config.MqConfig;
import com.bolivariano.microservice.tuklajem.dtos.DebtRequestDTO;
import com.bolivariano.microservice.tuklajem.dtos.DebtResponseDTO;
import com.bolivariano.microservice.tuklajem.dtos.MessageAditionalDataDTO;
import com.bolivariano.microservice.tuklajem.dtos.MessageInputConsultDTO;
import com.bolivariano.microservice.tuklajem.dtos.MessageInputPaymentDTO;
import com.bolivariano.microservice.tuklajem.dtos.MessageInputProcessDTO;
import com.bolivariano.microservice.tuklajem.dtos.MessageInputRevertPaymentDTO;
import com.bolivariano.microservice.tuklajem.dtos.MessageOutputConsultDTO;
import com.bolivariano.microservice.tuklajem.dtos.MessageOutputPaymentDTO;
import com.bolivariano.microservice.tuklajem.dtos.MessageOutputProcessDTO;
import com.bolivariano.microservice.tuklajem.dtos.MessageOutputRevertPaymentDTO;
import com.bolivariano.microservice.tuklajem.dtos.MessageProcessAditionalDataDTO;
import com.bolivariano.microservice.tuklajem.dtos.PaymentRequestDTO;
import com.bolivariano.microservice.tuklajem.dtos.PaymentResponseDTO;
import com.bolivariano.microservice.tuklajem.dtos.RevertResponseDTO;
import com.bolivariano.microservice.tuklajem.dtos.RevertRequestDTO;
import com.bolivariano.microservice.tuklajem.enums.MessageStatus;
import com.bolivariano.microservice.tuklajem.enums.ProviderErrorCode;
import com.bolivariano.microservice.tuklajem.exception.ResponseExecption;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class ConsumerService {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JmsService jmsService;

	@Autowired
	private ProviderService providerService;

	public void stage(String message, String correlationId) throws JsonProcessingException {
		try {

			MessageInputProcessDTO messageInputProcessDTO = objectMapper
					.readValue(message, MessageInputProcessDTO.class); // Deserialización

			switch (messageInputProcessDTO.getTipoFlujo()) {
				case CONSULTA:
					this.consulting(messageInputProcessDTO.getMensajeEntradaConsultarDeuda(), correlationId);
					break;
				case PAGO:
					this.payment(messageInputProcessDTO.getMensajeEntradaEjecutarPago(), correlationId);
					break;
				case REVERSO:
					this.revert(messageInputProcessDTO.getMensajeEntradaEjecutarReverso(), correlationId);
					break;
				default:
					throw new ResponseExecption(HttpStatus.NOT_ACCEPTABLE, "type operation null");
			}
		} catch (JsonProcessingException e) {
			log.error("❌ ERROR DE STAGE: {}", e.getMessage(), e);
			MessageOutputProcessDTO messageOutputProcessDTO = new MessageOutputProcessDTO();
			MessageOutputConsultDTO messageOutputConsultDTO = new MessageOutputConsultDTO();

			messageOutputConsultDTO.setCodigoError("300");
			messageOutputConsultDTO.setMensajeUsuario(e.getMessage());

			messageOutputProcessDTO.setEstado(MessageStatus.ERROR);
			messageOutputProcessDTO.setCodigo("0");
			messageOutputProcessDTO.setMensajeUsuario("CONSULTA EJECUTADA");

			messageOutputProcessDTO.setMensajeSalidaConsultarDeuda(messageOutputConsultDTO);

			jmsService.sendResponseMessage(MqConfig.CHANNEL_RESPONSE, messageOutputProcessDTO, correlationId);
		}

	}

	public void consulting(MessageInputConsultDTO messageInputProcess, String correlationId)
			throws JsonProcessingException {
		try {

			log.info("📤 INICIANDO PROCESO DE CONSULTA");

			MessageOutputProcessDTO messageOutputProcessDTO = new MessageOutputProcessDTO();
			MessageOutputConsultDTO messageOutputConsultDTO = new MessageOutputConsultDTO();

			String identifier = messageInputProcess
					.getServicio()
					.getIdentificador();

			MessageProcessAditionalDataDTO aditionalsData = messageInputProcess
					.getServicio()
					.getDatosAdicionales();

			MessageAditionalDataDTO terminal = Arrays.stream(aditionalsData.getDatoAdicional())
					.filter(item -> item.getCodigo().equals("e_term"))
					.findFirst()
					.orElse(null);

			DebtRequestDTO debtRequest = new DebtRequestDTO();

			// Data Binding
			debtRequest.setIdentificador(identifier);
			debtRequest.setTerminal(terminal.getValor());
			debtRequest.setFecha(messageInputProcess.getFecha());
			debtRequest.setHora(messageInputProcess.getFecha());

			DebtResponseDTO debt = this.providerService.getDebt(debtRequest);

			if (debt.getCod_respuesta().equals(ProviderErrorCode.TRANSACCION_ACEPTADA.getcode())) {
				// * ------------------- */
				// Mesaje Salida Consulta
				// ? Los valores monto minimo y maximo estan al revez
				// ? porque el provvedor los manda asi, porque?... nose...
				messageOutputConsultDTO.setMontoMinimo(debt.getValor_maximo().doubleValue());
				messageOutputConsultDTO.setLimiteMontoMinimo(debt.getValor_maximo().doubleValue());
				messageOutputConsultDTO.setLimiteMontoMaximo(debt.getValor_minimo().doubleValue());

				// * ------------------- */
				messageOutputConsultDTO.setMensajeSistema("CONSULTA EJECUTADA");
				messageOutputConsultDTO.setCodigoError(debt.getCod_respuesta());
				messageOutputConsultDTO.setNombreCliente(debt.getNom_cliente());
				messageOutputConsultDTO.setMensajeUsuario(debt.getMsg_respuesta());
				messageOutputConsultDTO.setIdentificadorDeuda(debt.getIdentificador_deuda());
				messageOutputConsultDTO.setDatosAdicionales(aditionalsData);
				// messageOutputConsultDTO.setMontoTotal(10.00);

			}

			// Mensaje de salida proceso;
			messageOutputProcessDTO.setEstado(MessageStatus.OK);
			messageOutputProcessDTO.setCodigo(debt.getCod_respuesta());
			messageOutputProcessDTO.setMensajeUsuario(debt.getMsg_respuesta());
			messageOutputProcessDTO.setMensajeSalidaConsultarDeuda(messageOutputConsultDTO);

			log.info("📥 FINALIZANDO PROCESO DE CONSULTA");

			jmsService.sendResponseMessage(
					MqConfig.CHANNEL_RESPONSE,
					messageOutputProcessDTO,
					correlationId);

		} catch (Exception e) {

			log.error("❌ ERROR AL GENERAR CONSULTA: {}", e.getMessage(), e);
			MessageOutputProcessDTO messageOutputProcessDTO = new MessageOutputProcessDTO();
			MessageOutputConsultDTO messageOutputConsultDTO = new MessageOutputConsultDTO();

			messageOutputConsultDTO.setCodigoError("300");
			messageOutputConsultDTO.setMensajeUsuario(e.getMessage());

			messageOutputProcessDTO.setEstado(MessageStatus.ERROR);
			messageOutputProcessDTO.setCodigo("0");
			messageOutputProcessDTO.setMensajeUsuario("CONSULTA NO EJECUTADA");

			messageOutputProcessDTO.setMensajeSalidaConsultarDeuda(messageOutputConsultDTO);

			jmsService.sendResponseMessage(MqConfig.CHANNEL_RESPONSE, messageOutputProcessDTO, correlationId);
		}

	}

	private void payment(MessageInputPaymentDTO messageInputProcess, String correlationId)
			throws JsonProcessingException {
		try {
			log.info("📤 INICIANDO PROCESO DE PAGO");

			MessageOutputProcessDTO messageOutputProcessDTO = new MessageOutputProcessDTO();
			MessageOutputPaymentDTO messageOutputPaymentDTO = new MessageOutputPaymentDTO();

			String identifier = messageInputProcess
					.getServicio()
					.getIdentificador();

			MessageProcessAditionalDataDTO aditionalsData = messageInputProcess
					.getServicio()
					.getDatosAdicionales();

			MessageAditionalDataDTO terminal = Arrays.stream(aditionalsData.getDatoAdicional())
					.filter(item -> item.getCodigo().equals("e_term"))
					.findFirst()
					.orElse(null);

			Integer importe = BigDecimal.valueOf(messageInputProcess.getValorPago())
					.setScale(2, RoundingMode.HALF_UP)
					.movePointRight(2)
					.intValue();

			PaymentRequestDTO paymentRequest = new PaymentRequestDTO();

			// tengo que parchear una fecha actual de la maquina +5m en el futuro porque no
			// puedo pagar en tiempo pasado, entenderia que pudiera pagar dentro de un rango
			// de tiempo
			LocalDateTime TEST_HORA = LocalDateTime.now().plusMinutes(5);

			paymentRequest.setTerminal(terminal.getValor());
			paymentRequest.setFecha(TEST_HORA.toString()); // ! hay que quitar esta vaina, es un parche
			paymentRequest.setHora(TEST_HORA.toString()); // ! hay que quitar esta vaina, es un parche
			paymentRequest.setCod_cliente(identifier);
			paymentRequest.setImporte(importe);

			PaymentResponseDTO payment = this.providerService.setPayment(paymentRequest);

			// Buscamos y Actualizamos el e_cod_respuesta que hara referencia a el CAMP_ALT1
			MessageAditionalDataDTO[] aditionalsDataWithTRX = Arrays.stream(aditionalsData.getDatoAdicional())
					.map(item -> {
						if (item.getCodigo().equals("e_cod_respuesta")) {
							item.setValor(payment.getCod_trx());
						}
						return item;
					})
					.toArray(MessageAditionalDataDTO[]::new);

			if (payment.getCod_respuesta().equals(ProviderErrorCode.TRANSACCION_ACEPTADA.getcode())) {
				// Re asignamos los datos adicionales con el nuevo ar
				aditionalsData.setDatoAdicional(aditionalsDataWithTRX);
				// Mesaje Salida Pago
				messageOutputPaymentDTO.setMensajeSistema("PAGO EJECUTADA");
				messageOutputPaymentDTO.setMontoTotal(messageInputProcess.getValorPago());
				messageOutputPaymentDTO.setMensajeUsuario(payment.getMsg_respuesta());
				messageOutputPaymentDTO.setFechaDebito(TEST_HORA.toString());	// ! hay que quitar esta vaina, es un parche
				messageOutputPaymentDTO.setFechaPago(TEST_HORA.toString());		// ! hay que quitar esta vaina, es un parche
				messageOutputPaymentDTO.setCodigoError(payment.getCod_respuesta());
				messageOutputPaymentDTO.setBanderaOffline(false);
				messageOutputPaymentDTO.setDatosAdicionales(aditionalsData);
				messageOutputPaymentDTO.setReferencia(identifier);
			}

			// Mensaje de salida proceso;
			messageOutputProcessDTO.setEstado(MessageStatus.OK);
			messageOutputProcessDTO.setCodigo(payment.getCod_respuesta());
			messageOutputProcessDTO.setMensajeUsuario(payment.getMsg_respuesta());
			messageOutputProcessDTO.setMensajeSalidaEjecutarPago(messageOutputPaymentDTO);
			log.info("📥 FINALIZANDO PROCESO DE PAGO");

			jmsService.sendResponseMessage(
					MqConfig.CHANNEL_RESPONSE,
					messageOutputProcessDTO,
					correlationId);

		} catch (Exception e) {
			log.error("❌ ERROR AL GENERAR PAGO: {}", e.getMessage(), e);
			MessageOutputProcessDTO messageOutputProcessDTO = new MessageOutputProcessDTO();
			MessageOutputPaymentDTO messageOutputConsultDTO = new MessageOutputPaymentDTO();

			
			messageOutputProcessDTO.setMensajeUsuario("PAGO NO EJECUTADA");
			messageOutputProcessDTO.setEstado(MessageStatus.ERROR);
			messageOutputConsultDTO.setMensajeUsuario(e.getMessage());
			messageOutputConsultDTO.setCodigoError("300");
			messageOutputProcessDTO.setCodigo("300");

			messageOutputProcessDTO.setMensajeSalidaEjecutarPago(messageOutputConsultDTO);

			jmsService.sendResponseMessage(MqConfig.CHANNEL_RESPONSE, messageOutputProcessDTO, correlationId);
		}
	}

	private void revert(MessageInputRevertPaymentDTO messageInputProcess, String correlationId)
			throws JsonProcessingException {
		try {
			log.info("📤 INICIANDO PROCESO DE REVERSO");

			MessageOutputProcessDTO messageOutputProcessDTO = new MessageOutputProcessDTO();
			MessageOutputRevertPaymentDTO messageOutputRevertPaymentDTO = new MessageOutputRevertPaymentDTO();

			String identifier = messageInputProcess
					.getServicio()
					.getIdentificador();

			MessageProcessAditionalDataDTO aditionalsData = messageInputProcess
					.getServicio()
					.getDatosAdicionales();

			String terminal = Arrays.stream(aditionalsData.getDatoAdicional())
					.filter(item -> item.getCodigo().equals("e_term"))
					.findFirst()
					.orElse(null)
					.getValor();

			Integer importe = BigDecimal.valueOf(messageInputProcess.getValorPago())
					.setScale(2, RoundingMode.HALF_UP)
					.movePointRight(2)
					.intValue();

			String trxCode = Arrays.stream(aditionalsData.getDatoAdicional())
					.filter(item -> item.getCodigo().equals("e_cod_respuesta"))
					.findFirst()
					.orElse(null)
					.getValor();

			RevertRequestDTO revertRequest = new RevertRequestDTO();

			revertRequest.setImporte(importe);
			revertRequest.setCod_cliente(identifier);
			revertRequest.setTerminal(terminal);
			revertRequest.setCod_trx(trxCode);
			revertRequest.setFecha(messageInputProcess.getFechaPago());
			revertRequest.setHora(messageInputProcess.getFechaPago());

			RevertResponseDTO revertPayment = this.providerService.setRevert(revertRequest);

			// ! REPORTAR A EL PROVEEDOR DE QUE EN CONSULTA Y PAGO ESTO ES UN STRING XD
			if (revertPayment.getCod_respuesta()
					.equals(Integer.parseInt(ProviderErrorCode.TRANSACCION_ACEPTADA.getcode()))) {
				// Mesaje Salida Reversos
				messageOutputRevertPaymentDTO.setMensajeSistema("REVERSO EJECUTADA");
				messageOutputRevertPaymentDTO.setMensajeUsuario(revertPayment.getMsg_respuesta());
				messageOutputRevertPaymentDTO.setFechaDebito(messageInputProcess.getFechaPago());
				messageOutputRevertPaymentDTO.setMontoTotal(messageInputProcess.getValorPago());
				messageOutputRevertPaymentDTO.setFechaPago(messageInputProcess.getFechaPago());
				messageOutputRevertPaymentDTO.setBanderaOffline(false);
				messageOutputRevertPaymentDTO.setDatosAdicionales(aditionalsData);
				// ! REPORTAR A EL PROVEEDOR DE QUE EN CONSULTA Y PAGO ESTO ES UN STRING XD
				messageOutputRevertPaymentDTO.setCodigoError(revertPayment.getCod_respuesta().toString());
				messageOutputRevertPaymentDTO.setReferencia(identifier);
			}

			// Mensaje de salida proceso;
			messageOutputProcessDTO.setEstado(MessageStatus.OK);
			// ! REPORTAR A EL PROVEEDOR DE QUE EN CONSULTA Y PAGO ESTO ES UN STRING XD
			messageOutputProcessDTO.setCodigo(revertPayment.getCod_respuesta().toString());
			messageOutputProcessDTO.setMensajeUsuario(revertPayment.getMsg_respuesta());
			messageOutputProcessDTO.setMensajeSalidaEjecutarPago(messageOutputRevertPaymentDTO);

			log.info("📥 FINALIZANDO PROCESO DE REVERSO");

			jmsService.sendResponseMessage(
					MqConfig.CHANNEL_RESPONSE,
					messageOutputProcessDTO,
					correlationId);

		} catch (Exception e) {
			log.error("❌ ERROR AL GENERAR REVERSO: {}", e.getMessage(), e);
			MessageOutputProcessDTO messageOutputProcessDTO = new MessageOutputProcessDTO();
			MessageOutputPaymentDTO messageOutputConsultDTO = new MessageOutputPaymentDTO();

			messageOutputConsultDTO.setCodigoError("300");
			messageOutputConsultDTO.setMensajeUsuario(e.getMessage());

			messageOutputProcessDTO.setEstado(MessageStatus.ERROR);
			messageOutputProcessDTO.setCodigo("0");
			messageOutputProcessDTO.setMensajeUsuario("REVERSO NO EJECUTADA");

			messageOutputProcessDTO.setMensajeSalidaEjecutarPago(messageOutputConsultDTO);

			jmsService.sendResponseMessage(MqConfig.CHANNEL_RESPONSE, messageOutputProcessDTO, correlationId);
		}
	}
}
