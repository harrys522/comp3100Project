
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class dsclient {
    public static void main(String[] args) {
        try {
            Socket s = new Socket("localhost", 50000);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            // ds-server handshake
            send(out, "HELO");
            String auth = in.readLine();
            String username = System.getProperty("user.name");
            if (auth.equals("OK")) {
                System.out.println("AUTH");
                send(out,"AUTH" + username);
            } else {
                System.out.println("No server response");
                return;
            }
            String ready = in.readLine();
            if (ready.equals("OK")) {
                System.out.println("REDY");
                send(out,"REDY");
            }

            // Store the first job
            var rcvd = in.readLine();

            // Get servers and store the ones being used.
            send(out,"GETS All");
            System.out.println("GETS All");
            String GETInfo = in.readLine();
            String[] getSplit = GETInfo.split(" ");
            int serverCount = Integer.getInteger(getSplit[1]);
            int mostCores = 0;
            System.out.println("DATA" + getSplit[1] + " " + getSplit[2]);
            Server[] servers = new Server[serverCount];
            ArrayList<Server> lrrServers = new ArrayList<Server>();
            send(out,"OK");

            var lrrtype = "";
            // Read GETS All server list output.
            for (int i = 0; i < servers.length; i++) {
                System.out.println("GOT TO LOOP");
                String line = in.readLine();
                servers[i] = Server.fromString(line);

                // Store first server's type.
                if(lrrServers.get(0)!=null){
                    lrrtype = lrrServers.get(0).type;
                }
                // Select based on cores and only the first type.
                if (servers[i].cores > mostCores && servers[i].type.equals(lrrtype)) {
                    mostCores = servers[i].cores;
                    lrrServers.add(servers[i]);
                    System.out.println(servers[i].cores);
                }
            }
            send(out,"OK");

            var currentServer = 0;
            // Job Scheduling loop
            while (!rcvd.equals("NONE")) {
                System.out.println("rcvd:" + rcvd);

                if (rcvd.startsWith("JOBN")) {
                    Job currentJob = Job.fromString(rcvd);
                    send(out,"SCHD " + currentJob.id + " " + currentJob.type + " " + currentServer);
                    out.write(("SCHD " + currentJob.id + " " + currentJob.type + " " + currentServer + "\n").getBytes());
                }

                // OTHER COMMANDS ie JCPL

                currentServer++;
                if(currentServer>lrrServers.size()){
                    currentServer=0;
                }
                rcvd = in.readLine();
            }
            send(out,"QUIT");
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
    public static void send(DataOutputStream out, String cmd){
        try{
            out.write((cmd + "\n").getBytes());
        } catch (Exception IOException) {
            System.out.println("IO Exception");
        }
    }
}
