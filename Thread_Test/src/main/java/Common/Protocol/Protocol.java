package Common.Protocol;
public final class Protocol {
    // CONNECTION:
    public static final String ALIVE_CLIENT = "ALIVE_CLIENT";
    public static final String ALIVE_SERVER = "ALIVE_SERVER";
    public static final String CLIENT_OFFLINE = "CLIENT_OFFLINE";
    public static final String SERVER_OFFLINE = "SERVER_OFFLINE";
    public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";
    public static final String CLIENT_CONNECT_MSG = "FIRST_CONNECTION";
    public static final String NICKNAME_IS_TAKEN = "NICKNAME_IS_TAKEN";
    public static final String NICKNAME_IS_EMPTY = "NICKNAME_IS_EMPTY";
    public static final String MAX_CLIENTS = "MAX_CLIENTS";
    public static final String NOT_AUTH = "NOT_AUTH";
    public static final String SUCCESS_CONNECT = "SUCCESSFUL_CONNECTION";

    // ROOMS:
    public static final String ROOM_IN_GAME = "ROOM_IN_GAME";
    public static final String ROOM_WAITING_PLAYER = "ROOM_WAITING_PLAYER";
    public static final String ROOM_WAITING_START = "ROOM_WAITING_START";
    public static final String GET_ROOMS = "GET_ROOMS";
    public static final String NO_ROOMS = "NO_ROOMS";
    public static final String RETURN_ROOMS = "RETURN_ROOMS";
    public static final String CREATE_ROOM = "CREATE_ROOM";
    public static final String DISCONNECT_FROM_ROOM = "DISCONNECT_FROM_ROOM";
    public static final String JOIN_ROOM = "JOIN_ROOM";
    public static final String REQUEST_START_GAME = "REQUEST_START_GAME";
    public static final String CONNECT_TO_A_NEW_SOCKET = "CONNECT_TO_A_NEW_SOCKET";

    // GAME NETWORK:
    public static final String READY_FOR_GAME = "READY_FOR_GAME";

    public static final String CONFIRM_CONNECTION_TO_GAME = "CONFIRM_CONNECTION_TO_GAME";
    public static final String SEND_PADDLE_Y_DIR = "SEND_PADDLE_Y_DIR";
    public static final String GET_OPPONENT_PADDLE_Y_DIR = "GET_OPPONENT_PADDLE_Y_DIR";
    public static final String GET_BALL_ANGLE = "GET_BALL_ANGLE";
    public static final String GET_BALL_INIT = "GET_BALL_INIT";
    public static final String SEND_BALL_REFLECTION = "SEND_BALL_REFLECTION";

    public static final String SEND_BALL_DESTINATION = "SEND_BALL_DESTINATION";
    public static final String GET_BALL_DESTINATION = "GET_BALL_DESTINATION";

    public static final String RIGHT_WON = "RIGHT_WON";
    public static final String LEFT_WON = "LEFT_WON";

    public static final String END_GAME = "END_GAME";

    //GAME GUI:
    public static final String PADDLE_REPAINT = "PADDLE_REPAINT";

    // GUI:
    public static final String GUI_REPAINT = "GUI_REPAINT";
    public static final String GUI_ROOM_REPAINT = "GUI_ROOM_REPAINT";
}
