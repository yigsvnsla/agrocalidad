
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
 *         <element name="BillInquiryResponse" type="{http://tempuri.org/}BillInquiryResponse" minOccurs="0"/>
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
    "billInquiryResponse"
})
@XmlRootElement(name = "BillInquiryRs")
public class BillInquiryRs {

    @XmlElement(name = "BillInquiryResponse")
    protected BillInquiryResponse billInquiryResponse;

    /**
     * Obtiene el valor de la propiedad billInquiryResponse.
     * 
     * @return
     *     possible object is
     *     {@link BillInquiryResponse }
     *     
     */
    public BillInquiryResponse getBillInquiryResponse() {
        return billInquiryResponse;
    }

    /**
     * Define el valor de la propiedad billInquiryResponse.
     * 
     * @param value
     *     allowed object is
     *     {@link BillInquiryResponse }
     *     
     */
    public void setBillInquiryResponse(BillInquiryResponse value) {
        this.billInquiryResponse = value;
    }

}
