public class Server {
    String type;
    int limit;
    int bootupTime;
    float hourlyRate;
    int cores;
    int memory;
    int disk;
    public static Server fromString(String line){
        String[] split = line.split(" ");

        var server = new Server();
        server.type = split[1];
        server.limit = Integer.parseInt(split[2]);
        server.bootupTime = Integer.parseInt(split[3]);
        server.hourlyRate = Integer.parseInt(split[4]);
        server.cores = Integer.parseInt(split[5]);
        server.memory = Integer.parseInt(split[6]);
        server.disk = Integer.parseInt(split[7]);
        return server;
    }
}
