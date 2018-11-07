package offlinenetworking;


public class RecieveClient {
    
    
    String filenamereal;
    String filenamechanged;
    long filesizeF;
    int rclient;
    int sclient;
    long mchnkszE;

    public RecieveClient(String filenamereal, String filenamechanged, long filesizeF, int rclient, int sclient, long mchnkszE) {
        this.filenamereal = filenamereal;
        this.filenamechanged = filenamechanged;
        this.filesizeF = filesizeF;
        this.rclient = rclient;
        this.sclient = sclient;
        this.mchnkszE = mchnkszE;
    }

    
    public String getFilenamereal() {
        return filenamereal;
    }

    public String getFilenamechanged() {
        return filenamechanged;
    }

    public long getFilesizeF() {
        return filesizeF;
    }

    public int getRclient() {
        return rclient;
    }

    public int getSclient() {
        return sclient;
    }

    public void setFilenamereal(String filenamereal) {
        this.filenamereal = filenamereal;
    }

    public void setFilenamechanged(String filenamechanged) {
        this.filenamechanged = filenamechanged;
    }

    public long getMchnkszE() {
        return mchnkszE;
    }

    public void setMchnkszE(long mchnkszE) {
        this.mchnkszE = mchnkszE;
    }

    public void setFilesizeF(long filesizeF) {
        this.filesizeF = filesizeF;
    }

    public void setRclient(int rclient) {
        this.rclient = rclient;
    }

    public void setSclient(int sclient) {
        this.sclient = sclient;
    }
    
    
}
