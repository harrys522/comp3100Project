
import java.io.*;
import java.net.*;
import java.nio.Buffer;
import java.util.ArrayList;

public class dsclient {
    public static void main(String[] args) {
        try {
            int port = 50000;
            String address = "localhost";
            Socket s = new Socket(address, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            DataOutputStream out = new DataOutputStream(s.getOutputStream());

            // ds-server handshake
            send(out, "HELO");
            String auth = receive(in, "OK");
            String username = System.getProperty("user.name");
            System.out.println("AUTH " + username + " " + auth);
            send(out, "AUTH" + username);
            String ready = receive(in, "OK");
            System.out.println("REDY");
            send(out, "REDY");

            // Store the first job
            var rcvd = receive(in, "JOBN");
            Job first = Job.fromJOBN(rcvd);

            ArrayList<Server> usedServers = new ArrayList<Server>();
            usedServers = getLrrServers(out, in, first);
            System.out.println("Got LRR Servers of type: " + usedServers.get(0).type);

            var currentServer = 0;
            // Job Scheduling loop
            while (!rcvd.equals("NONE")) {
                System.out.println("rcvd:" + rcvd);

                if (rcvd.startsWith("JOBN")) {
                    Job currentJob = Job.fromJOBN(rcvd);
                    var scheduleCmd = currentJob.id + " " + currentJob.type + " " + currentServer;
                    send(out, "SCHD " + scheduleCmd);
                    //out.write(("SCHD " + currentJob.id + " " + currentJob.type + " " + currentServer + "\n").getBytes());
                    currentServer++;
                    if (currentServer > usedServers.size()) {
                        currentServer = 0;
                    }
                }
                // OTHER COMMANDS ie JCPL


                rcvd = in.readLine();
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

    public static ArrayList<Server> getLrrServers(DataOutputStream out, BufferedReader in, Job first) {
        ArrayList<Server> lrrServers = new ArrayList<Server>();
        try {
            var lrrtype = "";
            int mostCores = 0;

            String getCmd = "GETS Capable " + first.cores + " " + first.memory + " " + first.disk;
            //String getCmd = "GETS All";
            System.out.println(getCmd); // Debugging
            send(out, getCmd);
            String data = receive(in, "DATA");

            String[] split = data.split(" ");
            int serverCount = Integer.getInteger(split[1]);
            Server[] servers = new Server[serverCount];

            String rcvd = receive(in, ""); // Initialise variable and print first server.

            for (int i = 0; i < serverCount; i++) {
                System.out.println(rcvd);
                servers[i] = Server.fromString(rcvd);

                // Store first server's type.
                if (lrrServers.get(0) != null) {
                    lrrtype = lrrServers.get(0).type;
                }
                // Select based on cores and only the first type.
                if (servers[i].cores > mostCores && servers[i].type.equals(lrrtype)) {
                    mostCores = servers[i].cores;
                    lrrServers.add(servers[i]);
                    System.out.println(servers[i].cores);
                }
                rcvd = in.readLine();
            }

            send(out, "OK");
        } catch (Exception IOException) {
            System.out.println("IO Exception (lrr)");
        }
        return lrrServers;
    }

    public static String receive(BufferedReader in, String contains) {
        try {
            String rcvd = in.readLine();
            if (rcvd.contains(contains)) {
                System.out.println(rcvd);
                return rcvd;
            } else {
                while (!rcvd.contains(contains)) {
                    rcvd = in.readLine();
                }
                System.out.println(rcvd);
                return rcvd;
            }
        } catch (Exception IOException) {
            System.out.println("IO Exception (rcv)");
        }
        return "Error recieving message"; // Error handling later
    }
}
