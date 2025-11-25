package org.palermo.totalbattle.service.property;

import jakarta.websocket.server.PathParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.palermo.totalbattle.player.SharedData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class PropertyController {

    private SharedData sharedData = SharedData.INSTANCE;

    @PostMapping("/property")
    public void set(@RequestBody PropertyBean propertyBean) {
        sharedData.getAutomationState().getProperties().put(propertyBean.getKey(), propertyBean.getValue());
        sharedData.saveAutomationState();
    }
    
    @GetMapping("/property/{key}")
    public String set(@PathVariable("key") String key) {
        return ObjectUtils.firstNonNull(sharedData.getAutomationState().getProperties().get(key), "");
    }
}
