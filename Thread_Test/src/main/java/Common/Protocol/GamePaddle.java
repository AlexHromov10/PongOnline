package Common.Protocol;

public class GamePaddle {
    public Integer pNum;
    public int yDir;

    public static class Builder {
        private GamePaddle newGamePaddle;

        public Builder() {
            newGamePaddle = new GamePaddle();
        }

        public GamePaddle.Builder withPNum(int pNum){
            newGamePaddle.pNum = pNum;
            return this;
        }

        public GamePaddle.Builder withYDir(int y){
            newGamePaddle.yDir = y;
            return this;
        }

        public GamePaddle build(){
            return newGamePaddle;
        }

    }
}