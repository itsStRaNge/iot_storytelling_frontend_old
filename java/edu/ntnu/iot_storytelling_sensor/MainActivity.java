package edu.ntnu.iot_storytelling_sensor;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import edu.ntnu.iot_storytelling_sensor.Manager.FirebaseManager;


public class MainActivity extends SensorUtilities{


    public ProgressBar m_progress_bar;
    public TextView m_progress_text;
    public LinearLayout m_progess_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);

        if(Configuration.isSensor()){
            /* Application is a Sensor Application
            * Setup QR Scan Button because of the Intent Result we get
            * */

            /* Setup QR Scan Button */
            ImageView camButton = (ImageView) findViewById(R.id.camera_button);
            camButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // because of this line 'MainActivity.this'
                    Intent intent = new Intent(MainActivity.this, QRScanActivity.class);
                    startActivityForResult(intent, QR_Call);
                }
            });
        }else{
            findViewById(R.id.sensor_overlay).setVisibility(View.GONE);
        }

        /* Download Progress Overlay */
        m_progress_bar = findViewById(R.id.progress_bar);
        m_progress_text = findViewById(R.id.progress_text);
        m_progess_layout = findViewById(R.id.progress_layout);

        // Start Firebase Services
        new FirebaseManager(this);
    }

}
