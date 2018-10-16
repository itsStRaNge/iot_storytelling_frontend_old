package edu.ntnu.iot_storytelling_sensor;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import edu.ntnu.iot_storytelling_sensor.Manager.FileManager;
import edu.ntnu.iot_storytelling_sensor.Manager.FirebaseManager;
import edu.ntnu.iot_storytelling_sensor.Manager.UploadInterface;
import edu.ntnu.iot_storytelling_sensor.Manager.UploadManager;
import pl.droidsonroids.gif.GifImageView;


/* Implements all utilities to display and interact with the User as Sensor App */

public abstract class SensorUtilities extends FileManager implements View.OnDragListener,
                                                            View.OnTouchListener,
                                                            UploadInterface {

    public final static int QR_Call = 0;
    public final static int PERMISSION_REQUEST_CAMERA = 1;

    private GifImageView m_field_obj;
    private GifImageView m_rel_obj;

    private String m_qr_code="code1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Configuration.isSensor()) {
            /* display sensor background */
            findViewById(R.id.sensor_overlay).setVisibility(View.VISIBLE);

            /* Check for permissions */
            check_camera_permission();

            /* Drag and Drop Init */
            findViewById(R.id.field_topleft).setOnDragListener(this);
            findViewById(R.id.field_topright).setOnDragListener(this);
            findViewById(R.id.field_bottomleft).setOnDragListener(this);
            findViewById(R.id.field_bottomright).setOnDragListener(this);
            findViewById(R.id.parent_view).setOnDragListener(this);

            m_field_obj = (GifImageView) findViewById(R.id.myimage_fields);
            m_field_obj.setOnTouchListener(this);
            m_rel_obj = (GifImageView) findViewById(R.id.myimage_rel);
            m_rel_obj.setOnTouchListener(this);
        }
    }


    /* PERMISSION REQUEST FOR CAMERS */
    private void check_camera_permission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},PERMISSION_REQUEST_CAMERA);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    check_camera_permission();
                }
            }
        }
    }

    /* ON TOUCH LISTENER */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                    v);
            v.startDrag(data, shadowBuilder, v, 0);
            v.setVisibility(View.INVISIBLE);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        Drawable enterShape = getDrawable(R.drawable.shape_droptarget);
        Drawable normalShape = getDrawable(R.drawable.shape);

        switch(event.getAction()) {
            case DragEvent.ACTION_DRAG_ENTERED:
                if(v.getId() != R.id.parent_view)
                    v.setBackground(enterShape);
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                if(v.getId() != R.id.parent_view)
                    v.setBackground(normalShape);
                break;
            case DragEvent.ACTION_DROP:
                ViewGroup container = (ViewGroup) v;

                if(container.getId() == R.id.parent_view){
                    float x_cord = event.getX() - m_rel_obj.getWidth() / 2;
                    float y_cord = event.getY() - m_rel_obj.getHeight() / 2;
                    m_rel_obj.setX(x_cord);
                    m_rel_obj.setY(y_cord);
                    m_rel_obj.setVisibility(View.VISIBLE);
                    m_rel_obj.bringToFront();
                    m_field_obj.setVisibility(View.INVISIBLE);
                }else{
                    ViewGroup owner = (ViewGroup) m_field_obj.getParent();
                    owner.removeView(m_field_obj);
                    container.addView(m_field_obj);
                    v.setBackground(normalShape);
                    m_field_obj.setVisibility(View.VISIBLE);
                    m_rel_obj.setVisibility(View.INVISIBLE);
                }
                // oyvind wants reaction only on qr code scan
                // create_request();
                break;
        }
        return true;
    }

    /* QR CODE SCANNER CALLBACK*/
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == QR_Call) {
            if (resultCode == RESULT_OK) {
                m_qr_code = data.getStringExtra("scanCode");
                Log.d("QR_CODE", m_qr_code);
                create_request();
            } else {
                Log.d("Error", "Could not read QR Code");
            }
        }
    }

    /* NETWORKING */
    private void create_request(){
        int position = 0; // stays zero if m_rel_obj is active

        if(m_field_obj.getVisibility() == View.VISIBLE){
            ViewGroup parent = (ViewGroup) m_field_obj.getParent();
            switch(parent.getId()){
                case R.id.field_topleft:
                    position = 1;
                    break;
                case R.id.field_topright:
                    position = 2;
                    break;
                case R.id.field_bottomleft:
                    position = 3;
                    break;
                case R.id.field_bottomright:
                    position = 4;
                    break;
            }
        }

        try {
            JSONObject json_pkg = new JSONObject();

            json_pkg.put("position", position);
            json_pkg.put("qr_code", m_qr_code);

            startRequest(json_pkg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /* UploadManager CALLBACKS */
    @Override
    public void startRequest(JSONObject packet) {
        if(!data_synced()){
            return;
        }

        UploadManager network = new UploadManager(this);
        network.send(packet);
    }

    @Override
    public void serverResult(String result) {
        if(!result.isEmpty()){
            Toast toast = Toast.makeText(getApplicationContext(), "Error: " + result, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

}
