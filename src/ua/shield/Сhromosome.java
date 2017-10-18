package ua.shield;

import java.util.Arrays;

/**
 * Created by sa on 05.10.17.
 */

public class Сhromosome {
    private int[] gens; //наборо генов
    private int resultFitness; //результат фитнесфункции
    private int generation;

    public Сhromosome(Сhromosome chromos,int generation) {
        this.gens =chromos.getGens();
        this.resultFitness=chromos.getResultFitness();
        this.generation=generation;
    }

    public Сhromosome(int[] gens, int generation) {
        this.gens = gens;
        this.generation = generation;
    }

    public int[] getGens() {
        return gens;
    }


    public int getResultFitness() {
        return resultFitness;
    }

    public void setResultFitness(int resultFitness) {
        this.resultFitness = resultFitness;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Сhromosome chromosome = (Сhromosome) o;

        if (resultFitness != chromosome.resultFitness) return false;
        return Arrays.equals(gens, chromosome.gens);

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(gens);
        result = 31 * result + resultFitness;
        return result;
    }

    @Override
    public String toString() {
        return "Сhromosome{" +
                "resultFitness=" + resultFitness +
                '}';
    }
}
