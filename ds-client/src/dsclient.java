import java.io.*;
import java.net.*;
import java.nio.Buffer;
import java.util.ArrayList;

public class dsclient {
    public static void main(String[] args) {
        try {
            int port = 50000;
            String address = "localhost";
            String algorithm = "lrr";
            Socket s = new Socket(address, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            // Handle algorithm argument
            for(int i=0;i< args.length;i++){
                if(args[i].equals("-a")){
                    algorithm = args[i+1];
                    System.out.println("USING ALGORITHM:" + algorithm);
                }
            }

            // ds-server handshake
            send(out, "HELO");
            String auth = receive(in, "OK");
            String username = System.getProperty("user.name");

            System.out.println("AUTH " + username + " " + auth);
            send(out, "AUTH " + username);

            String ready = receive(in, "OK");
            System.out.println("REDY");
            send(out, "REDY");

            // Receive the first job
            var rcvd = receive(in, "JOBN");
            Job first = Job.fromJOBN(rcvd);

            var Servers = new ArrayList<Server>();

            // Apply scheduling algorithm
            if(algorithm.equals("lrr")){

                Servers = LRR.getServers(out, in, first);
                LRR.schedule(Servers, in, out);

            } else if(algorithm.equals("fc")) {

                Servers = FF.getServers(out, in, first);
                FF.schedule(Servers, in, out);

            } else {
                System.out.println("Algorithm not found:" + algorithm);
            }

            send(out, "QUIT");
            //Close connection
            out.flush();
            out.close();
            s.close();
        } catch (ConnectException e) {
            System.out.println("Connection Refused");
        } catch (Exception IOException) {
            System.out.println("IO Exception");
        }
    }

    public static void send(DataOutputStream out, String cmd) {
        try {
            out.write((cmd + "\n").getBytes());
            out.flush();
        } catch (Exception IOException) {
            System.out.println("IO Exception (send)");
        }
    }

    public static String receive(BufferedReader in, String contains) {
        try {
            String rcvd = in.readLine();
            if (rcvd.contains(contains)) {
                System.out.println("S:" + rcvd);
                return rcvd;
            } else {
                System.out.println("Did not receive expected message: " + rcvd);
                return rcvd;
            }
        } catch (Exception IOException) {
            System.out.println("IO Exception (rcv)");
        }
        return "Error receiving message"; // In theory this should never run.
    }
}
