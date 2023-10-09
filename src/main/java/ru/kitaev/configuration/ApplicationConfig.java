package ru.kitaev.configuration;

import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import ru.kitaev.dto.AlarmLevel;
import ru.kitaev.dto.Sensor;
import ru.kitaev.feign.SensorsServerClient;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Configuration
public class ApplicationConfig {
    @Autowired
    private SensorsServerClient sensorsServerClient;

    @Bean
    public ThreadPoolTaskScheduler getProbeTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler
                = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix(
                "getProbeTaskScheduler");
        return threadPoolTaskScheduler;
    }

    @Bean
    public ThreadPoolTaskScheduler alarmNotifyTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler
                = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix(
                "alarmNotifyTaskScheduler");
        return threadPoolTaskScheduler;
    }

    @Bean
    public void getSensorsInitialData() {
        List<Sensor> sensors = Optional.ofNullable(sensorsServerClient.getSensorsData())
                .map(entireData -> Arrays.asList(entireData.split("\n")))
                .orElse(Collections.emptyList()).stream()
                .map(Sensor::new)
                .collect(Collectors.toList());
        Map<Integer, List<Sensor>> probeIntervalGroups = sensors.stream()
                .collect(Collectors.groupingBy(Sensor::getProbeInterval));

        Map<Integer, List<Sensor>> notifyIntervalGroups = sensors.stream()
                .collect(Collectors.groupingBy(Sensor::getNotifyInterval));

        probeIntervalGroups.forEach((key, value) -> {
            PeriodicTrigger trigger = new PeriodicTrigger(key, TimeUnit.SECONDS);
            trigger.setInitialDelay(key);
            getProbeTaskScheduler().schedule(new GetProbeTask(key, value), trigger);
        });

        notifyIntervalGroups.forEach((key, value) -> {
            PeriodicTrigger trigger = new PeriodicTrigger(key, TimeUnit.SECONDS);
            trigger.setInitialDelay(key);
            alarmNotifyTaskScheduler().schedule(new NotifyTask(key, value), trigger);
        });

    }

    private class GetProbeTask implements Runnable {

        private final int interval;
        private final List<Sensor> sensors;

        private GetProbeTask(int interval, List<Sensor> sensors) {
            this.interval = interval;
            this.sensors = sensors;
        }


        @Override
        public void run() {
            System.out.printf("%s sec interval is started. \n", interval);
            sensors.forEach(sensor -> {
                try {
                    double probe = Double.parseDouble(sensorsServerClient.getProbe(sensor.getSensorId()));
                    AlarmLevel level;
                    if (probe < sensor.getWarnThreshold()) {
                        level = AlarmLevel.NORM;
                    } else if (sensor.getWarnThreshold() <= probe && probe < sensor.getCritThreshold()) {
                        level = AlarmLevel.WARN;
                    } else {
                        level = AlarmLevel.CRIT;
                    }
                    sensor.setAlarmLevel(level);
                } catch (FeignException e) {
                    // skip update
                }
            });
            System.out.printf("%s sec interval is finished.\n", interval);
        }
    }

    private class NotifyTask implements Runnable {

        private final int interval;
        private final List<Sensor> sensors;

        private NotifyTask(int interval, List<Sensor> sensors) {
            this.interval = interval;
            this.sensors = sensors;
        }

        @Override
        public void run() {
            System.out.printf("Notify interval %s sec is started. \n", interval);
            Map<AlarmLevel, List<Sensor>> notifyGroups = sensors.stream().collect(Collectors.groupingBy(Sensor::getAlarmLevel));
            Optional.ofNullable(notifyGroups)
                    .map(list -> list.get(AlarmLevel.CRIT))
                    .orElse(Collections.emptyList())
                    .forEach(sensor -> {
                        System.out.printf("Sensor %s notification. Level %s \n", sensor.getSensorId(), AlarmLevel.CRIT);
                        sensorsServerClient.notify(sensor.getSensorId(), AlarmLevel.CRIT);
                    });
            Optional.ofNullable(notifyGroups)
                    .map(list -> list.get(AlarmLevel.WARN))
                    .orElse(Collections.emptyList())
                    .forEach(sensor -> {
                        System.out.printf("Sensor %s notification. Level %s \n", sensor.getSensorId(), AlarmLevel.WARN);
                        sensorsServerClient.notify(sensor.getSensorId(), AlarmLevel.WARN);
                    });
            System.out.printf("Notify interval %s sec is finished. \n", interval);
        }
    }
}
