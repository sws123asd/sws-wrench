package fun.wswj.wrench.dcc.types.common;

public class Constants {

    public final static String DYNAMIC_CONFIG_CENTER_REDIS_TOPIC = "DYNAMIC_CONFIG_CENTER_REDIS_TOPIC_";

    public final static String SYMBOL_COLON = ":";

    public static String getTopic(String application) {
        return DYNAMIC_CONFIG_CENTER_REDIS_TOPIC + application;
    }

}
