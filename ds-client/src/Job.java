public class Job {
    int id;
    String type;
    int submitTime;
    int estRunTime;
    int cores;
    int memory;
    int disk;
    public static Job fromString(String line){
        String[] split = line.split(" ");

        var job = new Job();
        job.id = Integer.parseInt(split[1]);
        job.type = split[2];
        job.submitTime = Integer.parseInt(split[3]);
        job.estRunTime = Integer.parseInt(split[4]);
        job.cores = Integer.parseInt(split[5]);
        job.memory = Integer.parseInt(split[6]);
        job.disk = Integer.parseInt(split[7]);
        return job;

    }
}
