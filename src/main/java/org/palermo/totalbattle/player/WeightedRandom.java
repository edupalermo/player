package org.palermo.totalbattle.player;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WeightedRandom {
    public static <T> T escolherComPeso(List<T> lista) {
        Random random = new Random();

        // Quanto menor o índice, maior o peso (ex: peso = tamanho - índice)
        int totalPeso = 0;
        int tamanho = lista.size();

        int[] pesos = new int[tamanho];
        for (int i = 0; i < tamanho; i++) {
            pesos[i] = tamanho - i;   // Elemento 0 tem peso maior
            totalPeso += pesos[i];
        }

        // Sorteia dentro do total de pesos
        int r = random.nextInt(totalPeso) + 1;

        // Encontra o índice correspondente ao peso acumulado
        int acumulado = 0;
        for (int i = 0; i < tamanho; i++) {
            acumulado += pesos[i];
            if (r <= acumulado) {
                return lista.get(i);
            }
        }

        return lista.get(tamanho - 1);
    }

    public static void main(String[] args) {
        List<String> dados = Arrays.asList("A", "B", "C", "D", "E");

        for (int i = 0; i < 20; i++) {
            System.out.println(escolherComPeso(dados));
        }
    }
}