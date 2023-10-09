package ru.kitaev.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.kitaev.dto.AlarmLevel;

@FeignClient(name = "sensors", url = "158.160.44.144:3000")
public interface SensorsServerClient {

    @RequestMapping(method = RequestMethod.GET, path = "sensors/list")
    String getSensorsData();

    @RequestMapping(method = RequestMethod.GET, path = "sensors/probe?sensor_id={sensorId}")
    String getProbe(@PathVariable("sensorId") String sensorId);

    @RequestMapping(method = RequestMethod.POST, path = "/alarms/notify?sensor_id={sensorId}&level={level}")
    void notify(@PathVariable("sensorId") String sensorId, @PathVariable("level") AlarmLevel level);
}
