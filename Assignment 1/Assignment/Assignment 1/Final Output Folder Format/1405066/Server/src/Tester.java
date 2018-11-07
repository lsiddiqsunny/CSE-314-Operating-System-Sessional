





import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.JPanel;

public class Tester extends Frame {
    

    public static void main(String args[]) {
        JPanel panelBgImg = new JPanel() {
            public void drawCenteredCircle(Graphics2D g, int x, int y, int r) {
                x = x-(r/2);
                y = y-(r/2);
                g.fillOval(x,y,r,r);
            }
        };
    }
}