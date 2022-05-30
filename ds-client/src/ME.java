import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.util.ArrayList;

public class ME {
    // The 'most efficient' algorithm with the purpose of minimising turnaround time.

    // Server and job stats in public variables to make scheduling decision?
    // ie Average job estimated time - give jobs over the average to the largest servers
    ArrayList<Server> Servers = new ArrayList<>();
    ArrayList<Integer> estimatedRuntimes = new ArrayList<>();
    int efficiencyModifier = 0;
    ME(int eff){
        efficiencyModifier = eff;
    }
    int mostCores = 0;
    
    int lrrId = 0;

    public void schedule(BufferedReader in, DataOutputStream out) {
        try {
            ArrayList<Server> Servers = allServers(in,out);

            String rcvd = dsclient.receive(in,"");
            //Schedule Loop
            while (!(rcvd.equals("NONE"))) {
                System.out.println("S:" + rcvd);

                if (rcvd.startsWith("JOBN") || rcvd.startsWith("JOBP")) {
                    Job currentJob = Job.fromJOBN(rcvd);
                    Server selected = Servers.get(0);

                    // Running average of estRunTimes (gets calculated on every schedule).
                    estimatedRuntimes.add(currentJob.estRunTime);
                    float averageETA = 0;
                    int totalForAverage = 0;
                    for(int i : estimatedRuntimes){
                        totalForAverage += i;
                    }
                    averageETA = totalForAverage / estimatedRuntimes.size();

                    // Select a server to schedule the current job to:
                    if(currentJob.estRunTime*100 >= averageETA*efficiencyModifier){
                        // Use LRR (most cores, ignore type) for these
                    } else {
                        // Select a server based on something else, ie best fit or first fit.
                    }

                    // SCHD
                    var scheduleCmd = currentJob.id + " " + selected.type + " " + selected.id; //
                    dsclient.send(out, "SCHD " + scheduleCmd);
                    selected.schedule(currentJob);
                }
                if (rcvd.equals("OK") || rcvd.equals(".")) {
                    dsclient.send(out, "REDY");
                }
                if (rcvd.startsWith("JCPL")) {
                    Server finished = Server.fromComplete(rcvd, Servers);
                    for(Server n : Servers){
                        if(n.id == finished.id){
                            n = finished;
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
                }
            }


            // Find server types with the most cores
            for(Server n : Servers){

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
