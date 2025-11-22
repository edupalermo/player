package org.palermo.totalbattle.service.army;

import org.palermo.totalbattle.internalservice.ArmyService;
import org.palermo.totalbattle.player.Player;
import org.palermo.totalbattle.player.Scenario;
import org.palermo.totalbattle.player.SharedData;
import org.palermo.totalbattle.service.army.bean.ArmyBean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/army")
public class ArmyController {
    
    private final ArmyService armyService = new ArmyService();

    @PostMapping()
    public void set(@RequestBody ArmyBean armyBean) {
        org.palermo.totalbattle.player.bean.ArmyBean serviceArmyBean = 
                org.palermo.totalbattle.player.bean.ArmyBean.builder()
                        .player(Player.getPlayerByName(armyBean.getPlayerName()))
                        .goal(armyBean.getGoal())
                        .waves(armyBean.getWaves())
                        .leadership(armyBean.getLeadership())
                        .dominance(armyBean.getDominance())
                        .authority(armyBean.getAuthority())
                .build();
        armyService.setArmy(serviceArmyBean);
    }
}
