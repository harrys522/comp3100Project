import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.util.ArrayList;

public class ME {
    // The 'most efficient' algorithm with the purpose of minimising turnaround time.
    // Uses average job estimated time - give the longest jobs to the largest servers
    ArrayList<Server> Servers = new ArrayList<>();
    float efficiencyModifier = 0;

    ME(float eff) {
        efficiencyModifier = eff;
    }

    int mostCores = 0;
    int lrrId = 0;
    int lrrMaxId = 0;
    String lrrType = "";

    public void schedule(BufferedReader in, DataOutputStream out) {
        try {
            ArrayList<Server> Servers = allServers(in, out);

            String rcvd = dsclient.receive(in, "");
            //Schedule Loop
            while (!(rcvd.equals("NONE"))) {
                System.out.println("S:" + rcvd);

                if (rcvd.startsWith("JOBN") || rcvd.startsWith("JOBP")) {
                    Job currentJob = Job.fromJOBN(rcvd);
                    Server selected = Servers.get(0);

                    // Other useful stats
                    int lowestWaitTime = 9999;
                    boolean skip = false;
                    // AVAILABLE AND EXACT
                    for (Server currentServer : Servers) {
                        if (currentServer.canFitAvailable(currentJob) && currentServer.exactFits(currentJob)) {
                            selected = currentServer;
                            skip = true;
                            break;
                        }
                    }
                    // AVAILABLE AND EMPTY
                    for (Server currentServer : Servers) {
                        if (skip) {
                            break;
                        }
                        boolean idle = currentServer.state.equals("idle")
                                || currentServer.state.equals("inactive");
                        if (currentServer.canFitAvailable(currentJob) && idle) {
                            selected = currentServer;
                            skip = true;
                            break;
                        }
                    } // AVAILABLE AND AN EXACT FRACTION

                     // AVAILABLE AND OPTIMISE ALL OVER 90
                    for (Server currentServer : Servers) {
                        if (skip) {
                            break;
                        }
                        boolean allOver80 = currentServer.canFitAvailable(currentJob)
                                && currentServer.optimiseCPU(currentJob, 90)
                                && currentServer.optimiseMemory(currentJob, 90)
                                && currentServer.optimiseDisk(currentJob, 90);
                        if (allOver80) {
                            selected = currentServer;
                            skip = true;
                            break;
                        }
                    } // AVAILABLE AND OPTIMISE TWO OVER 80
                    for (Server currentServer : Servers) {
                        if (skip) {
                            break;
                        }
                        boolean twoOver80 = currentServer.canFitAvailable(currentJob)
                                && ((currentServer.optimiseCPU(currentJob, 80)
                                && currentServer.optimiseMemory(currentJob, 80))
                                || (currentServer.optimiseDisk(currentJob, 80)
                                && currentServer.optimiseCPU(currentJob, 80))
                                || (currentServer.optimiseMemory(currentJob, 80)
                                && currentServer.optimiseDisk(currentJob, 80)));
                        if (twoOver80) {
                            selected = currentServer;
                            skip = true;
                            break;
                        }
                    }
                    for (Server currentServer : Servers) {
                        if (skip) {
                            break;
                        }
                        if (currentServer.canFitAvailable(currentJob)) {
                            selected = currentServer;
                            break;
                        }
                    }
                    // Quickest estimated wait time
                    /*
                    for (Server currentServer : bestServers) {
                        if (skip) {
                            break;
                        }
                        if (!(currentServer.canFit(currentJob))) {
                            break;
                        }
                        int eta = 0;
                        for (Job j : currentServer.jobsAssigned) {
                            eta += j.estRunTime;
                        }
                        if (eta == 0) {
                            selected = currentServer;
                            break;
                        }
                        if (eta < lowestWaitTime) {
                            lowestWaitTime = eta;
                            selected = currentServer;
                        }
                    }
                    */
                    if (!selected.canFit(currentJob)) {
                        for (Server currentServer : Servers) {
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
                    for (Server n : Servers) {
                        if (n.id == finished.id) {
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
                if (current.cores > mostCores) {
                    mostCores = current.cores;
                    if (current.id > lrrMaxId) {
                        lrrMaxId = current.id;
                    }
                }
            }

            // Find server types with the most cores
            boolean typeFound = false;
            for (Server n : Servers) {
                if (n.cores == mostCores && !typeFound) {
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
