package Server;

import Model.ListEntry;
import util.NetworkUtil;

import java.util.Enumeration;
import java.util.function.BiConsumer;

import static Server.Server.model;
import static Server.Server.table;

public class ShowCurrentUser implements Runnable {

    Thread thread;
    public ShowCurrentUser(){
        thread=new Thread(this);
        thread.start();
    }


    @Override
    public void run() {
        try {
            while (true) {
                Enumeration e = table.keys();
                int c = 0;
                model.removeAllElements();
                while (e.hasMoreElements()) {
                    model.add(c++, new ListEntry((String) e.nextElement()));
                }

                thread.sleep(2000);
            }

        }catch (Exception e){

        }

    }
}
