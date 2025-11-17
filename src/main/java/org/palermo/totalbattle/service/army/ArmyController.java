package org.palermo.totalbattle.service.army;

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

    @PostMapping()
    public void set(@RequestBody ArmyBean armyBean) {
        org.palermo.totalbattle.player.bean.ArmyBean serviceArmyBean = 
                org.palermo.totalbattle.player.bean.ArmyBean.builder()
                        .playerName(armyBean.getPlayerName())
                        .waves(armyBean.getWaves())
                        .leadership(armyBean.getLeadership())
                        .dominance(armyBean.getDominance())
                        .authority(armyBean.getAuthority())
                .build();
        SharedData.INSTANCE.setAndSaveArmy(serviceArmyBean);
        Player player = Player.builder()
                .name(armyBean.getPlayerName())
                .build();
        SharedData.INSTANCE.clearWait(player, Scenario.TRAIN_TROOPS);
    }
}
