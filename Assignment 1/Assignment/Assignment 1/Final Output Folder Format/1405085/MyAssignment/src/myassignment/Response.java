
package myassignment;

import java.io.Serializable;


public class Response implements Serializable{
    
    
    public static final String ACKNOWLEDGEMENT = "acknowledged";
    
    public static final String LOG_OUT = "logout";
    
    public static final String  RECEIVE_MESSAGE = "receive";
    
    public static final String STOP_TRANSFER = "stop";
    
    public static final String COMPLETE = "complete";
    
    public static final String SUCCESSFUL = "successful";
    
    public static final String UNSUCCESSFUL = "unsuccessful";
    
    public static final String SEND_FILE = "send";
    
    public static final String LOGGED_IN = "logged_in";
    
    public int size;
    
    public String response = null;
    
    public String id = null;
    
    
    public Response(int x,String id){
     
        switch(x){
            case 1: response = ACKNOWLEDGEMENT;
                    break;
            case 2: response = LOG_OUT;
                    break;            
            case 3: response = RECEIVE_MESSAGE;
                    break;
            case 4: response = STOP_TRANSFER;
                    break;                
            case 5: response = COMPLETE;
                    break;
            case 6: response = SUCCESSFUL;
                    break;                    
            case 7: response = UNSUCCESSFUL;
                    break;
            case 8: response = LOGGED_IN;
                    break;        
        }
        
        this.id = id;
    
    }
    
    public Response(int x,String id,int y){
     
        switch(x){
            case 1: response = ACKNOWLEDGEMENT;
                    break;
            case 2: response = LOG_OUT;
                    break;            
            case 3: response = RECEIVE_MESSAGE;
                    break;
            case 4: response = STOP_TRANSFER;
                    break;                
            case 5: response = COMPLETE;
                    break;
            case 6: response = SUCCESSFUL;
                    break;                    
            case 7: response = UNSUCCESSFUL;
                    break;
            case 8: response = LOGGED_IN;
                    break;        
        }
        
        this.id = id;
        this.size = y;
    
    }
    
}