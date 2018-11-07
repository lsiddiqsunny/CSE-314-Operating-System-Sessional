package prototype.client;

import java.io.IOException;
import java.util.Scanner;

import static java.lang.System.exit;


public class StartClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = new Client();
        String s; Scanner sc = new Scanner(System.in);
        ThreadClientSend tcs = new ThreadClientSend(client);/** ok*/
        ThreadClientReceive tcr = new ThreadClientReceive(client); /** not ok*/

    }
}
