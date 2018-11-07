
package myassignment;

import javax.swing.JFrame;

public class Singleton {
    private static JFrame frame = null;
    
    private Singleton(){
    
    }
     
    public static JFrame getInstance(){
        if(frame == null){
           frame = new JFrame();
           
        }
        return frame;
    
    }
    
}
    
