//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.07.04 at 06:19:08 ���� CST 
//


package com.tongtech.project;

import java.util.ArrayList;
import java.util.List;
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
 *       &lt;sequence>
 *         &lt;element ref="{}exception" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="timespent" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="logtimespent" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="logworkcount" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="earnedvalue" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="memo" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "exception"
})
@XmlRootElement(name = "peoplestat")
public class Peoplestat {

    protected List<String> exception;
    @XmlAttribute
    protected String name;
    @XmlAttribute
    protected Double timespent;
    @XmlAttribute
    protected Double logtimespent;
    @XmlAttribute
    protected Integer logworkcount;
    @XmlAttribute
    protected Double earnedvalue;
    @XmlAttribute
    protected String memo;

    /**
     * Gets the value of the exception property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the exception property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getException().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getException() {
        if (exception == null) {
            exception = new ArrayList<String>();
        }
        return this.exception;
    }

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
     * Gets the value of the timespent property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getTimespent() {
        return timespent;
    }

    /**
     * Sets the value of the timespent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setTimespent(Double value) {
        this.timespent = value;
    }

    /**
     * Gets the value of the logtimespent property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getLogtimespent() {
        return logtimespent;
    }

    /**
     * Sets the value of the logtimespent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setLogtimespent(Double value) {
        this.logtimespent = value;
    }

    /**
     * Gets the value of the logworkcount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getLogworkcount() {
        return logworkcount;
    }

    /**
     * Sets the value of the logworkcount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setLogworkcount(Integer value) {
        this.logworkcount = value;
    }

    /**
     * Gets the value of the earnedvalue property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getEarnedvalue() {
        return earnedvalue;
    }

    /**
     * Sets the value of the earnedvalue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setEarnedvalue(Double value) {
        this.earnedvalue = value;
    }

    /**
     * Gets the value of the memo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMemo() {
        return memo;
    }

    /**
     * Sets the value of the memo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMemo(String value) {
        this.memo = value;
    }

}