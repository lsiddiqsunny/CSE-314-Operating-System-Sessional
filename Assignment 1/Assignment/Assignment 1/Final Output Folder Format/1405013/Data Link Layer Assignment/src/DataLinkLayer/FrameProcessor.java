package DataLinkLayer;

class FrameProcessor {
    private static StringBuilder stuffAndConvertToString(byte[] array) {
        StringBuilder result = new StringBuilder("");
        int count1 = 0;
        for(byte tmp : array) {
            int tmpInt = (int)tmp;
            for(int j = 0; j < 8; ++j) {
                int bit = tmpInt & 0x80;
                bit >>= 7;
                tmpInt <<= 1;
                result.append(bit);
                if(bit == 0) {
                    count1 = 0;
                }
                else if(bit == 1) {
                    count1++;
                    if(count1 == 5) {
                        result.append(0);
                        count1 = 0;
                    }
                }
            }
        }
        return result;
    }

    private static StringBuilder deStuffAndConvertToString(byte[] array) {
        StringBuilder pattern = new StringBuilder("");
        int count1 = 0;
        boolean ignore = false;
        for(byte tmp : array) {
            int tmpInt = (int)tmp;
            for(int j = 0; j < 8; ++j) {
                int bit = tmpInt & 0x80;
                bit >>= 7;
                tmpInt <<= 1;
                if(ignore) {
                    ignore = false;
                    continue;
                }
                pattern.append(bit);
                if(bit == 0) count1 = 0;
                else if(bit == 1) {
                    count1++;
                    if(count1 == 5) {
                        ignore = true;
                        count1 = 0;
                    }
                }
            }
        }
        return pattern;
    }

    private static StringBuilder roundToBytes(StringBuilder pattern) {
        int patternSize = pattern.length();
        int need_to_add_bits_count = 0;
        if(patternSize % 8 != 0) {
            need_to_add_bits_count = 8 - patternSize%8;
        }
        StringBuilder retPattern = new StringBuilder("");
        for(int i = 0; i < need_to_add_bits_count; ++i) {
            retPattern.append(0);
        }
        retPattern.append(pattern);
        return retPattern;
    }


    private static byte[] convertStringToBytes(StringBuilder pattern) {
        int patternSize = pattern.length();
        byte[] resArray = new byte[patternSize/8];
        int count_for_bits = 0;
        int newByte = 0;
        int byteCount = 0;
        for(int i = 0; i < patternSize; ++i) {
            newByte <<= 1;
            if(pattern.charAt(i) == '1') {
                newByte += 1;
            }
            count_for_bits++;
            if(count_for_bits == 8) {
                count_for_bits = 0;
                resArray[byteCount] = (byte)(newByte & 0xFF);
                byteCount++;
                newByte = 0;
            }
        }
        return resArray;
    }


    static boolean verifyCheckSum(byte[] array) {
        int size = array.length;
        int checksumByte = (int)array[size - 1];
        int calculatedChecksum = (int)array[0];
        for(int i = 1; i < size - 1; ++i) {
            calculatedChecksum ^= (int)array[i];
        }
        return (calculatedChecksum == checksumByte);
    }


    static byte[] addCheckSum(byte[] array) {
        int size = array.length;
        int checksum = (int)array[0];
        for(int i = 1; i < size; ++i) {
            checksum ^= (int)array[i];
        }
        byte checksumByte = (byte)(0xFF & checksum);
        byte[] retArray = new byte[size + 1];
        System.arraycopy(array, 0, retArray, 0, size);
        retArray[size] = checksumByte;
        return retArray;
    }


    static byte[] bitStuff(byte[] array) {
        StringBuilder result = stuffAndConvertToString(array);
        StringBuilder ultimateResult = roundToBytes(result);
        return convertStringToBytes(ultimateResult);
    }



    static byte[] bitDeStuff(byte[] array) {
        StringBuilder result = deStuffAndConvertToString(array);
        StringBuilder ultimateResult = roundToBytes(result);
        byte[] byteArray = convertStringToBytes(ultimateResult);
        if(byteArray[0] == 0x00) {
            int retArraySize = byteArray.length - 1;
            byte[] retArray = new byte[retArraySize];
            System.arraycopy(byteArray, 1, retArray, 0, retArraySize);
            return retArray;
        }
        return byteArray;
    }

    static byte[] addFrameDelimiter(byte[] array) {
        int size = array.length;
        byte[] retArray = new byte[size + 2];
        retArray[0] = (byte)0x7E;
        retArray[size + 1] = (byte)0x7E;
        System.arraycopy(array, 0, retArray, 1, size);
        return retArray;
    }


    static byte[] removeFrameDelimiter(byte[] array) {
        int size = array.length;
        byte[] retArray = new byte[size - 2];
        System.arraycopy(array, 1, retArray, 0, size - 2);
        return retArray;
    }

}
