//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.08.09 at 05:41:10 ���� CST 
//


package generated;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the generated package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Projects }
     * 
     */
    public Projects createProjects() {
        return new Projects();
    }

    /**
     * Create an instance of {@link Peoplestats }
     * 
     */
    public Peoplestats createPeoplestats() {
        return new Peoplestats();
    }

    /**
     * Create an instance of {@link Projectstory }
     * 
     */
    public Projectstory createProjectstory() {
        return new Projectstory();
    }

    /**
     * Create an instance of {@link Jiraissue }
     * 
     */
    public Jiraissue createJiraissue() {
        return new Jiraissue();
    }

    /**
     * Create an instance of {@link Peoplestat }
     * 
     */
    public Peoplestat createPeoplestat() {
        return new Peoplestat();
    }

    /**
     * Create an instance of {@link Project }
     * 
     */
    public Project createProject() {
        return new Project();
    }

    /**
     * Create an instance of {@link Storystats }
     * 
     */
    public Storystats createStorystats() {
        return new Storystats();
    }

    /**
     * Create an instance of {@link Storystat }
     * 
     */
    public Storystat createStorystat() {
        return new Storystat();
    }

    /**
     * Create an instance of {@link Projectsubtask }
     * 
     */
    public Projectsubtask createProjectsubtask() {
        return new Projectsubtask();
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