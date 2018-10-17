package edu.ntnu.iot_storytelling_sensor;

public final class Configuration {

    /* ALLOWED TO CHANGE
     * ---------------------------------------------------------------*/

    // set true to use Productive node, otherwise develop node is used
    public static final boolean PRODUCTIVE = false;

    // Sensor for Sensor Application, Actuator for Actuator Application
    public static final String DEVICE_TYPE = "Actuator";

    // refers to the number in the Actuator list on firebase realtime database
    // Only needed if Actuator is selected
    public static final String DEVICE_NUMBER = "0";

    /* ONLY CHANGE FOLLOWING VARIABLES IF YOU KNOW WHAT YOU ARE DOING
    * ---------------------------------------------------------------*/
    /* Following variables specify nodes in the database
        refer to: https://ntnu-iot-storytelling.firebaseio.com/
     */
    public static final String PRODUCTIVE_NODE = "Productive";
    public static final String DEVELOP_NODE = "Develop";

    public static final String HOST_KEY = "Host";
    public static final String HOST_IP_KEY = "ip";
    public static final String HOST_HTTP_PORT_KEY = "http_port";
    public static final String HOST_TCP_PORT_KEY = "tcp_port";

    public static final String AUDIO_KEY = "audio";
    public static final String IMAGE_KEY = "image";
    public static final String TEXT_KEY = "text";

    public static final String SRC_AUDIO_KEY = "Audio";
    public static final String SRC_IMAGE_KEY = "Images";
    public static final String SRC_TEXT_KEY = "Text";

    public static boolean isSensor(){
        return DEVICE_TYPE.equals("Sensor");
    }

    public static String node(){
        if(PRODUCTIVE)
            return PRODUCTIVE_NODE;
        else
            return DEVELOP_NODE;
    }
}
