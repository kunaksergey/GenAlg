package ua.shield;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) {
        if (args.length != 10) {
            System.out.println("Необходимо 10 аргументов ");
            System.exit(0);
        }

        int[] in = new int[10];
        try {
            for (int i = 0; i < args.length; i++) {
                in[i] = Integer.parseInt(args[i]);
                if (in[i] < 1 || in[i] > 10) throw new NumberFormatException();
            }

            ExecutorService pool = Executors.newFixedThreadPool(10);

            Set<Future<Сhromosome>> set = new HashSet<>();
            for (int i = 0; i < 10; i++) {
                Future<Сhromosome> future = pool.submit(new GeneticAlg(in));
                set.add(future);
            }
            List<Сhromosome> resultList=new ArrayList<>();
            for (Future<Сhromosome> future : set) {
                try {
                    resultList.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(Arrays.toString(GeneticAlg.getMinGen(resultList).getGens()));
            pool.shutdown();
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат. Необходимы 10 чисел от 1 до 10");
        }
    }
}
