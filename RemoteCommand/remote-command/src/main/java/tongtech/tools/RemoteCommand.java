package tongtech.tools;

import org.jeromq.ZMQ;
import org.jeromq.ZMQ.Context;
import org.jeromq.ZMQ.Socket;

import java.io.IOException;

/**
* Hello World client
* Connects REQ socket to tcp://localhost:5559
* Sends "Hello" to server, expects "World" back
*
* Christophe Huntzinger <chuntzin_at_wanadoo.fr>
*/
public class RemoteCommand {
    String type = "";
    String destination = "";
    String command = "";

    public void commandServer () {
        //  Prepare our context and socket
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.REP);

        socket.bind(destination);

        while (true) {
            String result = "RESPONSE:";
            // Wait for next request from client
            String commandClient = socket.recvStr(0);
            System.out.println("Received " + commandClient);
            if (commandClient.equals("END...")) {
                break;
            }
            try {
                Process process = Runtime.getRuntime().exec(commandClient);
                //result += process.exitValue();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            socket.send(result);
        }

        socket.close();
        context.term();
    }

    public void commandClient () {
        Context context = ZMQ.context(1);

        //  Socket to talk to server
        Socket requester = context.socket(ZMQ.REQ);
        requester.connect(destination);

        System.out.println("launch and connect client.");
        requester.send(command);
        String reply = requester.recvStr(0);
        System.out.println("Request " + command + " ["+reply+"]");

        //  We never get here but clean up anyhow
        requester.close();
        context.term();
    }

    public static void main (String[] args) {
        String clientUsage =  "useage: tongtech.tools.RemoteCommand client dest(ex:tcp://1.1.1.1:12345) command";
        String serverUsage = "useage: tongtech.tools.RemoteCommand server dest(ex:tcp://*.12345)";
        if (args.length < 2) {
            System.out.println(clientUsage);
            System.out.println(serverUsage);
            return;
        }
        RemoteCommand remoteCommand = new RemoteCommand();
        remoteCommand.type = args[0];
        if (remoteCommand.type.equals("client") && args.length < 3) {
            System.out.println(clientUsage);
        }
        if (remoteCommand.type.equals("client"))  {
            remoteCommand.destination = args[1];
            remoteCommand.command = args[2];
            remoteCommand.commandClient();
        } else if (remoteCommand.type.equals("server")) {
            remoteCommand.destination = args[1];
            remoteCommand.commandServer();
        }
    }
}