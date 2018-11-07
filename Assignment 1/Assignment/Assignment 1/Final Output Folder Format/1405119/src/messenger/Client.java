package messenger;

import java.util.Scanner;
import util.ConnectionUtillities;
import util.Reader;
import util.Writer;

public class Client {
    static Scanner in = new Scanner(System.in);
    public static void main(String[] args) {
        ConnectionUtillities connection = new ConnectionUtillities("localhost",5678);
       
        System.out.println("Enter your student ID:");
        String a=in.nextLine();
        connection.write(a);        
        
        
        new Thread(new Reader(connection)).start();
        //new Thread(new Writer(connection)).start();
        
        while(true);
    }
}