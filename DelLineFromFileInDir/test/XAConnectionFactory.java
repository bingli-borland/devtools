/*
 *
 *
 * 
 * 
 * 
 */

/*
 * @(#)XAConnectionFactory.java	1.5 06/28/07
 */ 

package com.tongtech.tmqi;

import javax.jms.*;
import com.tongtech.tmqi.jmsclient.XAConnectionImpl;
import com.tongtech.tmqi.jmsclient.XAQueueConnectionImpl;
import com.tongtech.tmqi.jmsclient.XATopicConnectionImpl;

/**
 * An <code>XAConnectionFactory</code> is used to create XAConnections with
 * the Sun MQ Java Message Service (JMS) provider.
 *
 * @see         javax.jms.XAConnectionFactory javax.jms.XAConnectionFactory
 */
public class XAConnectionFactory extends com.tongtech.tmqi.ConnectionFactory implements javax.jms.XAConnectionFactory {

    /**
     * Create an XA connection with default user identity.
     * The connection is created in stopped mode. No messages
     * will be delivered until <code>Connection.start</code> method
     * is explicitly called.
     *   
     * @return a newly created XA connection.
     *   
     * @exception JMSException if JMS Provider fails to create XA Connection
     *                         due to some internal error.
     * @exception JMSSecurityException  if client authentication fails due to
     *                         invalid user name or password.
     */  

    public XAConnection createXAConnection() throws JMSException {
        return createXAConnection(getProperty(ConnectionConfiguration.tmqiDefaultUsername),
                                  getProperty(ConnectionConfiguration.tmqiDefaultPassword));
    }

    /**
     * Create an XA connection with specified user identity.
     * The connection is created in stopped mode. No messages
     * will be delivered until <code>Connection.start</code> method
     * is explicitly called.
     *   
     * @param username the caller's user name
     * @param password the caller's password
     *   
     * @return a newly created XA connection.
     *   
     * @exception JMSException if JMS Provider fails to create XA connection
     *                         due to some internal error.
     * @exception JMSSecurityException  if client authentication fails due to
     *                         invalid user name or password.
     */
 
    public XAConnection createXAConnection(String username, String password) throws JMSException {
        return new XAConnectionImpl(getCurrentConfiguration(), username, password, getConnectionType());
    }
 
    /**
     * Create an XA queue connection with default user identity.
     * The connection is created in stopped mode. No messages
     * will be delivered until <code>Connection.start</code> method
     * is explicitly called.
     *  
     * @return a newly created XA queue connection.
     *  
     * @exception JMSException if JMS Provider fails to create XA queue Connection
     *                         due to some internal error.
     * @exception JMSSecurityException  if client authentication fails due to
     *                         invalid user name or password.
     */

    public XAQueueConnection createXAQueueConnection() throws JMSException {
        return createXAQueueConnection(getProperty(ConnectionConfiguration.tmqiDefaultUsername),
                                        getProperty(ConnectionConfiguration.tmqiDefaultPassword));
    }

    /**
     * Create an XA queue connection with specific user identity.
     * The connection is created in stopped mode. No messages
     * will be delivered until <code>Connection.start</code> method
     * is explicitly called.
     *  
     * @param username the caller's user name
     * @param password the caller's password
     *
     * @return a newly created XA queue connection.
     *
     * @exception JMSException if JMS Provider fails to create XA queue Connection
     *                         due to some internal error.
     * @exception JMSSecurityException  if client authentication fails due to
     *                         invalid user name or password.
     */
 
    public XAQueueConnection createXAQueueConnection(String username,
                                                     String password) throws JMSException {
        return new XAQueueConnectionImpl(getCurrentConfiguration(), username, password, getConnectionType());
    }

    /**
     * Create an XA topic connection with default user identity.
     * The connection is created in stopped mode. No messages
     * will be delivered until <code>Connection.start</code> method
     * is explicitly called.
     *   
     * @return a newly created XA topic connection.
     *   
     * @exception JMSException if JMS Provider fails to create XA topic Connection
     *                         due to some internal error.
     * @exception JMSSecurityException  if client authentication fails due to
     *                         invalid user name or password.
     */  

    public XATopicConnection createXATopicConnection() throws JMSException {
        return createXATopicConnection(getProperty(ConnectionConfiguration.tmqiDefaultUsername),
                                        getProperty(ConnectionConfiguration.tmqiDefaultPassword));
    }

    /**
     * Create an XA topic connection with specified user identity.
     * The connection is created in stopped mode. No messages
     * will be delivered until <code>Connection.start</code> method
     * is explicitly called.
     *   
     * @param username the caller's user name
     * @param password the caller's password
     *   
     * @return a newly created XA topic connection.
     *   
     * @exception JMSException if JMS Provider fails to create XA topi connection
     *                         due to some internal error.
     * @exception JMSSecurityException  if client authentication fails due to
     *                         invalid user name or password.
     */
 
    public XATopicConnection createXATopicConnection(String username, String password) throws JMSException {
        return new XATopicConnectionImpl(getCurrentConfiguration(), username, password, getConnectionType());
    }

}
