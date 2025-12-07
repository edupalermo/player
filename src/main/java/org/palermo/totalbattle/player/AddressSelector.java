package org.palermo.totalbattle.player;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AddressSelector {
    
    private static final Map<Player, Integer> PLAYERS_COUNTER = new HashMap<>();
    
    private static final int LOOP = 5;
    
    private static final Random random = new Random();

    public static String select(Player player) {
        String address;
        int counter = PLAYERS_COUNTER.computeIfAbsent(player, (p) -> 0);

        switch(counter % LOOP) {
            case 0:
                address = getAddressByWeekDay();
                break;
            case 1:
                address = REPEATABLE_BONUS[random.nextInt(REPEATABLE_BONUS.length)];
                break;
            default:
                address = ONE_TIME_BONUS[choose(ONE_TIME_BONUS.length)];
                break;
        }
        
        counter = counter + 1;
        PLAYERS_COUNTER.put(player, counter);
        
        return address;
    }

    private static final String[] REPEATABLE_BONUS = new String[] {
            "https://totalbattle.com/en/?present=dragonroulette100",
            "https://totalbattle.com/en/?present=luckyroulette100",
            "https://totalbattle.com/en/?present=ramsesroulette100",
            "https://totalbattle.com/en/?present=rm65649016820614116771"
    };


    private static final String[] ONE_TIME_BONUS = new String[] {
            "https://totalbattle.com/en/?present=ref1_december_25_fJzr",
            "https://totalbattle.com/en/?present=ref4_november_25_GnPt",
            "https://totalbattle.com/en/?present=ref3_november_25_Uow4",
            "https://totalbattle.com/en/?present=ref5_november_25_ZN6z",
            "https://totalbattle.com/en/?present=ref1_november_25_rGga",
            "https://totalbattle.com/en/?present=ref1_october_25_6nhH",
            "https://totalbattle.com/en/?present=ref4_spareoctober_25_FK5y",
            "https://totalbattle.com/en/?present=ref2_october_25_3SX2", 
            "https://totalbattle.com/en/?present=ref4_september_25_AE0S",
            "https://totalbattle.com/en/?present=ref3_september_25_Wm0e"
    };

    private static String getAddressByWeekDay() {
        LocalDate today = LocalDate.now();
        int weekdayNumber = today.getDayOfWeek().getValue();

        switch (weekdayNumber) {
            case 1:
                return "https://totalbattle.com/en/?present=gold";
            case 2:
                return "https://totalbattle.com/en/?present=xp";
            case 3:
                return "https://totalbattle.com/en/?present=tar";
            case 4:
                return "https://totalbattle.com/en/?present=march25";
            case 5:
                return "https://totalbattle.com/en/?present=gold500";
            case 6:
                return "https://totalbattle.com/en/?present=speedups15";
            case 7:
                return "https://totalbattle.com/en/?present=speedups3";
        }
        throw new RuntimeException("Week day problem!");
    }

    public static int choose(int size) {
        Random random = new Random();

        // Quanto menor o índice, maior o peso (ex: peso = tamanho - índice)
        int totalPeso = 0;

        int[] pesos = new int[size];
        for (int i = 0; i < size; i++) {
            pesos[i] = size - i;   // Elemento 0 tem peso maior
            totalPeso += pesos[i];
        }

        // Sorteia dentro do total de pesos
        int r = random.nextInt(totalPeso) + 1;

        // Encontra o índice correspondente ao peso acumulado
        int acumulado = 0;
        for (int i = 0; i < size; i++) {
            acumulado += pesos[i];
            if (r <= acumulado) {
                return i;
            }
        }

        return size - 1;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            System.out.println(select(Player.PALERMO));
        }
    }
}
