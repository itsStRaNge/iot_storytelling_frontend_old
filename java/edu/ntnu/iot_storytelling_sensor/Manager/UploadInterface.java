package edu.ntnu.iot_storytelling_sensor.Manager;

import org.json.JSONObject;

public interface UploadInterface {
    void startRequest(JSONObject packet);
    void serverResult(String result);
}
