/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package byteArrayStuffs;

import java.util.Iterator;

/**
 *
 * @author Farhan
 */
public class ByteArrayBitIterable implements Iterable<Boolean> 
{
    private final byte[] array;

    public ByteArrayBitIterable(byte[] ara)
    {
        array=ara;
    }

    @Override
    public Iterator<Boolean> iterator() 
    {
        return new Iterator<Boolean>() 
        {
            private int bitIndex=0;
            private int arrayIndex=0;

            @Override
            public boolean hasNext() 
            {
                return (arrayIndex<array.length)&&(bitIndex<8);
            }

            @Override
            public Boolean next() 
            {
                Boolean val=(array[arrayIndex]>>(7-bitIndex)&1)==1;
                bitIndex++;
                if (bitIndex==8) {
                    bitIndex=0;
                    arrayIndex++;
                }
                return val;
            }
        };
    }
}
