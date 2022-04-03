
import jdk.jfr.SettingDefinition;

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

            var Servers = new ArrayList<Server>();

            Servers = getLrrServers(out, in, first); // This part will change for different algorithm.
            String serverType = Servers.get(0).type;
            System.out.println("Got " + Servers.size() + "x LRR Servers of type: " + Servers.get(0).type);

            var currentId = Servers.get(0).id;
            var lastId = Servers.get(Servers.size() - 1).id;

            send(out, "SCHD" + first.id + " " + serverType + " " + currentId); // Schedule first job from earlier.
            // Scheduling loop
            while (!(rcvd.equals("NONE"))) {
                System.out.println("S:" + rcvd);

                // Schedule a job
                if (rcvd.startsWith("JOBN") || rcvd.startsWith("JOBP")) {
                    Job currentJob = Job.fromJOBN(rcvd);
                    var scheduleCmd = currentJob.id + " " + serverType + " " + currentId;
                    send(out, "SCHD " + scheduleCmd);
                    //out.write(("SCHD " + currentJob.id + " " + currentJob.type + " " + currentServer + "\n").getBytes());
                    currentId++;
                    if (currentId > lastId) {
                        currentId = 0;
                    }
                }
                if (rcvd.equals("OK")) {
                    send(out, "REDY");
                }
                if (rcvd.startsWith("JCPL")) {
                    send(out, "REDY");
                }


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

    public static ArrayList<Server> getLrrServers(DataOutputStream out, BufferedReader in, Job first) {
        ArrayList<Server> lrrServers = new ArrayList<Server>();
        try {
            boolean typefound = false;
            var lrrtype = "";
            int mostCores = 0;

            String getCmd = "GETS Capable " + first.cores + " " + first.memory + " " + first.disk;
            System.out.println(getCmd); // Debugging
            send(out, getCmd);
            String data = receive(in, "DATA");

            String[] split = data.split(" ");
            int serverCount = Integer.parseInt(split[1]);
            Server[] servers = new Server[serverCount];

            send(out, "OK");

            for (int i = 0; i < serverCount; i++) {
                String rcvd = receive(in, "");
                servers[i] = Server.fromString(rcvd);

                // Find most cores
                if (servers[i].cores > mostCores) {
                    mostCores = servers[i].cores;
                }
            }
            for (int i = 0; i < serverCount; i++) {
                // Select based on cores and only the first type.
                if (servers[i].cores == mostCores) {
                    if (typefound == false) {
                        typefound = true;
                        lrrtype = servers[i].type;
                    }
                    if (servers[i].type.equals(lrrtype)) {
                        lrrServers.add(servers[i]);
                    }
                }
            }
            send(out, "OK");
            return lrrServers;
        } catch (Exception IOException) {
            System.out.println("IO Exception (lrr)");
        }
        return lrrServers;
    }
}
