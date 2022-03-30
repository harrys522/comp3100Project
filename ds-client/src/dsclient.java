
import java.io.*;
import java.net.*;
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
            String authcheck = in.readLine();
            String username = System.getProperty("user.name");

            // Attempt to authorise and tell the server when ready.
            if (authcheck.equals("OK")) {
                dout.write(("AUTH" + username + "\n").getBytes());
            } else {
                System.out.println("No server response");
                return;
            }
            String readycheck = in.readLine();
            if (readycheck.equals("OK")) {
                System.out.println("Authorised:" + username);
                dout.write(("REDY\n").getBytes());
            }

            // Store the first job in a Job object
            String first = in.readLine();
            System.out.println("REDY Response:" + first);
            Job firstJob = Job.fromString(first);

            // Start receiving jobs to schedule
            // Get currently running servers
            dout.write(("GETS All\n").getBytes());
            String GETInfo = in.readLine();
            dout.write(("OK\n").getBytes());

            String[] getSplit = GETInfo.split(" ");
            int serverCount = Integer.getInteger(getSplit[1]);
            int mostCores = 0;

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
            }

            // Do first job
            dout.write(("SCHD " + firstJob.id + " " + firstJob.type + " " + lrrServers.get(0).id).getBytes());
            int serverId = 1; // Starts at 1 since 0 was used for firstjob.

            while (true) {
                var rcvd = in.readLine();

                // Break condition to send QUIT
                if (rcvd.equals("NONE")) {
                    dout.write(("QUIT\n").getBytes());
                    String response = in.readLine();
                    if (response.equals("QUIT")) {
                        break;
                    } else {
                        System.out.println("ERROR quitting - missing server response.");
                    }
                }

                // Job Scheduling
                if (rcvd.startsWith("JOBN")) {
                    String line = rcvd;
                    Job currentJob = Job.fromString(line);
                    dout.write(("SCHD " + currentJob.id + " " + currentJob.type + " " + serverId + "\n").getBytes());
                    serverId++;
                    if (serverId > lrrServers.size()) {
                        serverId = 0;
                    }
                }
            }
            dout.flush();
            dout.close();
            s.close();
        } catch (ConnectException e) {
            System.out.println("Connection Refused");
        } catch (Exception IOException) {
            System.out.println("IO Exception");
        }

    }
}
