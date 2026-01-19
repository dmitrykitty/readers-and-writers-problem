package com.dnikitin;

import com.dnikitin.actors.Library;
import com.dnikitin.actors.Reader;
import com.dnikitin.actors.Writer;

import java.util.ArrayList;
import java.util.List;

public class LibraryRunner {
    static void main(String[] args) {
        int MAX_READERS = 5;
        int RESTING_TIME = 2000;
        if(args.length == 2) {
            MAX_READERS = Integer.parseInt(args[0]);
            RESTING_TIME = Integer.parseInt(args[1]);
        }

        Library library = new Library(MAX_READERS);
        List<Thread> users =  new ArrayList<>();

        for(int i = 0; i < 10; i++) {
            users.add(new Reader("reader" + i, library, RESTING_TIME));
        }
        for(int i = 0; i < 5; i++) {
            users.add(new Writer("writer" + i, library, RESTING_TIME));
        }

        for(Thread thread : users) {
            thread.start();
        }
        for(Thread thread : users) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
