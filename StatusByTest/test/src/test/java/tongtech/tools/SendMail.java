package tongtech.tools;

import javax.mail.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import javax.mail.internet.*;
import org.apache.logging.log4j.*;


/**
 * Created with IntelliJ IDEA.
 * User: liuhf
 * Date: 13-9-3
 * Time: 上午9:11
 * To change this template use File | Settings | File Templates.
 */
class LoggingOutputStream extends OutputStream
{

    protected boolean hasBeenClosed = false;

    protected byte[] buf;

    protected int count;

    private int bufLength;

    public static final int DEFAULT_BUFFER_LENGTH = 4096;

    protected Logger logger;

    protected Level level;

    private LoggingOutputStream()
    {

    }

    public LoggingOutputStream(Logger log, Level level)
            throws IllegalArgumentException {
        if (log == null) {
            throw new IllegalArgumentException("cat == null");
        }
        if (level == null) {
            throw new IllegalArgumentException("priority == null");
        }

        this.level = level;
        logger = log;
        bufLength = DEFAULT_BUFFER_LENGTH;
        buf = new byte[DEFAULT_BUFFER_LENGTH];
        count = 0;
    }



    public void close()
    {
        flush();
        hasBeenClosed = true;
    }



    public void write(final int b) throws IOException
    {
        if (hasBeenClosed) {
            throw new IOException("The stream has been closed.");
        }

        if (count == bufLength)
        {
            final int newBufLength = bufLength + DEFAULT_BUFFER_LENGTH;
            final byte[] newBuf = new byte[newBufLength];

            System.arraycopy(buf, 0, newBuf, 0, bufLength);

            buf = newBuf;
            bufLength = newBufLength;
        }

        buf[count] = (byte) b;
        count++;
    }



    public void flush()
    {
        if (count == 0)
        {
            return;
        }


        if (count == 1 && ((char) buf[0]) == '\n')
        {
            reset();
            return;
        }
        final byte[] theBytes = new byte[buf.length];
        System.arraycopy(buf, 0, theBytes, 0, count);
        logger.log(level, new String(theBytes));
        reset();
    }


    private void reset()
    {
        count = 0;
    }

}

public class SendMail {
    private static Logger logger = LogManager.getLogger(SendMail.class);
    String host="10.10.1.3";
    String user="tong\\cddev";
    String password="launce.214";
    String fromAddress = "cddev@tongtech.com";

    public void setHost(String host)
    {
        this.host=host;
    }
    public void setAccount(String user,String password)
    {
        this.user=user;
        this.password=password;
    }
    public void send(String to,String subject,String content)
    {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);//指定SMTP服务器
        props.put("mail.smtp.auth", "true");//指定是否需要SMTP验证
        try
        {
            Session mailSession = Session.getDefaultInstance(props);
            //mailSession.setDebug(true);//是否在控制台显示debug信息
            //mailSession.setDebugOut(new PrintStream(new  LoggingOutputStream(logger,Level.TRACE), true));
             Message message=new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(fromAddress));//发件人
            message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));//收件人
            message.setSubject(subject);//邮件主题
            //message.setText(content);//邮件内容
            message.setContent(content, "text/html");//; charset=GBK
            message.saveChanges();
            Transport transport = mailSession.getTransport("smtp");
            transport.connect(host, user, password);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public static void main(String args[])
    {
        SendMail sm=new SendMail();
        sm.send("user@yahoo.com.cn","标题","新邮件");
    }
}
