import java.util.ArrayList;

public class Server {
    String type;
    int id;
    String state;
    int curStartTime;
    int cores;
    int memory;
    int disk;
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
        server.type = split[3];
        server.id = Integer.parseInt(split[4]);

        // Find server
        for(int i=0;i< serverList.size();i++){
            Server thisServer = serverList.get(i);
            if(server.id == thisServer.id) {
                server = thisServer;
            }
        }
        server.state = "idle";
        return server;
    }
    Server available;
    ArrayList<Job> jobsAssigned = new ArrayList<Job>();
    public Server use(Server server, Job job) {
        // Function for changing available resources of a server. Needs better name ie assign/schedule
        jobsAssigned.add(job);
        server.available.cores -= job.cores;
        server.available.disk -= job.disk;
        server.available.memory -= job.memory;
        return server;
    }
}
