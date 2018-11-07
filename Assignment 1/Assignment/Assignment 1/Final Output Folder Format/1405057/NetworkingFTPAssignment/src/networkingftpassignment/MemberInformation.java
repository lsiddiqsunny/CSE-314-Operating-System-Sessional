package networkingftpassignment;

import java.net.Socket;

/**
 *
 * @author HP
 */

public class MemberInformation {
    String studentId;
    Socket socket;

    public MemberInformation(String studentId, Socket socket) {
        this.studentId = studentId;
        this.socket = socket;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    
    
}
