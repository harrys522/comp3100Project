import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class FC {
    public static void schedule(BufferedReader in, DataOutputStream out, Job first) {
        try {
            var rcvd = in.readLine();
            Job currentJob = first;

            // Scheduling loop
            while (!(rcvd.equals("NONE"))) {
                System.out.println("S:" + rcvd);

                // Schedule a job
                if (rcvd.startsWith("JOBN") || rcvd.startsWith("JOBP")) {
                    // Find first capable
                    currentJob = Job.fromJOBN(rcvd);
                    Server selected = getCapable(in, out, currentJob);

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
            System.out.println("IOException (fc Sched)");
        }
    }

    public static Server getCapable(BufferedReader in, DataOutputStream out, Job job) {
        String getCmd = "GETS Capable " + job.cores + " " + job.memory + " " + job.disk;
        dsclient.send(out, getCmd);
        String data = dsclient.receive(in, "DATA");
        String rcvd = dsclient.receive(in, "");
        Server server = Server.fromString(rcvd);

        return server;
    }

}
