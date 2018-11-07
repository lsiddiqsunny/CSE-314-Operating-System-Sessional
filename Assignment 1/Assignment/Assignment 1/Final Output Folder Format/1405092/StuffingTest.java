
import java.util.Arrays;


/**
 *
 * 
 */
public class StuffingTest
{
    public static byte[] Stuffed_bit(byte[] x){
        System.out.println("original byte array\t"+Arrays.toString(x));
        int k=0; 
        String bit_in_byte = "" ;
          int j,temp;
          j=x.length;
          String allpattern = "";
        while(k<j){
             temp = x[k] & 0b11111111;
            bit_in_byte = Integer.toBinaryString(temp);
            bit_in_byte = String.format("%8s",bit_in_byte).replace(' ', '0');
            allpattern= allpattern+bit_in_byte;
                k++;
             }
           
        
        System.out.println("allpattern\t"+allpattern);

        char [] pattern = allpattern.toCharArray();
        int sum=0;
        String stuffedstring = "";
     
        stuffedstring = allpattern.replaceAll("111111", "01111110");

        char [] chng = stuffedstring.toCharArray();
        System.out.println("stuffed\t\t"+stuffedstring);

        byte[] ch_byte=stuffedstring.getBytes();
        
        int l= chng.length;
           
        int ittn = l/8;
        
        int len= ittn*8;
        if((len)=!l){
           ittn=ittn+1;
        }
          
        else {
               len==l;
             }

         byte [] form_in_byte;
          form_in_byte  = new byte[ittn];
       
      
        
     
         int index=0,c,sum,b;
            int a=0;
         while(a<ittn){
                c=7;
               sum=0;
           for( b=(a*8);(b<((a+1)*8))&&(b<stuffedstring.length());b+=1,c-=1){
                    if(stuffedstring.charAt(b)=='1'){
                        sum=sum+((int)Math.pow(2,k));
                    }
                }
                  form_in_byte[index++]=(byte)sum;
             System.out.println("sent in byte array\t"+Arrays.toString(form_in_byte));

              return form_in_byte;
                a++;
           } 
             
}
    
    public static byte[] DeStuffed_bit(byte[] x){
        String allpattern = "";
        int add_zero = 0;
        
        String bit_in_byte = "" ;
        
        System.out.println("received byte array\t"+Arrays.toString(x));

        /*for(int i=0;i<b.length;i++){
            int temporaryInt = b[i] & 0xFF;
            bit_in_byte = Integer.toBinaryString(temporaryInt);
            bit_in_byte = String.format("%8s",bit_in_byte).replace(' ', '0');
            allpattern+= bit_in_byte;
        }
*/
          int j,temp;
          j=x.length;
          int k=0;
        while(k<j){
             temp = x[k] & 0b11111111;
            bit_in_byte = Integer.toBinaryString(temp);
            bit_in_byte = String.format("%8s",bit_in_byte).replace(' ', '0');
            allpattern= allpattern+bit_in_byte;
                k++;
             }          
      

        int sum=0;
        char [] pattern = allpattern.toCharArray();
        
        String stuffedstring = "";
        
        String main_str = allpattern;
        String str_found;
         str_found= = "01111110";
        add_zero = 0;
        while (main_str.contains(str_found)){
            main_str = main_str.replaceFirst(str_found, "-");
            add_zero=add_zero+1;
        }
        
        stuffedstring = allpattern.replaceAll("01111110", "111111");
        System.out.println("allpattern\t"+allpattern);
        System.out.println("stuffedstring\t"+stuffedstring);
        
        
        /*char [] chararray = stuffedstring.toCharArray();
        int neededToRemove = 8 -(add_zero%8);
        if(neededToRemove==8) neededToRemove=0;
       
        String effectiveMessage = "";
        
        effectiveMessage = stuffedstring.substring(0,chararray.length-neededToRemove);
        System.out.println("not effectve\t"+stuffedstring);
        int length1 = stuffedstring.length() - neededToRemove;
        System.out.println("length "+length1+"length "+stuffedstring.length()+"char length "+chararray.length+"needed to remove "+neededToRemove);

        System.out.println("effectiveMsg\t"+stuffedstring.substring(0,length1));
   */
       char [] chng = stuffedstring.toCharArray();
        System.out.println("stuffed\t\t"+stuffedstring);

        byte[] ch_byte=stuffedstring.getBytes();
        
        int l= chng.length;
           
        int ittn = l/8;
        
        int len= ittn*8;
        if((len)=!l){
           ittn=ittn+1;
        }
          
        else {
               len==l;
             }

         byte [] form_in_byte;
          form_in_byte  = new byte[ittn];
       
      
        
     
         int index=0,c,sum,b;
            int a=0;
         while(a<ittn){
                c=7;
               sum=0;
           for( b=(a*8);(b<((a+1)*8))&&(b<stuffedstring.length());b+=1,c-=1){
                    if(stuffedstring.charAt(b)=='1'){
                        sum=sum+((int)Math.pow(2,k));
                    }
                }
                  form_in_byte[index++]=(byte)sum;
             System.out.println("sent in byte array\t"+Arrays.toString(form_in_byte));

              return form_in_byte;
                a++;
           } 
             
}
        
   
    public static void main(String args[]){
        
        int[] arr= new int[]{0, 0, 7, 109, 126, 103, 31, 109,31, 101,126, 108, 126, 45,126};;
        //byte[] b,c,d;
      
        byte[] p = new byte[arr.length];
        
           int k=0;
          while(k<arr.length){
                p[k]=(byte)arr[k];
                 k++;
            }
       byte x;
         x=Stuffed_bit(p);
        System.out.println("Bit Destuffing");
        byte y;
         y=DeStuffed_bit(x);
        
    }
}
