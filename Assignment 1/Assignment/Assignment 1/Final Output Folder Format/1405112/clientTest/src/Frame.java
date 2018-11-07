import java.util.BitSet;

public class Frame
{
    BitSet bitSet;

    String toStringFromBit()
    {
        String s="";
        for (int i = 0; i <bitSet.length() ; i++)
        {
            if(bitSet.get(i))
            {
                s=s+"1";
            }
            else
                s=s+"0";
        }
        return s;

    }
}
