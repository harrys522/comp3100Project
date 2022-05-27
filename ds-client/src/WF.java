import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.util.ArrayList;

public class WF {
    public static ArrayList<Server> getServers(DataOutputStream out, BufferedReader in, Job first) {
        ArrayList<Server> ffServers = new ArrayList<Server>();
        try {
            String getCmd = "GETS All";
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
                    // Find worst fit (largest and most available server).
                    Job currentJob = Job.fromJOBN(rcvd);
                    Server selected = Servers.get(0);

                    // Running total vars
                    var runningCores =0;
                    var runningMem =0;
                    var runningDisk =0;
                    // Select by

                    for(int i=0;i<Servers.size();i++){
                        Server currentServer = Servers.get(i);
                        boolean fit = currentServer.cores >= currentJob.cores
                                && currentServer.memory >= currentJob.memory
                                && currentServer.disk >= currentJob.disk;
                        boolean idle = currentServer.state.equals("idle");
                        boolean notIdle = currentServer.state.equals("active") || currentServer.state.equals("booting");
                        if(fit && idle) {
                            selected = currentServer;
                            selected.state = "active";
                            break;
                        } else if (fit && notIdle){ // If no server found then select first active/booting
                            selected = currentServer;
                        } else {
                            System.out.println("NO CAPABLE SERVER FOUND");
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
                    Server finished = Server.fromComplete(rcvd, Servers);
                    dsclient.send(out, "REDY");
                }


                rcvd = in.readLine();
            }
        } catch (Exception IOException) {
            System.out.println("IOException (wf Sched)");
        }
    }
}
