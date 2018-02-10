package seminar.duydpdse62412.socketclientdemo;

import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by DUYDPDSE62412 on 2/7/2018.
 */

public class MessageSender extends AsyncTask<String, Void, Void> {
    Socket socket;
    DataOutputStream dataOutputStream;
    PrintWriter pw;

    @Override
    protected Void doInBackground(String... voids) {
        String message = voids[0];
        try{
            socket = new Socket("10.82.139.49", 7800);
            pw= new PrintWriter(socket.getOutputStream());
            pw.write(message);
            pw.flush();
            pw.close();
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    return null;
    }
}
