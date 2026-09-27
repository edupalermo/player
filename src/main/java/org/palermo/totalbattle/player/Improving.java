package org.palermo.totalbattle.player;

import org.palermo.totalbattle.player.task.*;
import org.palermo.totalbattle.selenium.leadership.MyRobot;
import org.palermo.totalbattle.selenium.leadership.Point;
import org.palermo.totalbattle.server.model.FlagInfo;
import org.palermo.totalbattle.server.model.FlagScenario;
import org.palermo.totalbattle.server.model.Player;
import org.palermo.totalbattle.util.FlagUtil;
import org.palermo.totalbattle.util.ServerFacade;

import java.lang.management.LockInfo;
import java.util.Comparator;
import java.util.Map;

public class Improving {

    private static final MyRobot robot = MyRobot.INSTANCE;
    
    private static final ServerFacade facade = new ServerFacade();

    public static void main(String[] args) {
        robot.leftClick(Point.of(467, 50));
        
        Player player = facade.retrievePlayer("Mightshaper").orElseThrow(() -> new RuntimeException());


        //Process process = Task.openOrdinaryBrowser(player);
        //Task.login(player);
        
        
        //Player player = new Player();
        //player.setName("Palermo");

         //BuildArmy buildArmy = new BuildArmy(player);
         //buildArmy.buildArmy(false);

        // (new ClanContribution(player)).helpClanMembers();
        // (new ClanContribution(player)).collectChests();

        // (new Quests(player)).evaluate();
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

        //(new AttackArena(player)).evaluate();
        //(new CollectAdministrationGift(player)).evaluate();
        //(new MineSilver(player)).evaluate();
        
        
        player.getFlags().entrySet().stream()
                .sorted(
                        Comparator
                                .comparing((Map.Entry<String, FlagInfo> e) ->
                                        FlagUtil.isActive(player, e.getKey()))
                                .thenComparing((Map.Entry<String, FlagInfo> e) -> e.getValue().getExpiration())
                )
            .forEach((entry) -> {
                if (FlagUtil.isActive(player, entry.getKey())) {
                    System.out.println(String.format("%s %s", filler(entry.getKey(), 48), FlagUtil.duration(entry.getValue())));
                    
                }
                else {
                    System.out.println(String.format("%s FREE", filler(entry.getKey(), 48)));
                }
            }); 
                
        
        facade.updatePlayer(player);
    }
    
    private static String filler(String name, int size) {
        int count = 0;
        if (size > name.length()) { 
            count = size - name.length();
        }
        return name + ".".repeat(count);
    }
}
