import java.util.ArrayList;

public class Server {
    String type;
    int id;
    String state;
    int curStartTime;
    int cores;
    int memory;
    int disk;

    // My variables
    Server available = this;
    ArrayList<Job> jobsAssigned = new ArrayList<Job>();
    public static Server fromString(String line){
        String[] split = line.split(" ");
        var server = new Server();
        server.type = split[0];
        server.id = Integer.parseInt(split[1]);
        server.state = split[2];
        server.curStartTime = Integer.parseInt(split[3]);
        server.cores = Integer.parseInt(split[4]);
        server.memory = Integer.parseInt(split[5]);
        server.disk = Integer.parseInt(split[6]);
        return server;
    }
    public static Server fromComplete(String line, ArrayList<Server> serverList){
        String[] split = line.split(" ");
        var server = new Server();
        int jobId = Integer.parseInt(split[2]);
        server.type = split[3];
        server.id = Integer.parseInt(split[4]);

        // Find server
        for(int i=0;i< serverList.size();i++){
            Server thisServer = serverList.get(i);
            if(server.id == thisServer.id) {
                server = thisServer;
            }
        }
        for(Job j : server.jobsAssigned){
            if(j.id == jobId){
                server.jobsAssigned.remove(j);
            }
        }

        // If available.resources == server.resources then:
        server.state = "idle";
        return server;
    }

    public Server schedule(Job job) {
        // Function for changing available resources of a server.
        jobsAssigned.add(job);
        this.available.cores -= job.cores;
        this.available.disk -= job.disk;
        this.available.memory -= job.memory;
        return available;
    }

    public void updateAvailable() {
        this.available.cores = this.cores;
        this.available.disk = this.disk;
        this.available.memory = this.memory;

        for(Job j : jobsAssigned) {
            this.available.cores -= j.cores;
            this.available.disk -= j.disk;
            this.available.memory -= j.memory;
        }
    }

    public boolean canFit(Job job){
        boolean fit = this.available.cores >= job.cores
                && this.available.memory >= job.memory
                && this.available.disk >= job.disk;
        return fit;
    }
}
