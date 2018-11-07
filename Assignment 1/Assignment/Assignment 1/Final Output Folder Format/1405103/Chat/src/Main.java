import java.*;
import java.lang.Object;
import java.util.StringTokenizer;

public class Main {
	    public static String myParser(String frame){
        	return frame.substring(8,frame.lastIndexOf("01111110"));
        }

    public static void main(String[] args) {
        System.out.println("Hello World!");
        String s ="01111110101011000111111000";
       // StringTokenizer st = new StringTokenizer(s);
     //	while (st.hasMoreTokens()) {
     		//String ss = st.nextToken("he");
     		int a=s.indexOf("01111110");
     		int b=s.lastIndexOf("01111110");
     		String ss=s.substring(a+8,b);
     		System.out.println(ss);
     		System.out.println(myParser(s));
     		//else System.out.println("1");
         	//String ss = st.nextToken();
     	//}
    }
}
