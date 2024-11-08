
package com.bolivariano.microservice.agrocalidad.wsdl;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para anonymous complex type.</p>
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.</p>
 * 
 * <pre>{@code
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="BillPaymentReversalResponse" type="{http://tempuri.org/}BillPaymentReversalResponse" minOccurs="0"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "billPaymentReversalResponse"
})
@XmlRootElement(name = "BillPaymentReversalRs")
public class BillPaymentReversalRs {

    @XmlElement(name = "BillPaymentReversalResponse")
    protected BillPaymentReversalResponse billPaymentReversalResponse;

    /**
     * Obtiene el valor de la propiedad billPaymentReversalResponse.
     * 
     * @return
     *     possible object is
     *     {@link BillPaymentReversalResponse }
     *     
     */
    public BillPaymentReversalResponse getBillPaymentReversalResponse() {
        return billPaymentReversalResponse;
    }

    /**
     * Define el valor de la propiedad billPaymentReversalResponse.
     * 
     * @param value
     *     allowed object is
     *     {@link BillPaymentReversalResponse }
     *     
     */
    public void setBillPaymentReversalResponse(BillPaymentReversalResponse value) {
        this.billPaymentReversalResponse = value;
    }

}
