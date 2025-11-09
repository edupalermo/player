package org.palermo.totalbattle.service.wait;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class CooldownController {
    
    @GetMapping("/cooldowns")
    public List<CooldownBean> list() {
        return null;
    }
}
