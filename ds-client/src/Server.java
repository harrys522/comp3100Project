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

    // Command Functions
}
