import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.util.ArrayList;

public class FF {
    public static ArrayList<Server> getServers(DataOutputStream out, BufferedReader in, Job first) {
        ArrayList<Server> ffServers = new ArrayList<Server>();
        try {
            boolean typefound = false;
            var lrrtype = "";
            int mostCores = 0;

            //String getCmd = "GETS Capable " + first.cores + " " + first.memory + " " + first.disk;
            String getCmd = "GETS All";

            System.out.println(getCmd); // Debugging
            dsclient.send(out, getCmd);
            String data = dsclient.receive(in, "DATA");

            String[] split = data.split(" ");
            int serverCount = Integer.parseInt(split[1]);

            dsclient.send(out, "OK");

            for (int i = 0; i < serverCount; i++) {
                String rcvd = dsclient.receive(in, "");
                Server current = Server.fromString(rcvd);
                ffServers.add(current);
            }

            dsclient.send(out, "OK");
            return ffServers;
        } catch (Exception IOException) {
            System.out.println("IO Exception (lrr)");
        }
        return ffServers;
    }

    public static void schedule(ArrayList<Server> Servers, BufferedReader in, DataOutputStream out) {
        try {
            var rcvd = in.readLine();

            // Scheduling loop
            while (!(rcvd.equals("NONE"))) {
                System.out.println("S:" + rcvd);

                // Schedule a job
                if (rcvd.startsWith("JOBN") || rcvd.startsWith("JOBP")) {
                    // Find first capable
                    Job currentJob = Job.fromJOBN(rcvd);
                    Server selected = Servers.get(0);
                    for(int i=0;i<Servers.size();i++){
                        Server currentServer = Servers.get(i);
                        // cores memory disk
                        if(currentServer.cores >= currentJob.cores && currentServer.memory >= currentJob.memory && currentServer.disk > currentJob.disk) {
                            selected = currentServer;
                            break;
                        } else {
                            selected = Servers.get(i);
                        }
                    }

                    // SCHD
                    var scheduleCmd = currentJob.id + " " + selected.type + " " + selected.id; //
                    dsclient.send(out, "SCHD " + scheduleCmd);
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
