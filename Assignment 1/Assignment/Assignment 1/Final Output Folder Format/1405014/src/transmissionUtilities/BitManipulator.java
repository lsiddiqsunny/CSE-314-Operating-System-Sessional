package transmissionUtilities;

import java.util.ArrayList;

public class BitManipulator {
    private byte oldCheckSum;
    private byte newCheckSum;
    private byte oldSequence;
    private byte newSequence;

    public BitManipulator ()
    {
        oldCheckSum = 0b00000000;
        newCheckSum = 0b00000000;
        oldSequence = 0b00000000;
        newSequence = 0b00000000;
    }
    
    public byte[] bitStuffer(byte[] inputByteArray, int length, int sequenceNumber)
    {
        String byteString = "";
        byte checkSum = (byte)0b00000000;
        int counter = 0;

        for (int i = 0; i < length; i++)
        {
            byte currentByte = inputByteArray[i];
            for (int j = 7; j >= 0; j--)
            {
                checkSum ^= (currentByte & (1 << j));
                if ((currentByte & (1 << j)) != 0)
                {
                    counter++;
                    if (counter == 5)
                    {
                        counter = 0;
                        byteString += "10";
                    }
                    else
                    {
                        byteString += "1";
                    }
                }
                else
                {
                    counter = 0;
                    byteString += "0";
                }
            }
        }

        if (byteString.length () % 8 != 0)
        {
            for (int i = 0; i < byteString.length () % 8; i++)
            {
                byteString = "0" + byteString;
            }
        }

        ArrayList<Integer> arrayList = new ArrayList<> ();
        arrayList.add (Integer.parseInt ("01111110", 2));
        arrayList.add (Integer.parseInt (Byte.toString (checkSum)));
        arrayList.add (sequenceNumber);

        for(String str : byteString.split("(?<=\\G.{8})"))
            arrayList.add(Integer.parseInt (str, 2));

        arrayList.add (Integer.parseInt ("01111110", 2));

        byte[] outputByteArray = new byte[arrayList.size ()];

        for (int i = 0; i < arrayList.size (); i++)
        {
            outputByteArray[i] = arrayList.get (i).byteValue ();
        }

        return outputByteArray;
    }

    public byte[] bitDestuffer(byte[] inputByteArray, int length, int sequenceNumber)
    {
        String byteString = "";
        int counter = 0;

        oldCheckSum = inputByteArray[1];

        oldSequence = inputByteArray[2];

        newSequence = (byte)sequenceNumber;

        for (int i = 3; i < length - 1; i++)
        {
            byte currentByte = inputByteArray[i];
            for (int j = 7; j >= 0; j--)
            {
                if ((currentByte & (1 << j)) != 0)
                {
                    counter++;
                    byteString += "1";
                }
                else
                {
                    if(counter == 5)
                    {
                        counter = 0;
                        continue;
                    }

                    counter = 0;
                    byteString += "0";
                }
            }
        }

        if (byteString.length () % 8 != 0)
        {
            byteString = byteString.substring (byteString.length () % 8);
        }

        ArrayList<Integer> arrayList = new ArrayList<> ();

        for(String str : byteString.split("(?<=\\G.{8})"))
            arrayList.add(Integer.parseInt (str, 2));

        byte[] outputByteArray = new byte[arrayList.size ()];

        newCheckSum = (byte)0b00000000;

        for (int i = 0; i < arrayList.size (); i++)
        {
            outputByteArray[i] = arrayList.get (i).byteValue ();
            newCheckSum ^= outputByteArray[i];
        }

        return outputByteArray;
    }

    public boolean hasCheckSumError(byte oldCheckSum, byte newCheckSum)
    {
        return oldCheckSum != newCheckSum;
    }

    public boolean hasSequenceError(byte oldSequence, byte newSequence) { return oldSequence != newSequence; }

    public byte[] getChunkInformation() {
        return new byte[]{oldCheckSum, newCheckSum, oldSequence, newSequence};
    }

    public void resetValues()
    {
        oldCheckSum = 0b00000000;
        newCheckSum = 0b00000000;
        oldSequence = 0b00000000;
        newSequence = 0b00000000;
    }

    public void showByteFrameBitDeStuffed(byte[] byteArray) {
        System.out.println ("Payload:");

        for (int i = 0; i < byteArray.length; i++) {
            for (int j = 0; j < 16; j++)
            {
                System.out.printf (String.format("%8s", Integer.toBinaryString(byteArray[i] & 0xFF)).replace(' ', '0') + " ");
            }
            System.out.println ("");
        }
    }

    public void showByteFrameBitStuffed(byte[] byteArray) {
        System.out.println ("Delimiter: " + String.format("%8s", Integer.toBinaryString(byteArray[0] & 0xFF)).replace(' ', '0'));

        System.out.println ("Check sum: " + String.format("%8s", Integer.toBinaryString(byteArray[1] & 0xFF)).replace(' ', '0'));

        System.out.println ("Sequence number: " + String.format("%8s", Integer.toBinaryString(byteArray[2] & 0xFF)).replace(' ', '0'));

        System.out.println ("Payload:");

        for (int i = 3; i < byteArray.length - 1; i++) {
            for (int j = 0; j < 16; j++)
            {
                System.out.printf (String.format("%8s", Integer.toBinaryString(byteArray[i] & 0xFF)).replace(' ', '0') + " ");
            }
            System.out.println ("");
        }

        System.out.println ("Delimiter: " + String.format("%8s", Integer.toBinaryString(byteArray[byteArray.length - 1] & 0xFF)).replace(' ', '0'));
    }
}
