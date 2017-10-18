package ua.shield;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Created by sa on 04.10.17.
 * Генетический алгоритм поиска лучшей комбинации
 */
public class GeneticAlg implements Callable<Сhromosome>{

    private int[] priceCheckPoint; //входные данные
    private static final int CROSS_POINT = 5;//точка скрещивания
    private static final double MUTATION_PERCENT = 0.2; //вероятность мутации
    private static final double DEGRADATION_PERSCENT=0.9; //процент вырождаемости
    private static final int POPULATION_SIZE = 200; //размер популяции
    private static final int COUNT_GENERATION = 5000;//количество циклов

    private int count = 1; //номер поколения
    private List<Сhromosome> currentGeneration = new ArrayList<>(); //текущее поколение
    private final int[] coins = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}; //набор для генерации

    public GeneticAlg(int[] priceCheckPoint) {
        this.priceCheckPoint = priceCheckPoint;
    }

    /**
     * Фитнес функция, возвращает результат(@int) отклонения для набора (chromosomes)
     */
    private int fitnessFunction(int[] chromosomes) {
        int result = 0;
        //int length = (chromosomes.length <= priceCheckPoint.length) ? chromosomes.length : priceCheckPoint.length;
        for (int i = 0; i < 10; i++) {
            if (priceCheckPoint[i] > chromosomes[i]) {
                result += priceCheckPoint[i] - chromosomes[i];
            }
        }
        return result;
    }

    /**
     * Генерируеум первое поколение хромосом
     */
    private void firstGeneration() {
        currentGeneration = generationListChromosome(POPULATION_SIZE);
    }

    /**
     * Генерируем @count хромосом
     */
    private List<Сhromosome> generationListChromosome(int count) {
        Сhromosome chromos;
        Set<Сhromosome> setChromos = new HashSet<>();
        while (setChromos.size() < count) {
            chromos = generationChromos();
            setChromos.add(chromos);
        }
        setChromos.forEach(e -> e.setResultFitness(fitnessFunction(e.getGens())));
        return new ArrayList<>(setChromos);
    }

    /**
     * Генерируем хромосому
     */
    private Сhromosome generationChromos() {
        int[] chromosomes = new int[10];
        List<Integer> listGen = Arrays.stream(coins).boxed().collect(Collectors.toList());
        for (int i = 0; i < chromosomes.length; i++) {
            int index = (int) (Math.random() * listGen.size());
            chromosomes[i] = listGen.get(index);
            listGen.remove(index);
        }
        return new Сhromosome(chromosomes, count);
    }

    //Получаем хромосому с минимальным значением fitness функции
    public static Сhromosome getMinGen(List<Сhromosome> in) {
        return getGenByFunction(in, (o1, o2) -> o1.getResultFitness() - o2.getResultFitness());
    }

    //Получаем хромосому с максимальным значением fitness функции
    private static Сhromosome getMaxGen(List<Сhromosome> in) {
        return getGenByFunction(in, (o1, o2) -> o2.getResultFitness() - o1.getResultFitness());
    }

    //получаем хромосому в зависимости от переданного компаратора
    private static Сhromosome getGenByFunction(List<Сhromosome> in, Comparator<Сhromosome> comparator) {
        List<Сhromosome> gens = new LinkedList<>(in);
        Collections.sort(gens, comparator);
        return gens.get(0);
    }

    /**
     * Производим селекцию лучших
     */
    private void selection() {
        List<Сhromosome> nextGeneration = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            int index1 = (int) (Math.random() * (currentGeneration.size() - 1));
            int index2 = (int) (Math.random() * (currentGeneration.size() - 1));
            Сhromosome chromos = (currentGeneration.get(index1).getResultFitness()
                    < currentGeneration.get(index2).getResultFitness()) ? currentGeneration.get(index1) : currentGeneration.get(index2);
            nextGeneration.add(new Сhromosome(chromos,count));

        }
        currentGeneration = nextGeneration;
    }

    /*
     *Скрещиваем хромосомы
     */
    private void cross() {
        Set<Сhromosome> crossList = new HashSet<>();
        for (int index1 = 0; index1 < currentGeneration.size(); index1++) {
            for (int index2 = 0; index2 < currentGeneration.size(); index2++) {
                if (index1 == index2) break; //хромосомы с одним индексом пропускаем

                Сhromosome parentOne = currentGeneration.get(index1);
                Сhromosome parentTwo = currentGeneration.get(index2);
                if (parentOne.equals(parentTwo)) break; //если есть хромосомы с одинаковым набором генов-отбрасываем

                //Ищем ключи для скрещивания
                Map<Integer, Integer[][]> keyMap = new HashMap<>();
                for (int i = 1; i < parentOne.getGens().length - 2; i++) {
                    //берем по очереди наборы от 0 до i
                    Integer[] leftPartParentOne = Arrays.stream(Arrays.copyOfRange(parentOne.getGens(), 0, i + 1)).boxed().toArray(Integer[]::new);
                    Integer[] leftPartParentTwo = Arrays.stream(Arrays.copyOfRange(parentTwo.getGens(), 0, i + 1)).boxed().toArray(Integer[]::new);

                    //находим их пересечение.
                    Set<Integer> setChrosmDiff = new HashSet<>(new ArrayList<>(Arrays.asList(leftPartParentOne)));
                    setChrosmDiff.removeAll(new ArrayList<>(Arrays.asList(leftPartParentTwo)));

                    //если наборы одинаковые, то сохраняем вместе с индексом.
                    if (setChrosmDiff.size() == 0) {
                        keyMap.put(i, new Integer[][]{leftPartParentOne, leftPartParentTwo});
                    }
                }

                //если такие ключи найдены, начинаем скрещивание:
                if (keyMap.keySet().size() > 0) {

                    // выбираем ближайший к точке скрещивания
                    int crossPosition = -1;

                    for (Integer p : keyMap.keySet()) {
                        if (crossPosition != -1) {
                            if (Math.abs(CROSS_POINT - p) < Math.abs(CROSS_POINT - crossPosition)) {
                                crossPosition = p;
                            }
                        } else {
                            crossPosition = p;
                        }
                    }
                    //выходные наборы
                    int[] childArrOne = new int[10];
                    int[] childArrTwo = new int[10];
                    //копируем части в новые наборы
                    System.arraycopy(parentOne.getGens(), 0, childArrOne, 0, crossPosition + 1);
                    System.arraycopy(parentTwo.getGens(), crossPosition + 1, childArrOne, crossPosition + 1, parentTwo.getGens().length - 1 - crossPosition);
                    System.arraycopy(parentTwo.getGens(), 0, childArrTwo, 0, crossPosition + 1);
                    System.arraycopy(parentOne.getGens(), crossPosition + 1, childArrTwo, crossPosition + 1, parentOne.getGens().length - 1 - crossPosition);
                    //на основе новых наборов создаем новые гены
                    Сhromosome genGhildOne = new Сhromosome(childArrOne, count);
                    Сhromosome genGhildTwo = new Сhromosome(childArrTwo, count);
                    genGhildOne.setResultFitness(fitnessFunction(genGhildOne.getGens()));
                    genGhildTwo.setResultFitness(fitnessFunction(genGhildTwo.getGens()));
                    //Добавляем новые гены в текущее поколение
                    crossList.add(genGhildOne);
                    crossList.add(genGhildTwo);
                }
            }
        }
        currentGeneration = new ArrayList<>(crossList);

        //Если набор вырождается -добавляем новые хромосомы
        if (currentGeneration.size()/POPULATION_SIZE < DEGRADATION_PERSCENT) {
            currentGeneration.addAll(generationListChromosome(POPULATION_SIZE - currentGeneration.size()));
        }
    }

    //Проводим мутацию
    private void mutation() {
        double random = Math.random();

        if (random <= MUTATION_PERCENT) {

            //выбираем любой ген
            int index = (int) (Math.random() * (currentGeneration.size() - 1));
            int[] chrosm = currentGeneration.get(index).getGens();

            //выбираем два значения с наборе
            int index1 = (int) (Math.random() * 9);
            int index2 = (int) (Math.random() * 9);
            while (index1 == index2) {
                index2 = (int) (Math.random() * 9);
            }

            //меняем местами
            chrosm[index1] = chrosm[index1] ^ chrosm[index2];
            chrosm[index2] = chrosm[index1] ^ chrosm[index2];
            chrosm[index1] = chrosm[index1] ^ chrosm[index2];

            //пересчитываем fitnessFunction
            currentGeneration.get(index).setResultFitness(fitnessFunction(currentGeneration.get(index).getGens()));
        }
    }

    //Запускаем алгоритм
    @Override
    public Сhromosome call() {
        firstGeneration();
        while (count < COUNT_GENERATION) {
            selection();
            cross();
            mutation();
            count++;
        }

        //возвращаем ген с min значением функции
        return getMinGen(currentGeneration);
    }


}
