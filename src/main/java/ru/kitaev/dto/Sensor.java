package ru.kitaev.dto;

public class Sensor {
    private final String sensorId;
    private final double warnThreshold;
    private final double critThreshold;
    private final int probeInterval;
    private final int notifyInterval;
    private volatile AlarmLevel alarmLevel = AlarmLevel.NORM;

    public Sensor(String dataLine) {
        String[] dataArray = dataLine.split("\t");

        this.sensorId = dataArray[0];
        this.warnThreshold = Double.parseDouble(dataArray[1]);
        this.critThreshold = Double.parseDouble(dataArray[2]);
        this.probeInterval = Integer.parseInt(dataArray[3]);
        this.notifyInterval = Integer.parseInt(dataArray[4]);
    }

    public Sensor(String sensorId, double warnThreshold, double critThreshold, int probeInterval, int notifyInterval) {
        this.sensorId = sensorId;
        this.warnThreshold = warnThreshold;
        this.critThreshold = critThreshold;
        this.probeInterval = probeInterval;
        this.notifyInterval = notifyInterval;
    }


    public String getSensorId() {
        return sensorId;
    }

    public double getWarnThreshold() {
        return warnThreshold;
    }

    public double getCritThreshold() {
        return critThreshold;
    }

    public int getProbeInterval() {
        return probeInterval;
    }

    public int getNotifyInterval() {
        return notifyInterval;
    }

    public void setAlarmLevel(AlarmLevel alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    public AlarmLevel getAlarmLevel() {
        return alarmLevel;
    }
}
