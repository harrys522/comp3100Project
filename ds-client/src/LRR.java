import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.util.ArrayList;
public class LRR {
    public static ArrayList<Server> getServers(DataOutputStream out, BufferedReader in, Job first) {
        ArrayList<Server> lrrServers = new ArrayList<Server>();
        try {
            boolean typefound = false;
            var lrrtype = "";
            int mostCores = 0;

            String getCmd = "GETS Capable " + first.cores + " " + first.memory + " " + first.disk;
            System.out.println(getCmd); // Debugging
            dsclient.send(out, getCmd);
            String data = dsclient.receive(in, "DATA");

            String[] split = data.split(" ");
            int serverCount = Integer.parseInt(split[1]);
            Server[] servers = new Server[serverCount];

            dsclient.send(out, "OK");

            for (int i = 0; i < serverCount; i++) {
                String rcvd = dsclient.receive(in, "");
                servers[i] = Server.fromString(rcvd);

                // Find most cores
                if (servers[i].cores > mostCores) {
                    mostCores = servers[i].cores;
                }
            }
            for (int i = 0; i < serverCount; i++) {
                // Select based on cores and only the first type.
                if (servers[i].cores == mostCores) {
                    if (!typefound) {
                        typefound = true;
                        lrrtype = servers[i].type;
                    }
                    if (servers[i].type.equals(lrrtype)) {
                        lrrServers.add(servers[i]);
                    }
                }
            }
            dsclient.send(out, "OK");
            return lrrServers;
        } catch (Exception IOException) {
            System.out.println("IO Exception (lrr)");
        }
        return lrrServers;
    }

    public static void schedule(ArrayList<Server> Servers, BufferedReader in, DataOutputStream out) {
        try {
            var currentId = Servers.get(0).id;
            var lastId = Servers.get(Servers.size() - 1).id;
            var rcvd = in.readLine();
            String serverType = Servers.get(0).type;
            // Scheduling loop
            while (!(rcvd.equals("NONE"))) {
                System.out.println("S:" + rcvd);

                // Schedule a job
                if (rcvd.startsWith("JOBN") || rcvd.startsWith("JOBP")) {
                    Job currentJob = Job.fromJOBN(rcvd);
                    var scheduleCmd = currentJob.id + " " + serverType + " " + currentId; //
                    dsclient.send(out, "SCHD " + scheduleCmd);
                    //out.write(("SCHD " + currentJob.id + " " + currentJob.type + " " + currentServer + "\n").getBytes());
                    currentId++;
                    if (currentId > lastId) {
                        currentId = 0;
                    }
                }
                if (rcvd.equals("OK") || rcvd.equals(".")) {
                    dsclient.send(out, "REDY");
                }
                if (rcvd.startsWith("JCPL")) {
                    dsclient.send(out, "REDY");
                }


                rcvd = in.readLine();
            }
        } catch (Exception IOException) {
            System.out.println("IOException (LRR Sched)");
        }
    }
}
