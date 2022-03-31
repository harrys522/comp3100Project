
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class dsclient {
    public static void main(String[] args) {
        try {
            // Socket Setup
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

            // Store the first job in a Job object
            String first = in.readLine();
            Job firstJob = Job.fromJOBN(first);

            // Find servers for LRR
            send(out,"GETS All");
            String GETInfo = in.readLine();
            String[] getSplit = GETInfo.split(" ");
            int serverCount = Integer.getInteger(getSplit[1]);
            int mostCores = 0;
            System.out.println(getSplit[1] + " " + getSplit[2]);

            Server[] servers = new Server[serverCount];
            ArrayList<Server> lrrServers = new ArrayList<Server>();
            int j = 0;

            for (int i = 0; i < servers.length; i++) {
                String line = in.readLine();
                servers[i] = Server.fromString(line);

                if (servers[i].cores > mostCores) {
                    mostCores = servers[i].cores;
                    lrrServers.add(servers[i]);
                    System.out.println(servers[i].cores);
                }
                //Select first server-type only
            }

            // Do first job
            send(out, "SCHD" + firstJob.id + " " + firstJob.type + " " + lrrServers.get(0).id);
            int serverId = 1; // Starts at 1 since 0 was used for firstjob.

            while (true) {
                var rcvd = in.readLine();
                System.out.println("rcvd" + rcvd);

                // Break condition
                if (rcvd.equals("NONE")) {
                    send(out, "QUIT");
                    break;
                    //out.write(("QUIT\n").getBytes());
                }

                // Job Scheduling
                /*
                if (rcvd.startsWith("JOBN")) {
                    String line = rcvd;
                    Job currentJob = Job.fromString(line);
                    send(out,"SCHD " + currentJob.id + " " + currentJob.type + " " + serverId);
                    out.write(("SCHD " + currentJob.id + " " + currentJob.type + " " + serverId + "\n").getBytes());

                    // Servers rotate through the largest servers and reset to 0 at the end.
                    serverId++;
                    if (serverId > lrrServers.size()) {
                        serverId = 0;
                    }
                }
                */
            }

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
