package Common.Protocol;

public class Packet {
    public String command;
    public String msg;
    public Client client;
    public Room[] rooms;
    public GamePaddle gamePaddle;
    public GameBall gameBall;

    public static class Builder {
        private Packet newPacket;

        public Builder(String command) {
            newPacket = new Packet();
            newPacket.command = command;
        }
        public Packet.Builder withMsg(String msg){
            newPacket.msg = msg;
            return this;
        }

        public Packet.Builder withClient(Client client){
            newPacket.client = client;
            return this;
        }

        public Packet.Builder withRoom(Room room){
            Room[] roomsArr = new Room[1];
            roomsArr[0] = room;
            newPacket.rooms = roomsArr;
            return this;
        }

        public Packet.Builder withGamePaddle(GamePaddle gameMsg){
            newPacket.gamePaddle = gameMsg;
            return this;
        }

        public Packet.Builder withGameBall(GameBall gameBall){
            newPacket.gameBall = gameBall;
            return this;
        }

        public Packet build(){
            return newPacket;
        }

    }

}
