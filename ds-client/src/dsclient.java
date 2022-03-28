
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class dsclient {
    public static void main(String[] args) {
        try {
            // Socket Setup
            Socket s = new Socket("localhost", 50000);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());

            // ds-server handshake
            dout.write(("HELO\n").getBytes());
            System.out.println("Client:HELO");
            String authcheck= in.readLine();
            System.out.println("Server:" + authcheck);
            String username = System.getProperty("user.name");

            // Attempt to authorise and tell the server when ready.
            if (authcheck.equals("OK")) {
                dout.write(("AUTH"+ username +"\n").getBytes());
            } else {
                System.out.println("No server response");
                return;
            }
            String readycheck = in.readLine();
            if(readycheck.equals("OK")) {
                System.out.println("Authorised:"+username);
                dout.write(("REDY\n").getBytes());
            }

            //
            String first = in.readLine();
            if(first.startsWith("JOBN")){
                System.out.println("Server:"+first);

            }

            // Start receiving jobs to schedule
            while(true) {
                // Get currently running servers
                dout.write(("GETS All").getBytes());
                String GETInfo = in.readLine();
                if(GETInfo.contains("DATA")){
                    dout.write(("OK").getBytes());
                    var GETparts = GETInfo.split(" ");
                    int size = Integer.getInteger(GETparts[1]);
                    Server[] servers = new Server[size];
                    for(int i=0; i< servers.length;i++){
                        String line = in.readLine();
                        servers[i] = Server.fromString(line);
                    }
                }

                var rcvd = in.readLine();
                if(rcvd.equals("QUIT")){
                    break;
                }
                if(rcvd.startsWith("JOBN")){
                    String line = rcvd;
                    Job currentJob = Job.fromString(line);
                }
                //
            }

            dout.flush();
            dout.close();
            s.close();
        } catch (Exception IOException) {
            System.out.println("IO Exception");
        }
    }
}
