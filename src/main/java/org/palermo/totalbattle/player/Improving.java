package org.palermo.totalbattle.player;

import org.palermo.totalbattle.player.task.BuildArmy;
import org.palermo.totalbattle.player.task.ClanContribution;
import org.palermo.totalbattle.player.task.FreeSale;
import org.palermo.totalbattle.player.task.PayTaxes;
import org.palermo.totalbattle.player.task.Quests;
import org.palermo.totalbattle.player.task.SummoningCircle;
import org.palermo.totalbattle.player.task.Thelensia;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.server.model.Player;
import org.palermo.totalbattle.util.ServerFacade;

public class Improving {

    private static final MyRobot robot = MyRobot.INSTANCE;
    
    private static final ServerFacade facade = new ServerFacade();

    public static void main(String[] args) {
        robot.leftClick(Point.of(467, 50));
        
        Player player = facade.retrievePlayer("Mightshaper").orElseThrow(() -> new RuntimeException());
        
        //Player player = new Player();
        //player.setName("Palermo");

         //BuildArmy buildArmy = new BuildArmy(player);
         //buildArmy.buildArmy(true);

        // (new ClanContribution(player)).helpClanMembers();
        // (new ClanContribution(player)).collectChests();

        (new Quests(player)).evaluate();
        // (new FreeSale(player)).freeSale();
        // new Thelensia(player).evaluate();

        //new SummoningCircle(player).evaluate();
        
        /*
        BuildArmy buildArmy = new BuildArmy(player);
        for (int i = 0; i < 100; i++) {
            try {
                buildArmy.buildArmy(false);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
         */
        
        //new PayTaxes(player).pay();
        
        // buildArmy.playSpeedUpPopup(60);
        
        // buildArmy.fillResource(13900000);
        // buildArmy.playOpenBoostersPopUp();
        
        facade.updatePlayer(player);
    }
}
