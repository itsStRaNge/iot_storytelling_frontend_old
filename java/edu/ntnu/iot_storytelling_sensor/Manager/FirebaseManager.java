package edu.ntnu.iot_storytelling_sensor.Manager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import edu.ntnu.iot_storytelling_sensor.Configuration;
import edu.ntnu.iot_storytelling_sensor.MainActivity;
import edu.ntnu.iot_storytelling_sensor.R;

import static edu.ntnu.iot_storytelling_sensor.Configuration.*;
import static edu.ntnu.iot_storytelling_sensor.Configuration.SRC_IMAGE_KEY;


public class FirebaseManager implements ValueEventListener {

    private MainActivity m_context;
    public FirebaseManager(MainActivity context){
        m_context = context;

        DatabaseReference database = FirebaseDatabase.getInstance().getReference(Configuration.node());

        DatabaseReference host = database.child(HOST_KEY);
        host.addValueEventListener(this);

        DatabaseReference device = database.child(DEVICE_TYPE);
        if(!Configuration.isSensor()) device = device.child(DEVICE_NUMBER);
        device.addValueEventListener(this);
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snap) {
        String key = snap.getKey();

        if (key != null) {
            switch (key) {
                case HOST_KEY: {
                    UploadManager.HOST_IP = snap.child(HOST_IP_KEY).getValue(String.class);
                    DownloadManager.HOST_PORT = snap.child(HOST_HTTP_PORT_KEY).getValue(Integer.class);
                    UploadManager.HOST_PORT = snap.child(HOST_TCP_PORT_KEY).getValue(Integer.class);

                    ArrayList<String> audio_files =
                            (ArrayList<String>) snap.child(SRC_AUDIO_KEY).getValue();
                    ArrayList<String> image_files =
                            (ArrayList<String>) snap.child(SRC_IMAGE_KEY).getValue();
                    ArrayList<String> text_files =
                            (ArrayList<String>) snap.child(SRC_TEXT_KEY).getValue();

                    m_context.deleteCache();

                    // start downloading
                    m_context.m_progress_bar.setProgress(0);
                    m_context.m_progress_text.setText(R.string.progress_default_text);
                    m_context.m_progess_layout.setVisibility(View.VISIBLE);
                    new DownloadManager(m_context).execute(audio_files, image_files, text_files);
                    break;
                }
                case DEVICE_NUMBER:{
                    if(m_context.data_synced())
                        updateState(snap);
                    break;
                }
            }
        }
    }

    private void updateState(DataSnapshot state){
        String audio_file = state.child(AUDIO_KEY).getValue(String.class);
        String image_file = state.child(IMAGE_KEY).getValue(String.class);
        String text_file = state.child(TEXT_KEY).getValue(String.class);

        Log.d("Debug", "update State: " + audio_file
                + " - " + image_file
                + " - " + text_file);

        m_context.showImage(image_file);
        m_context.displayText(text_file);
        m_context.playAudio(audio_file);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        Log.w("Error", "loadPost:onCancelled", databaseError.toException());
    }
}
