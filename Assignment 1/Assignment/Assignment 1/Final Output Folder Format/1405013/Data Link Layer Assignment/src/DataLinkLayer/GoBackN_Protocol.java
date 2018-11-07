package DataLinkLayer;

import UI.BitPattern;
import UI.LogWindow;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Random;

public class GoBackN_Protocol {
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private int windowSize;
    private ArrayList<byte[]> chunks;
    private Socket socket;
    private int next_frame_to_send;
    private int ack_expected;
    private int seq_expected;
    private LogWindow logWindow;
    private int numberOfFrames;
    private boolean allAcknowledgementsReceived;
    private String fileID;
    private boolean loseFrame;
    private boolean showFrames;

    private int timeout;
    private boolean introduce_bad_lost_frame;

    public GoBackN_Protocol(ObjectInputStream ois, ObjectOutputStream oos,
                            int windowSize, ArrayList<byte[]> chunks, Socket socket,
                            LogWindow logWindow, String fileID, int timeout) {
        this.ois = ois;
        this.oos = oos;
        this.windowSize = windowSize;
        this.chunks = chunks;
        this.socket = socket;
        this.logWindow = logWindow;
        this.fileID = fileID;
        this.timeout = timeout;
        numberOfFrames = chunks.size();
        next_frame_to_send = 0;
        ack_expected = 0;
        allAcknowledgementsReceived = false;
        introduce_bad_lost_frame = false;
        loseFrame = false;
    }

    public GoBackN_Protocol(ObjectInputStream ois, ObjectOutputStream oos,
                            LogWindow logWindow, String fileID) {
        this.ois = ois;
        this.oos = oos;
        this.logWindow = logWindow;
        this.fileID = fileID;
        seq_expected = 0;
        introduce_bad_lost_frame = false;
        loseFrame = false;
    }

    public void setShowFrames(boolean showFrames) {
        this.showFrames = showFrames;
    }


    public void setIntroduce_bad_lost_frame(boolean introduce_bad_lost_frame) {
        this.introduce_bad_lost_frame = introduce_bad_lost_frame;
    }

    private byte[] makeBadFrame(byte[] frame) {
        Random rand = new Random();
        int size = frame.length;
        int badIndex = rand.nextInt()%size;
        if(badIndex < 0) badIndex = -badIndex;
        int badByte = rand.nextInt()%(0xFF + 1);
        frame[badIndex] = (byte)(badByte & 0xFF);
        return frame;
    }

    private boolean verifyData(byte[] dataFrame) {
        boolean a = (dataFrame[0] == 0x7E) && (dataFrame[dataFrame.length - 1] == 0x7E);
        byte[] withoutDelim = FrameProcessor.removeFrameDelimiter(dataFrame);
        byte[] deStuffedDataFrame = FrameProcessor.bitDeStuff(withoutDelim);
        boolean b = (int)deStuffedDataFrame[1] == seq_expected;
        boolean c = deStuffedDataFrame[0] == (byte)0xEE;
        boolean d = FrameProcessor.verifyCheckSum(deStuffedDataFrame);
        return (a && b && c && d);
    }

    private boolean verifyAck(byte[] ackFrame) {
        boolean a = (ackFrame[0] == 0x7E) && (ackFrame[ackFrame.length - 1] == 0x7E);
        byte[] withoutDelim = FrameProcessor.removeFrameDelimiter(ackFrame);
        byte[] deStuffedAckFrame = FrameProcessor.bitDeStuff(withoutDelim);
        boolean b = (int) deStuffedAckFrame[1] == ack_expected;
        boolean c = FrameProcessor.verifyCheckSum(deStuffedAckFrame);
        boolean d = deStuffedAckFrame[0] == (byte) 0xFE;
        return (a && b && c && d);
    }

    private byte[] makeDataFrame(byte[] frame) {
        int frameSize = frame.length;
        if(showFrames) {
            logWindow.appendToFrameLog("The Data Frame for Seq No. " + next_frame_to_send + ":\n");
            BitPattern.printBits(frame, logWindow);
        }
        byte[] frameBeforeChecksum = new byte[frameSize + 2];
        frameBeforeChecksum[0] = (byte) 0xEE; //adding data/ack frame for data
        frameBeforeChecksum[1] = (byte) (next_frame_to_send & 0xFF); //adding seq no.
        System.arraycopy(frame, 0, frameBeforeChecksum, 2, frameSize);
        byte[] frameAfterChecksum = FrameProcessor.addCheckSum(frameBeforeChecksum);
        if(showFrames) {
            logWindow.appendToFrameLog("All the extra bytes added(Data Frame No. " + next_frame_to_send
                    + "):\n");
            BitPattern.printBits(frameAfterChecksum, logWindow);
        }
        byte[] frameAfterStuffing = FrameProcessor.bitStuff(frameAfterChecksum);
        if(showFrames) {
            logWindow.appendToFrameLog("Stuffed Data Frame No. " + next_frame_to_send + ":\n");
            BitPattern.printBits(frameAfterStuffing, logWindow);
        }
        return FrameProcessor.addFrameDelimiter(frameAfterStuffing);
    }


    private byte[] makeAckFrame() {
        byte[] frame = new byte[2];
        frame[0] = (byte)0xFE; //adding data/ack frame for ack
        frame[1] = (byte)(seq_expected & 0xFF); //adding ack no.
        if(showFrames) {
            logWindow.appendToFrameLog("The Ack Frame No. " + seq_expected + ":\n");
            BitPattern.printBits(frame, logWindow);
        }
        byte[] checksumAdded = FrameProcessor.addCheckSum(frame);
        if(showFrames) {
            logWindow.appendToFrameLog("Checksum Added(Ack Frame No. " + seq_expected + "):\n");
            BitPattern.printBits(checksumAdded, logWindow);
        }
        byte[] stuffedFrame = FrameProcessor.bitStuff(checksumAdded);
        if(showFrames) {
            logWindow.appendToFrameLog("Stuffed Ack Frame No. " + seq_expected + ":\n");
            BitPattern.printBits(stuffedFrame, logWindow);
        }
        return FrameProcessor.addFrameDelimiter(stuffedFrame);
    }


    public boolean sendFrames() {
        while (!allAcknowledgementsReceived) {
            try {
                socket.setSoTimeout(timeout);
                if (next_frame_to_send < numberOfFrames) {
                    for (int i = 0; i < windowSize; ++i) {
                        byte[] frame_to_send = makeDataFrame(chunks.get(next_frame_to_send));

                        //if introduce_bad_lost_frame is true, random number will be generated
                        //to ask the user if he wants to introduce a bad frame or a lost frame
                        //with a probability of 1/4 and to determine if the frame will be bad or
                        //lost again random number is generated.
                        if(introduce_bad_lost_frame) {
                            Random rand = new Random();
                            int chance = rand.nextInt() % 8;
                            if (chance == 0) {
                                int bad_or_lost = rand.nextInt() % 2; //0 for bad, 1 for lost
                                if (bad_or_lost == 0) {
                                    int ans = JOptionPane.showConfirmDialog(null,
                                            "Do you want to introduce" +
                                                    " a bad frame for frame no. " + next_frame_to_send
                                            + "?",
                                            null,
                                            JOptionPane.YES_NO_OPTION);
                                    if(ans == JOptionPane.YES_OPTION) {
                                        frame_to_send = makeBadFrame(frame_to_send);
                                    }
                                }
                                else {
                                    int ans = JOptionPane.showConfirmDialog(null,
                                            "Do you want to lose frame no. "
                                                    + next_frame_to_send + "?", null,
                                            JOptionPane.YES_NO_OPTION);
                                    if(ans == JOptionPane.YES_OPTION) {
                                        loseFrame = true;
                                    }
                                }
                            }
                        }

                        if(!loseFrame) {
                            oos.writeObject(frame_to_send);
                            logWindow.appendToLog("Frame " + next_frame_to_send + " of size " +
                                    frame_to_send.length + " for File ID: " + fileID + "\n");
                        }
                        next_frame_to_send++;
                        if (next_frame_to_send == numberOfFrames) break;
                        loseFrame = false;
                    }
                }
                while (ack_expected < next_frame_to_send){
                    byte[] ackFrame = (byte[]) ois.readObject();
                    if (verifyAck(ackFrame)) {
                        if(showFrames) {
                            logWindow.appendToFrameLog("Received Ack Frame No. " + ack_expected + ":\n");
                            BitPattern.printBits(ackFrame, logWindow);
                        }
                        byte[] withoutDelim = FrameProcessor.removeFrameDelimiter(ackFrame);
                        if(showFrames) {
                            logWindow.appendToFrameLog("After removing frame delim(Ack Frame No. " +
                                    ack_expected + "):\n");
                            BitPattern.printBits(withoutDelim, logWindow);
                        }
                        byte[] deStuffed = FrameProcessor.bitDeStuff(withoutDelim);
                        if(showFrames) {
                            logWindow.appendToFrameLog("DeStuffed Ack Frame No. " + ack_expected + ":\n");
                            BitPattern.printBits(deStuffed, logWindow);
                        }
                        logWindow.appendToLog("Ack No. " + ack_expected + " received\n");
                        ack_expected++;
                        if (ack_expected == numberOfFrames) {
                            allAcknowledgementsReceived = true;
                            break;
                        }
                    }
                    else {
                        logWindow.appendToLog("Problem with the received ack frame, " +
                                "frame discarded\n");
                    }
                }
            } catch (SocketTimeoutException e) {
                logWindow.appendToLog("Sending frame " + ack_expected + " again\n");
                next_frame_to_send = ack_expected;
            } catch (IOException e) {
                logWindow.appendToLog(e.toString() + ": cannot write to output stream" +
                        " or read from input stream\n");
                return false;
            } catch (ClassNotFoundException e) {
                logWindow.appendToLog(e.toString() + ": cannot cast to the desired type\n");
                return false;
            }
        }
        return true;
    }

    public ArrayList<byte[]> receiveFrames() {
        ArrayList<byte[]> retArray = new ArrayList<>();
        while (true) {
            try {
                Object object = ois.readObject();
                if (object instanceof String) {
                    String msg = (String) object;
                    if (msg.compareTo("No chunk left. Task complete.") == 0) {
                        logWindow.appendToLog(msg + "\n");
                        return retArray;
                    }
                }
                else if (object instanceof byte[]) {
                    byte[] frame = (byte[]) object;
                    if(verifyData(frame)) {
                        if(showFrames) {
                            logWindow.appendToFrameLog("Received Data Frame No. " + seq_expected + ":\n");
                            BitPattern.printBits(frame, logWindow);
                        }
                        byte[] withoutDelim = FrameProcessor.removeFrameDelimiter(frame);
                        if(showFrames) {
                            logWindow.appendToFrameLog("After removing frame delim(Data Frame No. " +
                                    seq_expected + "):\n");
                            BitPattern.printBits(withoutDelim, logWindow);
                        }
                        byte[] deStuffedFrame = FrameProcessor.bitDeStuff(withoutDelim);
                        if(showFrames) {
                            logWindow.appendToFrameLog("DeStuffed Data Frame No. " + seq_expected + ":\n");
                            BitPattern.printBits(deStuffedFrame, logWindow);
                        }
                        byte[] payload = new byte[deStuffedFrame.length - 3];
                        System.arraycopy(deStuffedFrame, 2, payload, 0,
                                deStuffedFrame.length - 3);
                        if(showFrames) {
                            logWindow.appendToFrameLog("Main Data Frame No. " + seq_expected + ":\n");
                            BitPattern.printBits(payload, logWindow);
                        }
                        retArray.add(payload);
                        byte[] ack_to_send = makeAckFrame();
                        logWindow.appendToLog("Frame of Seq No. " + seq_expected + " received\n");
                        logWindow.appendToLog("Sending Ack for frame " + seq_expected + "\n");
                        oos.writeObject(ack_to_send);
                        seq_expected++;
                    }
                    else {
                        logWindow.appendToLog("Problem with the received data " +
                                "frame, frame discarded\n");
                    }
                }
            } catch (IOException e) {
                logWindow.appendToLog(e.toString() + ": cannot write to output stream" +
                        " or read from input stream\n");
                return retArray;
            } catch (ClassNotFoundException e) {
                logWindow.appendToLog(e.toString() + ": cannot cast to the desired type\n");
                return retArray;
            }
        }
    }
}