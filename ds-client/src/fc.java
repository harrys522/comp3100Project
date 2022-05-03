import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class fc {
    public static Server find(BufferedReader in, DataOutputStream out, Job first){
        try {
            String getCmd = "GETS Capable " + first.cores + " " + first.memory + " " + first.disk;
            System.out.println(getCmd); // Debugging
            dsclient.send(out, getCmd);
            String data = dsclient.receive(in, "DATA");

            dsclient.send(out, "OK");
            String rcvd = in.readLine();
            Server found = Server.fromString(rcvd);
            dsclient.send(out, "OK");
            return found;
        }
        catch (Exception IOException) {
            System.out.println("IO Exception (fc)");
        }
        return null;
    }

    public static void schedule(BufferedReader in, DataOutputStream out, String rcvd) {
        try {
            int currentId = 0;
            // Scheduling loop
            while (!(rcvd.equals("NONE"))) {
                System.out.println("S:" + rcvd);

                // Schedule a job
                if (rcvd.startsWith("JOBN") || rcvd.startsWith("JOBP")) {
                    Job currentJob = Job.fromJOBN(rcvd);
                    // Update GETS Capable
                    //servers = getServers(in,out,currentJob);
                    Server useServer = find(in,out,currentJob);

                    //var scheduleCmd = currentJob.id + " " + servers.get(currentId).type + " " + servers.get(currentId).id;
                    var scheduleCmd = currentJob.id + " " + useServer.type + " " + useServer.id;
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
            System.out.println("IOException (fc Sched)");
        }
    }
}
