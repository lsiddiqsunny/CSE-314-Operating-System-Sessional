package Adaptor;

import Model.ListEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ListEntryCellRenderer extends JLabel implements ListCellRenderer
{
   //private JLabel label;

   public ListEntryCellRenderer(){
      // label = new JLabel();
       //label.setOpaque(true);
       setOpaque(false);
       setIconTextGap(3);
   }
  
   public Component getListCellRendererComponent(JList list, Object value,
                                                 int index, boolean isSelected,
                                                 boolean cellHasFocus) {
      ListEntry node = (ListEntry) value;

       JPanel box=new JPanel();

       JRadioButton b=new JRadioButton();
       JButton b1=new JButton(node.getValue());
       b1.setBackground(new Color(70,66,218));
       b1.setForeground(Color.WHITE);
       b.setSelected(true);

       box.add(b);
       box.add(b1);
       box.setLayout(new BoxLayout(box, BoxLayout.X_AXIS));
       box.setBackground(new Color(56,58,60));
       box.setBorder(new EmptyBorder(2,15,0,0));

       if(isSelected) {
          // box.setBackground(new Color(220,250,255));
       }
  
     return box;
   }
}

