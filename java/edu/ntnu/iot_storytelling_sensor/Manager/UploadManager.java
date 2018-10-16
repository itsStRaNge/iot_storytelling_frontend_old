package edu.ntnu.iot_storytelling_sensor.Manager;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class UploadManager extends AsyncTask<JSONObject,JSONObject,String> {
    public static String HOST_IP = "10.22.76.45";
    public static Integer HOST_PORT = 8888;

    private UploadInterface m_caller = null;

    public UploadManager(UploadInterface caller){
        m_caller = caller;
    }

    public void send(JSONObject packet){
        execute(packet);
    }
    @Override
    protected String doInBackground(JSONObject... obje) {
        Socket nsocket = null;
        DataOutputStream dataOutputStream;

        try {
            /* Open Connection*/
            nsocket = new Socket();
            nsocket.connect(new InetSocketAddress(HOST_IP, HOST_PORT),500);
            dataOutputStream = new DataOutputStream(nsocket.getOutputStream());

            /* Send data*/
            dataOutputStream.writeBytes(obje[0].toString());
            dataOutputStream.flush();
            nsocket.close();
        } catch(UnknownHostException e) {
            Log.e("Network", "Unknown host: " + HOST_IP);
            return "Unknown Host";
        } catch(IOException e) {
            Log.e("Network", "No I/O");
            return "Failed to Connect";
        } catch(Exception e){
            Log.e("Network", e.toString());
            return "Unexpected Error";
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            m_caller.serverResult(result);
        }catch(NullPointerException e){
            m_caller.serverResult("");
        }
    }
}
