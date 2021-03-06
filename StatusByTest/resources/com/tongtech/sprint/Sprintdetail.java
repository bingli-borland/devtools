//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.10.11 at 01:44:21 ���� CST 
//


package com.tongtech.sprint;

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
 *         &lt;element ref="{}taskdetail" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}worklogdetail" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="issuetype" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="issuekey" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="issuesummary" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="issuestatus" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="issuepoint" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="issuetimespent" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="issuelogtimespent" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="issuelogworkcount" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "taskdetail",
    "worklogdetail"
})
@XmlRootElement(name = "sprintdetail")
public class Sprintdetail {

    protected List<Taskdetail> taskdetail;
    protected List<Worklogdetail> worklogdetail;
    @XmlAttribute
    protected String name;
    @XmlAttribute
    protected String issuetype;
    @XmlAttribute
    protected String issuekey;
    @XmlAttribute
    protected String issuesummary;
    @XmlAttribute
    protected String issuestatus;
    @XmlAttribute
    protected String issuepoint;
    @XmlAttribute
    protected Double issuetimespent;
    @XmlAttribute
    protected Double issuelogtimespent;
    @XmlAttribute
    protected Integer issuelogworkcount;

    /**
     * Gets the value of the taskdetail property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the taskdetail property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTaskdetail().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Taskdetail }
     * 
     * 
     */
    public List<Taskdetail> getTaskdetail() {
        if (taskdetail == null) {
            taskdetail = new ArrayList<Taskdetail>();
        }
        return this.taskdetail;
    }

    /**
     * Gets the value of the worklogdetail property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the worklogdetail property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWorklogdetail().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Worklogdetail }
     * 
     * 
     */
    public List<Worklogdetail> getWorklogdetail() {
        if (worklogdetail == null) {
            worklogdetail = new ArrayList<Worklogdetail>();
        }
        return this.worklogdetail;
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
     * Gets the value of the issuetype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIssuetype() {
        return issuetype;
    }

    /**
     * Sets the value of the issuetype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIssuetype(String value) {
        this.issuetype = value;
    }

    /**
     * Gets the value of the issuekey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIssuekey() {
        return issuekey;
    }

    /**
     * Sets the value of the issuekey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIssuekey(String value) {
        this.issuekey = value;
    }

    /**
     * Gets the value of the issuesummary property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIssuesummary() {
        return issuesummary;
    }

    /**
     * Sets the value of the issuesummary property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIssuesummary(String value) {
        this.issuesummary = value;
    }

    /**
     * Gets the value of the issuestatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIssuestatus() {
        return issuestatus;
    }

    /**
     * Sets the value of the issuestatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIssuestatus(String value) {
        this.issuestatus = value;
    }

    /**
     * Gets the value of the issuepoint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIssuepoint() {
        return issuepoint;
    }

    /**
     * Sets the value of the issuepoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIssuepoint(String value) {
        this.issuepoint = value;
    }

    /**
     * Gets the value of the issuetimespent property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getIssuetimespent() {
        return issuetimespent;
    }

    /**
     * Sets the value of the issuetimespent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setIssuetimespent(Double value) {
        this.issuetimespent = value;
    }

    /**
     * Gets the value of the issuelogtimespent property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getIssuelogtimespent() {
        return issuelogtimespent;
    }

    /**
     * Sets the value of the issuelogtimespent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setIssuelogtimespent(Double value) {
        this.issuelogtimespent = value;
    }

    /**
     * Gets the value of the issuelogworkcount property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getIssuelogworkcount() {
        return issuelogworkcount;
    }

    /**
     * Sets the value of the issuelogworkcount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setIssuelogworkcount(Integer value) {
        this.issuelogworkcount = value;
    }

}
