//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.07.04 at 06:19:08 ���� CST 
//


package com.tongtech.project;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="finishedstroycount" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="finishedstroypoint" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="remainstroycount" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="remainstroypoint" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="unknownstroycount" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "storystat")
public class Storystat {

    @XmlAttribute
    protected String name;
    @XmlAttribute
    protected Double finishedstroycount;
    @XmlAttribute
    protected Double finishedstroypoint;
    @XmlAttribute
    protected Double remainstroycount;
    @XmlAttribute
    protected Double remainstroypoint;
    @XmlAttribute
    protected Double unknownstroycount;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the finishedstroycount property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getFinishedstroycount() {
        return finishedstroycount;
    }

    /**
     * Sets the value of the finishedstroycount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setFinishedstroycount(Double value) {
        this.finishedstroycount = value;
    }

    /**
     * Gets the value of the finishedstroypoint property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getFinishedstroypoint() {
        return finishedstroypoint;
    }

    /**
     * Sets the value of the finishedstroypoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setFinishedstroypoint(Double value) {
        this.finishedstroypoint = value;
    }

    /**
     * Gets the value of the remainstroycount property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getRemainstroycount() {
        return remainstroycount;
    }

    /**
     * Sets the value of the remainstroycount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setRemainstroycount(Double value) {
        this.remainstroycount = value;
    }

    /**
     * Gets the value of the remainstroypoint property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getRemainstroypoint() {
        return remainstroypoint;
    }

    /**
     * Sets the value of the remainstroypoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setRemainstroypoint(Double value) {
        this.remainstroypoint = value;
    }

    /**
     * Gets the value of the unknownstroycount property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getUnknownstroycount() {
        return unknownstroycount;
    }

    /**
     * Sets the value of the unknownstroycount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setUnknownstroycount(Double value) {
        this.unknownstroycount = value;
    }

}
