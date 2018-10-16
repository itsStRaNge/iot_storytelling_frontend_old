package edu.ntnu.iot_storytelling_sensor.Manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import edu.ntnu.iot_storytelling_sensor.Configuration;
import edu.ntnu.iot_storytelling_sensor.MainActivity;
import edu.ntnu.iot_storytelling_sensor.R;

public class DownloadManager extends AsyncTask<ArrayList<String>, Float, Boolean> {

    public static Integer HOST_PORT = 0;

    private MainActivity m_context;

    private Integer m_total_num_files=0;
    private Integer m_downloaded_files = 0;

    DownloadManager(MainActivity context){
        m_context = context;
    }

    protected Boolean doInBackground(ArrayList... lists) {

        ArrayList<String> audio_files = lists[0];
        ArrayList<String> image_files = lists[1];
        ArrayList<String> text_files = lists[2];

        m_total_num_files = audio_files.size() + image_files.size() + text_files.size();
        m_context.m_progress_bar.setMax(m_total_num_files);
        m_context.m_progress_bar.setVisibility(View.VISIBLE);

        if(!download_audio(audio_files))
            return false;
        if(!download_images(image_files))
            return false;
        if(!download_text(text_files))
            return false;
        return true;
    }

    protected void onProgressUpdate(Float... p) {
        m_downloaded_files++;
        m_context.m_progress_bar.setProgress(m_downloaded_files);
        m_context.m_progress_text.setText(String.valueOf(m_downloaded_files) + " / "
                + String.valueOf(m_total_num_files));
    }

    @Override
    protected void onPostExecute(Boolean  p) {
        if(p)
            m_context.m_progess_layout.setVisibility(View.INVISIBLE);
        m_context.download_finished(p);
        m_context = null;
    }

    private InputStream download(String type, String file_name){
        try {
            // open connection
            URL url = new URL("http",
                    UploadManager.HOST_IP, HOST_PORT,
                    type + "/" + file_name);

            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();

            // get file content
            return connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean download_images(ArrayList<String> files){
        try {
            for(String file_name : files) {
                InputStream input = download(Configuration.IMAGE_KEY, file_name);

                // save content
                FileOutputStream out;
                out = m_context.openFileOutput(file_name, Context.MODE_PRIVATE);
                if (input != null) {
                    Bitmap bitmap = BitmapFactory.decodeStream(input);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                }else{
                    return false;
                }
                // clean up
                out.flush();
                out.close();
                input.close();

                // publish file progress
                publishProgress(0.0f);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean download_audio(ArrayList<String> files){
        try {
            for(String file_name : files) {
                InputStream input = download(Configuration.AUDIO_KEY, file_name);

                // save content
                FileOutputStream out;
                out = m_context.openFileOutput(file_name, Context.MODE_PRIVATE);
                byte data[] = new byte[1024];
                int count;
                if (input != null) {
                    while ((count = input.read(data)) != -1)
                        out.write(data, 0, count);
                }else{
                    return false;
                }

                // clean up
                out.flush();
                out.close();
                input.close();

                // publish file progress
                publishProgress(0.0f);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean download_text(ArrayList<String> files){
        try {
            for(String file_name : files) {
                InputStream input = download(Configuration.TEXT_KEY, file_name);

                // save content
                FileOutputStream out;
                out = m_context.openFileOutput(file_name, Context.MODE_PRIVATE);
                byte data[] = new byte[1024];
                int count;

                if (input != null) {
                    while ((count = input.read(data)) != -1)
                        out.write(data, 0, count);
                }else{
                    return false;
                }
                // clean up
                out.flush();
                out.close();
                input.close();

                // publish file progress
                publishProgress(0.0f);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}