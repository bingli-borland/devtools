//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.10.11 at 01:44:21 ���� CST 
//


package com.tongtech.sprint;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.tongtech.sprint package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Exception_QNAME = new QName("", "exception");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.tongtech.sprint
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Worklogdetail }
     * 
     */
    public Worklogdetail createWorklogdetail() {
        return new Worklogdetail();
    }

    /**
     * Create an instance of {@link Sprintdetail }
     * 
     */
    public Sprintdetail createSprintdetail() {
        return new Sprintdetail();
    }

    /**
     * Create an instance of {@link Sprints }
     * 
     */
    public Sprints createSprints() {
        return new Sprints();
    }

    /**
     * Create an instance of {@link Taskdetail }
     * 
     */
    public Taskdetail createTaskdetail() {
        return new Taskdetail();
    }

    /**
     * Create an instance of {@link Sprintdetails }
     * 
     */
    public Sprintdetails createSprintdetails() {
        return new Sprintdetails();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "exception")
    public JAXBElement<String> createException(String value) {
        return new JAXBElement<String>(_Exception_QNAME, String.class, null, value);
    }

}
