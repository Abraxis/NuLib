package org.abraxis.nulib;

public class Config {
    private static Config instance = new Config();
    private String mqHost = "localhost";
    private String topic_events = "topic_events";
    private String udevadmPath = "/sbin/udevadm";

    public static Config getInstance() {
        return instance;
    }

    public String getMqHost() {
        return mqHost;
    }

    public String getTopic_events() {
        return topic_events;
    }

    public String getUdevadmPath() {
        return udevadmPath;
    }
}