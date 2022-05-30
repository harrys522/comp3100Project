import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.util.ArrayList;

public class ME {
    // The 'most efficient' algorithm with the purpose of minimising turnaround time.
    // Uses average job estimated time - give the longest jobs to the largest servers
    ArrayList<Server> Servers = new ArrayList<>();
    ArrayList<Integer> estimatedRuntimes = new ArrayList<>();
    float efficiencyModifier = 0;
    ME(float eff){
        efficiencyModifier = eff;
    }
    int mostCores = 0;
    int lrrId = 0;
    int lrrMaxId = 0;
    String lrrType = "";
    public void schedule(BufferedReader in, DataOutputStream out) {
        try {
            ArrayList<Server> Servers = allServers(in,out);

            String rcvd = dsclient.receive(in,"");
            //Schedule Loop*
            while (!(rcvd.equals("NONE"))) {
                System.out.println("S:" + rcvd);

                if (rcvd.startsWith("JOBN") || rcvd.startsWith("JOBP")) {
                    Job currentJob = Job.fromJOBN(rcvd);
                    Server selected = Servers.get(0);

                    // Running average of estRunTimes (gets calculated on every schedule).
                    estimatedRuntimes.add(currentJob.estRunTime);
                    int totalForAverage = 0;

                    for(int i : estimatedRuntimes){
                        totalForAverage += i;
                    }
                    float averageETA = totalForAverage / estimatedRuntimes.size();

                    // Other useful stats
                    int lowestWaitTime = 99000;
                    boolean skip = false;

                    // Select a server to schedule the current job to:
                    if(currentJob.estRunTime >= averageETA*(1 + efficiencyModifier/100) && estimatedRuntimes.size() > 5){
                        // Use LRR (most cores) for these
                        for(int i=0;i<Servers.size();i++){
                            Server currentServer = Servers.get(i);
                            boolean LRR = currentServer.cores == mostCores
                                        && currentServer.type.equals(lrrType)
                                        && currentServer.id == lrrId;
                            if(LRR && currentServer.canFit(currentJob)){
                                selected = currentServer;
                                lrrId++;
                                if(lrrId>lrrMaxId){
                                    lrrId=0;
                                }
                            }
                        }
                    } else {
                        // Select a server based on something else, ie best fit or first fit.
                        // First available, idle, fit
                        for(Server currentServer : Servers) {
                            boolean idle = currentServer.state.equals("idle")
                                    || currentServer.state.equals("inactive");
                            if (currentServer.canFitAvailable(currentJob) && idle) {
                                selected = currentServer;
                                skip = true;
                                break;
                            }
                        }
                        for(Server currentServer : Servers) {
                            if(skip){
                                break;
                            }
                            if (currentServer.canFitAvailable(currentJob)){
                                selected = currentServer;
                                skip=true;
                                break;
                            }
                        }
                        // Quickest estimated wait time
                        for(Server currentServer : Servers){
                            if(skip){
                                break;
                            }
                            if(!(currentServer.canFit(currentJob))){
                                break;
                            }
                            int eta = 0;
                            for(Job j : currentServer.jobsAssigned){
                                eta += j.estRunTime;
                            }
                            if(eta==0){
                                selected = currentServer;
                                break;
                            }
                            if(eta<lowestWaitTime){
                                lowestWaitTime = eta;
                                selected = currentServer;
                            }
                        }
                    }
                    if(!selected.canFit(currentJob)){
                        for(Server currentServer : Servers) {
                            if (currentServer.canFit(currentJob)) {
                                selected = currentServer;
                                break;
                            }
                        }
                    }

                    // SCHD Command
                    var scheduleCmd = currentJob.id + " " + selected.type + " " + selected.id;
                    dsclient.send(out, "SCHD " + scheduleCmd);
                    selected.schedule(currentJob);
                }
                if (rcvd.equals("OK") || rcvd.equals(".") || rcvd.isBlank()) {
                    dsclient.send(out, "REDY");
                }
                if (rcvd.startsWith("JCPL")) {
                    Server finished = Server.fromComplete(rcvd, Servers);
                    for(Server n : Servers){
                        if(n.id == finished.id){
                            n = finished;
                            n.updateAvailable();
                        }
                    }
                    dsclient.send(out, "REDY");
                }
                if (rcvd.startsWith("ERR")) {
                    dsclient.send(out, "OK");
                }
                rcvd = in.readLine();
            }
        } catch (Exception IOException) {
            System.out.println("IOException (Most Efficient)");
        }
    }

    public ArrayList<Server> allServers(BufferedReader in, DataOutputStream out) {
        try {
            // Get server list from ds-server
            String getCmd = "GETS All";
            dsclient.send(out, getCmd);
            String data = dsclient.receive(in, "DATA");
            String[] split = data.split(" ");
            int serverCount = Integer.parseInt(split[1]);
            dsclient.send(out, "OK");

            // Populate Servers arraylist
            for (int i = 0; i < serverCount; i++) {
                String rcvd = dsclient.receive(in, "");
                Server current = Server.fromString(rcvd);
                Servers.add(current);
                if(current.cores > mostCores){
                    mostCores = current.cores;
                    if(current.id>lrrMaxId){
                        lrrMaxId=current.id;
                    }
                }
            }

            // Find server types with the most cores
            boolean typeFound = false;
            for(Server n : Servers){
                if(n.cores == mostCores && !typeFound){
                    lrrType = n.type;
                    typeFound = true;
                }
            }

            // Complete GETS and return the arraylist.
            dsclient.send(out, "OK");
            return Servers;
        } catch (Exception IOException) {
            System.out.println("IO Exception (most efficient)");
        }
        return null;
    }
}
