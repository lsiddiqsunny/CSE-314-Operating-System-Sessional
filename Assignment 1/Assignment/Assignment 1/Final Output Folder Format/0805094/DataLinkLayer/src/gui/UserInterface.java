/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

/**
 *
 * @author Tanzim Ahmed
 */

import controller.Controller;
import dll.DLLLogger;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import util.Printer;

public class UserInterface
  extends JFrame
  implements ActionListener, DLLLogger
{
  private static final int JPANEL_WIDTH = 650;
  JButton connectJButton;
  JButton browseSendJButton;
  JButton sendJButton;
  JTextField fileNameJTextField;
  JTextField textJTextField;
  JLabel fileInfoJLabel;
  DefaultListModel sendDefaultListModel;
  DefaultListModel receiveDefaultListModel;
  JTextField fileNameSaveJTextField;
  JButton browseSaveJButton;
  JButton clearJButton;
  JTextField srcJTextField;
  JTextField destJTextField;
  JLabel sendSuccessfulJLabel;
  JLabel receiveSuccessfulJLabel;
  JTextArea receivedTextJTextArea;
  
  public UserInterface()
  {
    JPanel srcDestJPanel = new JPanel();
    BoxLayout srcDesBoxLayout = new BoxLayout(srcDestJPanel, 0);
    
    srcDestJPanel.setLayout(srcDesBoxLayout);
    JLabel srcJLabel = new JLabel("Sender Mac ");
    JLabel destJLabel = new JLabel("Receiver Mac ");
    JLabel connectJLabel = new JLabel("Connect ");
    
    this.srcJTextField = new JTextField();
    this.destJTextField = new JTextField();
    
    this.srcJTextField.setText("1A");
    this.destJTextField.setText("2A");
    
    this.connectJButton = new JButton("Connect ");
    this.connectJButton.addActionListener(this);
    
    srcDestJPanel.add(Box.createHorizontalStrut(10));
    srcDestJPanel.add(srcJLabel);
    srcDestJPanel.add(Box.createHorizontalStrut(10));
    srcDestJPanel.add(this.srcJTextField);
    srcDestJPanel.add(Box.createHorizontalStrut(10));
    srcDestJPanel.add(destJLabel);
    srcDestJPanel.add(Box.createHorizontalStrut(10));
    srcDestJPanel.add(this.destJTextField);
    srcDestJPanel.add(Box.createHorizontalStrut(10));
    srcDestJPanel.add(connectJLabel);
    srcDestJPanel.add(Box.createHorizontalStrut(10));
    srcDestJPanel.add(this.connectJButton);
    srcDestJPanel.add(Box.createHorizontalStrut(10));
    
    Dimension srcDestJPanelDimension = new Dimension(650, 25);
    
    srcDestJPanel.setPreferredSize(srcDestJPanelDimension);
    srcDestJPanel.setMaximumSize(srcDestJPanelDimension);
    srcDestJPanel.setMinimumSize(srcDestJPanelDimension);
    
    JPanel fileChooserJPanel = new JPanel();
    
    JLabel selectFileJLabel = new JLabel("Select File: ");
    this.fileNameJTextField = new JTextField();
    this.fileNameJTextField.setEditable(false);
    
    this.browseSendJButton = new JButton("Browse");
    this.browseSendJButton.addActionListener(this);
    this.browseSendJButton.setEnabled(false);
    this.sendJButton = new JButton("Send");
    this.sendJButton.addActionListener(this);
    this.sendJButton.setEnabled(false);
    
    BoxLayout fileChooserBoxLayout = new BoxLayout(fileChooserJPanel, 0);
    fileChooserJPanel.setLayout(fileChooserBoxLayout);
    
    fileChooserJPanel.add(Box.createHorizontalStrut(10));
    fileChooserJPanel.add(selectFileJLabel);
    fileChooserJPanel.add(Box.createHorizontalStrut(10));
    fileChooserJPanel.add(this.fileNameJTextField);
    fileChooserJPanel.add(Box.createHorizontalStrut(10));
    fileChooserJPanel.add(this.browseSendJButton);
    fileChooserJPanel.add(Box.createHorizontalStrut(10));
    fileChooserJPanel.add(this.sendJButton);
    fileChooserJPanel.add(Box.createHorizontalStrut(10));
    
    fileChooserJPanel.setPreferredSize(srcDestJPanelDimension);
    fileChooserJPanel.setMaximumSize(srcDestJPanelDimension);
    fileChooserJPanel.setMinimumSize(srcDestJPanelDimension);
    
    JPanel fileInfoJPanel = new JPanel();
    this.fileInfoJLabel = new JLabel("No file selected. Select a file");
    
    BoxLayout fileInfoBoxLayout = new BoxLayout(fileInfoJPanel, 0);
    fileInfoJPanel.setLayout(fileInfoBoxLayout);
    fileInfoJPanel.add(Box.createHorizontalStrut(10));
    fileInfoJPanel.add(this.fileInfoJLabel);
    fileInfoJPanel.add(Box.createHorizontalStrut(10));
    
    fileInfoJPanel.setPreferredSize(srcDestJPanelDimension);
    fileInfoJPanel.setMaximumSize(srcDestJPanelDimension);
    fileInfoJPanel.setMinimumSize(srcDestJPanelDimension);
    
    Dimension logJScrollPanelDimension = new Dimension(300, 200);
    
    JPanel sendLogJPanel = new JPanel();
    BoxLayout sendLogBoxLayout = new BoxLayout(sendLogJPanel, 1);
    sendLogJPanel.setLayout(sendLogBoxLayout);
    
    this.sendSuccessfulJLabel = new JLabel();
    this.receiveSuccessfulJLabel = new JLabel();
    
    JLabel sendJLabel = new JLabel("Sent");
    sendJLabel.setAlignmentX(0.5F);
    this.sendDefaultListModel = new DefaultListModel();
    JList sendLogJList = new JList(this.sendDefaultListModel);
    
    JScrollPane sendLogJScrollPane = new JScrollPane(sendLogJList);
    sendLogJScrollPane.setPreferredSize(logJScrollPanelDimension);
    sendLogJScrollPane.setMinimumSize(logJScrollPanelDimension);
    sendLogJScrollPane.setMaximumSize(logJScrollPanelDimension);
    
    sendLogJPanel.add(sendJLabel);
    sendLogJPanel.add(Box.createVerticalStrut(10));
    sendLogJPanel.add(sendLogJScrollPane);
    
    this.sendSuccessfulJLabel.setAlignmentX(0.5F);
    sendLogJPanel.add(this.sendSuccessfulJLabel);
    
    JPanel receiveLogJPanel = new JPanel();
    BoxLayout receiveLogBoxLayout = new BoxLayout(receiveLogJPanel, 1);
    receiveLogJPanel.setLayout(receiveLogBoxLayout);
    
    JLabel receiveJLabel = new JLabel("Received");
    receiveJLabel.setAlignmentX(0.5F);
    this.receiveDefaultListModel = new DefaultListModel();
    JList receiveLogJList = new JList(this.receiveDefaultListModel);
    
    JScrollPane receiveLogJScrollPane = new JScrollPane(receiveLogJList);
    receiveLogJScrollPane.setPreferredSize(logJScrollPanelDimension);
    receiveLogJScrollPane.setMinimumSize(logJScrollPanelDimension);
    receiveLogJScrollPane.setMaximumSize(logJScrollPanelDimension);
    
    receiveLogJPanel.add(receiveJLabel);
    receiveLogJPanel.add(Box.createVerticalStrut(10));
    receiveLogJPanel.add(receiveLogJScrollPane);
    this.receiveSuccessfulJLabel.setAlignmentX(0.5F);
    receiveLogJPanel.add(this.receiveSuccessfulJLabel);
    
    Dimension logJPanelDimension = new Dimension(650, 260);
    JPanel logJPanel = new JPanel();
    BoxLayout logBoxLayout = new BoxLayout(logJPanel, 0);
    logJPanel.setLayout(logBoxLayout);
    
    logJPanel.setPreferredSize(logJPanelDimension);
    logJPanel.setMinimumSize(logJPanelDimension);
    logJPanel.setMaximumSize(logJPanelDimension);
    
    logJPanel.add(sendLogJPanel);
    logJPanel.add(Box.createHorizontalStrut(10));
    logJPanel.add(receiveLogJPanel);
    
    Dimension receiveTextDimension = new Dimension(650, 100);
    
    JPanel receiveTextJPanel = new JPanel();
    BoxLayout receiveTextBoxLayout = new BoxLayout(receiveTextJPanel, 0);
    receiveTextJPanel.setLayout(receiveTextBoxLayout);
    
    this.receivedTextJTextArea = new JTextArea();
    JScrollPane receivedTextJScrollPane = new JScrollPane(this.receivedTextJTextArea);
    
    receiveTextJPanel.add(Box.createHorizontalStrut(10));
    receiveTextJPanel.add(receivedTextJScrollPane);
    receiveTextJPanel.add(Box.createHorizontalStrut(10));
    
    receiveTextJPanel.setPreferredSize(receiveTextDimension);
    receiveTextJPanel.setMaximumSize(receiveTextDimension);
    receiveTextJPanel.setMinimumSize(receiveTextDimension);
    
    JPanel receivedPacketsJPanel = new JPanel();
    BoxLayout receivedPacketsBoxLayout = new BoxLayout(receivedPacketsJPanel, 0);
    receivedPacketsJPanel.setLayout(receivedPacketsBoxLayout);
    
    JLabel receivedPacketsJLabel = new JLabel("Received Packets");
    
    receivedPacketsJLabel.setHorizontalAlignment(2);
    
    Dimension receivedPacketsDimension = new Dimension(650, 30);
    receivedPacketsJLabel.setHorizontalTextPosition(4);
    
    receivedPacketsJPanel.add(Box.createHorizontalStrut(10));
    receivedPacketsJPanel.add(receivedPacketsJLabel);
    receivedPacketsJPanel.add(Box.createHorizontalStrut(10));
    
    receivedPacketsJPanel.setPreferredSize(receivedPacketsDimension);
    receivedPacketsJPanel.setMinimumSize(receivedPacketsDimension);
    receivedPacketsJPanel.setMaximumSize(receivedPacketsDimension);
    
    JPanel fileChooserSaveJPanel = new JPanel();
    
    JLabel saveFileJLabel = new JLabel("Save to File: ");
    this.fileNameSaveJTextField = new JTextField();
    this.fileNameSaveJTextField.setEditable(false);
    
    this.browseSaveJButton = new JButton("Browse");
    this.browseSaveJButton.addActionListener(this);
    this.browseSaveJButton.setEnabled(false);
    this.clearJButton = new JButton("Clear");
    this.clearJButton.addActionListener(this);
    this.clearJButton.setEnabled(false);
    
    BoxLayout fileChooserSaveBoxLayout = new BoxLayout(fileChooserSaveJPanel, 0);
    fileChooserSaveJPanel.setLayout(fileChooserSaveBoxLayout);
    
    fileChooserSaveJPanel.add(Box.createHorizontalStrut(10));
    fileChooserSaveJPanel.add(saveFileJLabel);
    fileChooserSaveJPanel.add(Box.createHorizontalStrut(10));
    fileChooserSaveJPanel.add(this.fileNameSaveJTextField);
    fileChooserSaveJPanel.add(Box.createHorizontalStrut(10));
    fileChooserSaveJPanel.add(this.browseSaveJButton);
    fileChooserSaveJPanel.add(Box.createHorizontalStrut(10));
    fileChooserSaveJPanel.add(this.clearJButton);
    fileChooserSaveJPanel.add(Box.createHorizontalStrut(10));
    
    fileChooserSaveJPanel.setPreferredSize(srcDestJPanelDimension);
    fileChooserSaveJPanel.setMaximumSize(srcDestJPanelDimension);
    fileChooserSaveJPanel.setMinimumSize(srcDestJPanelDimension);
    
    JPanel mainJPanel = new JPanel();
    BoxLayout frameBoxLayout = new BoxLayout(mainJPanel, 1);
    mainJPanel.setLayout(frameBoxLayout);
    mainJPanel.add(Box.createVerticalGlue());
    mainJPanel.add(srcDestJPanel);
    mainJPanel.add(Box.createVerticalStrut(10));
    mainJPanel.add(fileChooserJPanel);
    mainJPanel.add(Box.createVerticalStrut(10));
    mainJPanel.add(fileInfoJPanel);
    
    mainJPanel.add(logJPanel);
    
    mainJPanel.add(receivedPacketsJPanel);
    mainJPanel.add(receiveTextJPanel);
    mainJPanel.add(Box.createVerticalStrut(10));
    mainJPanel.add(fileChooserSaveJPanel);
    mainJPanel.add(Box.createVerticalGlue());
    
    setLayout(new BorderLayout());
    add(mainJPanel, "Center");
    setVisible(true);
    setDefaultCloseOperation(3);
    setSize(800, 600);
  }
  
  private static final Exception sourceMacOutOfBound = new Exception("Source Mac Out Of Bound");
  private static final Exception destinationMacOutOfBound = new Exception("Destination Mac Out Of Bound");
  private Controller controller;
  
  public void actionPerformed(ActionEvent e)
  {
    if (e.getSource().equals(this.connectJButton))
    {
      try
      {
        byte sourceMac = Byte.parseByte(this.srcJTextField.getText(), 16);
        byte destinationMac = Byte.parseByte(this.destJTextField.getText(), 16);
        if ((sourceMac < 1) || (sourceMac > Byte.MAX_VALUE)) {
          throw sourceMacOutOfBound;
        }
        if ((destinationMac < 1) || (destinationMac > Byte.MAX_VALUE)) {
          throw destinationMacOutOfBound;
        }
        System.out.println("Creating controller ...");
        this.controller = new Controller(sourceMac, destinationMac, this);
        this.srcJTextField.setEnabled(false);
        this.destJTextField.setEnabled(false);
        this.connectJButton.setEnabled(false);
        this.browseSendJButton.setEnabled(true);
      }
      catch (UnknownHostException ex)
      {
        JOptionPane.showMessageDialog(this, "Unknown Host. Can't start the application. Make sure SimSwitch is running properly", "Unknown Host", 1);
      }
      catch (SocketException ex)
      {
        JOptionPane.showMessageDialog(this, "Socket Busy. Can't start the application. Make sure SimSwitch is running properly", "Socket Busy", 1);
      }
      catch (IOException ex)
      {
        JOptionPane.showMessageDialog(this, "IO Problem. Can't start the application. Make sure SimSwitch is running properly", "IO Problem", 1);
      }
      catch (NumberFormatException numberFormatException)
      {
        JOptionPane.showMessageDialog(this, "Invalid Mac Address. Illegal Number Format. Should be a Hex value between 0x01 to 0x7F", "Invalid Mac Address", 1);
      }
      catch (Exception ex)
      {
        if (ex.equals(sourceMacOutOfBound)) {
          JOptionPane.showMessageDialog(this, "Invalid Source Mac Address. Should be a Hex value between 0x01 to 0x7F", "Invalid Mac Address", 1);
        } else if (ex.equals(destinationMacOutOfBound)) {
          JOptionPane.showMessageDialog(this, "Invalid Destination Mac Address. Should be a Hex value between 0x01 to 0x7F", "Invalid Mac Address", 1);
        }
      }
    }
    else if (e.getSource().equals(this.browseSendJButton))
    {
      JFileChooser fileChooser = new JFileChooser();
      
      int returnVal = fileChooser.showOpenDialog(this);
      if (returnVal == 0) {
        try
        {
          File file = fileChooser.getSelectedFile();
          this.fileNameJTextField.setText(file.getAbsolutePath());
          this.fileInfoJLabel.setText("File size: " + file.length() + " bytes");
          this.sendJButton.setEnabled(true);
          this.controller.setSendFile(file);
        }
        catch (IOException ex)
        {
          Logger.getLogger(UserInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
    else if (e.getSource().equals(this.sendJButton))
    {
      this.browseSendJButton.setEnabled(false);
      this.sendJButton.setEnabled(false);
      this.controller.sendFrames();
    }
    else if (e.getSource().equals(this.browseSaveJButton))
    {
      JFileChooser fileChooser = new JFileChooser();
      int returnVal = fileChooser.showSaveDialog(this);
      if (returnVal == 0)
      {
        File file = fileChooser.getSelectedFile();
        this.fileNameSaveJTextField.setText(file.getAbsolutePath());
        this.clearJButton.setEnabled(true);
        this.controller.setReceiveFile(file);
        try
        {
          this.controller.saveReceivedFile();
        }
        catch (FileNotFoundException ex)
        {
          JOptionPane.showMessageDialog(this, "File not found. Set a proper File", "File not found", 1);
        }
        catch (IOException ex)
        {
          JOptionPane.showMessageDialog(this, "File write problem. Set a proper File", "File write problem", 1);
        }
      }
    }
    else if (e.getSource().equals(this.clearJButton))
    {
      this.fileNameSaveJTextField.setText("");
    }
  }
  
  public void addSendLog(String logMessage)
  {
    this.sendDefaultListModel.addElement(logMessage);
  }
  
  public void addReceivedLog(String logMessage)
  {
    this.receiveDefaultListModel.addElement(logMessage);
  }
  
  public void sendEOFSuccessful()
  {
    this.sendSuccessfulJLabel.setText("Send Successful");
  }
  
  public void receivedEOFSuccessful()
  {
    this.receiveSuccessfulJLabel.setText("Receive successful");
    this.browseSaveJButton.setEnabled(true);
  }
  
  public void appendPacket(byte[] packet)
  {
    this.receivedTextJTextArea.append(Printer.byteArrayToHexString(packet) + "\n");
  }
  
  public static void main(String[] args)
  {
    UserInterface ui = new UserInterface();
  }
}

