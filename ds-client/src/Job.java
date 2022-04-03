public class Job {
    int id;
    String type;
    int submitTime;
    int estRunTime;
    int cores;
    int memory;
    int disk;

    // Translate the server's JOBN into a Job object.
    public static Job fromJOBN(String jobn) {
        String[] split = jobn.split(" ");
        var job = new Job();

        job.submitTime = Integer.parseInt(split[1]);
        job.id = Integer.parseInt(split[2]);
        job.estRunTime = Integer.parseInt(split[3]);
        job.cores = Integer.parseInt(split[4]);
        job.memory = Integer.parseInt(split[5]);
        job.disk = Integer.parseInt(split[6]);

        return job;
    }
}
