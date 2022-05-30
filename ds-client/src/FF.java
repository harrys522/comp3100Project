import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

public class FF {
    public static ArrayList<Server> getServers(BufferedReader in, DataOutputStream out, Job first) {
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
            System.out.println("IO Exception (ff)");
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
                    Job currentJob = Job.fromJOBN(rcvd);
                    Server selected = Servers.get(Servers.size()-1); // Select last server by default
                    boolean serverFound = false;

                    for(Server currentServer : Servers) {
                        boolean idle = currentServer.state.equals("idle") || currentServer.state.equals("inactive");
                        if(serverFound){
                            break;
                        }
                        if (currentServer.canFitAvailable(currentJob) && idle) {
                            selected = currentServer;
                            serverFound = true;
                            break;
                        }
                    }
                    for(Server currentServer : Servers) {
                        if(serverFound){
                            break;
                        }
                        if (currentServer.canFitAvailable(currentJob)) {
                            selected = currentServer;
                            serverFound = true;
                            break;
                        }
                    }
                    for(Server currentServer : Servers) {
                        if(serverFound){
                            break;
                        }
                        if (currentServer.canFit(currentJob)) {
                            selected = currentServer;
                            break;
                        }
                    }

                    // SCHD
                    var scheduleCmd = currentJob.id + " " + selected.type + " " + selected.id; //
                    dsclient.send(out, "SCHD " + scheduleCmd);
                    selected.schedule(currentJob);
                }
                if (rcvd.equals("OK") || rcvd.equals(".") ) { //|| rcvd.equals("")
                    dsclient.send(out, "REDY");
                }
                if (rcvd.startsWith("JCPL")) {
                    Server finished = Server.fromComplete(rcvd, Servers);
                    dsclient.send(out, "REDY");
                }
                if (rcvd.startsWith("ERR")) {
                    dsclient.send(out, "REDY");
                }
                rcvd = in.readLine();
            }
        } catch (Exception IOException) {
            System.out.println("IOException (ff Sched)");
        }
    }
}
