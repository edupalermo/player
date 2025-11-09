package org.palermo.totalbattle.service.system;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.player.SharedData;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SystemController {

    @PostMapping("/system/stop")
    public void stop() {
        System.exit(0);
    }

}
