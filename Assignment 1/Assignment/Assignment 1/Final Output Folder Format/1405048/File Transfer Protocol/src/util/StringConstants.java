package util;

import java.io.Serializable;

/**
 * @author ANTU on 21-Sep-17.
 * @project Socket
 */
public class StringConstants implements Serializable {
    public static final String SEND_FILENAME_AND_SIZE = "1";
    public static final String START_SENDING_FILE = "2";
    public static final String NOT_ENOUGH_SPACE_IN_SERVER = "3";
    public static final String SPACE_AVAILABLE_IN_SERVER = "21";
    public static final String KEEP_SENDING_FILE = "4";
    public static final String TIMEOUT_ABORT = "5";
    public static final String CHUNK_SENDING = "6";
    public static final String SENDING_COMPLETED = "7";
    public static final String SENT_SUCCESSFUL = "8";
    public static final String SENT_FAILED = "9";
    public static final String NO_STUDENT_ID = "10";
    public static final String SENDING_FIRST_CHUNK = "11";
    public static final String START_SERVER_SENDING = "12";
    public static final String RECEIVING_DENIED = "13";
    public static final String ID_EXISTS = "14";
    public static final String ID_DOES_NOT_EXIST = "15";
    public static final String CHUNKS_MATCHED = "16";
    public static final String CHUNKS_DID_NOT_MATCH = "17";
    public static final String ENOUGH_KEEPING_STEADY = "18";
    public static final String SERVER_START_SENDING = "19";
    public static final String SERVER_STOP_SENDING = "20";
}
