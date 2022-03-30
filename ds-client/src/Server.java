public class Server {
    String type;
    int id;
    String state;
    String curStartTime;
    int cores;
    int memory;
    int disk;
    public static Server fromString(String line){
        String[] split = line.split(" ");

        var server = new Server();
        server.type = split[1];
        server.id = Integer.parseInt(split[2]);
        server.state = split[3];
        server.curStartTime = split[4];
        server.cores = Integer.parseInt(split[5]);
        server.memory = Integer.parseInt(split[6]);
        server.disk = Integer.parseInt(split[7]);
        return server;
    }
}
