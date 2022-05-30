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
    int availableCores=0;
    int availableDisk=0;
    int availableMemory=0;
    ArrayList<Job> jobsAssigned = new ArrayList<>();
    int estimatedWait = 0;
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
        server.updateAvailable();
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
        Job removeJob = new Job();
        for(Job j : server.jobsAssigned){
            if(j.id == jobId){
                removeJob = j;
            }
        }
        server.jobsAssigned.remove(removeJob);
        server.updateAvailable();
        return server;
    }

    public void schedule(Job job) {
        // Function for changing available resources of a server.
        jobsAssigned.add(job);
        this.availableCores -= job.cores;
        this.availableDisk -= job.disk;
        this.availableMemory -= job.memory;
        this.state = "active";
    }

    public void updateAvailable() {
        this.availableCores = this.cores;
        this.availableDisk = this.disk;
        this.availableMemory = this.memory;
        for(Job j : jobsAssigned) {
            this.availableCores -= j.cores;
            this.availableDisk -= j.disk;
            this.availableMemory -= j.memory;
        }
        if(jobsAssigned.isEmpty()){
            this.state = "idle";
        }
        System.out.println("Updated resources, current assigned jobs: " + jobsAssigned.size());
    }
    public boolean canFit(Job job){
        boolean fit = this.cores >= job.cores
                && this.memory >= job.memory
                && this.disk >= job.disk;
        return fit;
    }
    public boolean canFitAvailable(Job job){
        boolean fit = this.availableCores >= job.cores
                && this.availableMemory >= job.memory
                && this.availableDisk >= job.disk;
        return fit;
    }
    public boolean exactFits(Job job){
        boolean fit = this.availableCores == job.cores
                && this.availableMemory == job.memory
                && this.availableDisk == job.disk;
        return fit;
    }
    public boolean fractionFits(Job job){
        boolean fit = false;
        int nCores = 0;
        int nMem = 0;
        int nDisk = 0;
        for(int i=1; i<10;i++){
            if(job.cores==availableCores/i){
                nCores = i;
            }
            if(job.memory==availableMemory/i){
                nMem = i;
            }
            if(job.disk==availableDisk/i){
                nDisk = i;
            }
        }
        if(nCores>1 || nMem>1 || nDisk>1){
            fit = true;
        }
        return fit;
    }

    public boolean optimiseCPU(Job job, int aim){
        boolean meetsAim = false;
        int fullUsage = this.cores*100;
        return meetsAim = fullUsage - (this.availableCores - job.cores) >= fullUsage * aim;
    }
    public boolean optimiseMemory(Job job, int aim){
        boolean meetsAim = false;
        int fullUsage = this.cores*100;
        return meetsAim = fullUsage - (this.availableCores - job.cores) >= fullUsage * aim;
    }
    public boolean optimiseDisk(Job job, int aim){
        boolean meetsAim = false;
        int fullUsage = this.cores*100;
        return meetsAim = fullUsage - (this.availableCores - job.cores) >= fullUsage * aim;
    }

}