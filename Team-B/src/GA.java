
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class GA {

    private Data data;
    private Individual[] population;
    private Individual[] auxPopulation;
    private int populationSize;
    private int chromeSize; //Número de grupos
    private int selectionPercent;
    private float mutationProbability;
    private int bestPosition;
    private Random random = new Random(System.currentTimeMillis());

    public GA(Data data, int populationSize, int selectionPercent, float mutationProbability) {
        this.data = data;
        this.populationSize = populationSize;
        this.population = new Individual[populationSize];
        this.chromeSize = data.getNumGroups();
        this.selectionPercent = selectionPercent;
        this.mutationProbability = mutationProbability;
    }

    public void initialPopulation() {
        for (int i = 0; i < populationSize; i++) {
            population[i] = individualGeneration();
        }
    }

    private Individual individualGeneration() {
        Individual individual = new Individual();

        individual.setGenes(data.randomGroups());
        individual.setRawFitness(fitness(individual.getGenes()));
        return individual;
    }

    private float fitness(int[][] genes) {
        float d;

        d = data.D(genes);
        return d;
    }

    public void checkFitnessMinimize() {
// opcion uno: la del libro
//        double cmax = -1;
//        for (int c = 0; c < populationSize; c++) {
//            if (population.get(c).getRawFitness() > cmax) {
//                cmax = population.get(c).getRawFitness();
//            }
//        }
//        cmax += cmax / 100;
//        for (int c = 0; c < populationSize; c++) {
//            population.get(c).setFitness(cmax - population.get(c).getRawFitness());
//        }

// opcion dos: la de internet (en los favoritos)
//        for (int c = 0; c < populationSize; c++) {
//            population.get(c).setFitness(1 / (1 + population.get(c).getRawFitness()));
//        }
// opcion tres: la sugerida en el libro que esta lo de la ruleta
        float rawFitness; //fitness sin normalización
        float vMin = 99, vMax = -99;
        float normV; //rawFitness normalizado

        for (int i = 0; i < populationSize; i++) {
            rawFitness = population[i].getRawFitness();
            if (rawFitness < vMin) {
                vMin = rawFitness;
            }
            if (rawFitness > vMax) {
                vMax = rawFitness;
            }
        }
        if (vMax == vMin) {
            vMax += vMax / 100;
            vMin -= vMin / 100;
        }
        for (int i = 0; i < populationSize; i++) {
            normV = (vMax - population[i].getRawFitness()) / (vMax - vMin);
            population[i].setFitness(normV);
        }
    }

    public void evaluation() {
        float cumScore = 0; // puntuacion acumulada de los individuos
        float bestFitness = 0; // mejor adaptacion
        float sumFitness = 0; //adaptación de toda la población

        for (int i = 0; i < populationSize; i++) {
            sumFitness += population[i].getFitness();
            if (population[i].getFitness() > bestFitness) {
                bestPosition = i;
                bestFitness = population[i].getFitness();
            }
        }
        for (int i = 0; i < populationSize; i++) {
            population[i].setScore(population[i].getFitness() / sumFitness);
            population[i].setCumScore(population[i].getScore() + cumScore);
            cumScore += population[i].getScore();
        }
    }

    private void evaluationAux(ArrayList<Individual> pop) {
        float cumScore = 0; // puntuacion acumulada de los individuos
        float sumFit = 0;

        sumFit = 0;
        for (int i = 0; i < pop.size(); i++) {
            sumFit += pop.get(i).getFitness();
        }
        for (int i = 0; i < pop.size(); i++) {
            pop.get(i).setScore(pop.get(i).getFitness() / sumFit);
            pop.get(i).setCumScore(pop.get(i).getScore() + cumScore);
            cumScore += pop.get(i).getScore();
        }
    }

    public void rouletteWheelW(int num) {
        double prob; // probabilidad de selección
        int selPos; // posición del seleccionado
        ArrayList<Individual> auxPop = new ArrayList(Arrays.asList(population));

        population = new Individual[populationSize];
        //El número de disponibles para reproducción se hace par...
        if (!((populationSize - num) % 2 == 0)) {
            num++;
        }
        for (int i = 0; i < num; i++) {
            prob = random.nextDouble();
            selPos = 0;
            while ((prob > auxPop.get(selPos).getCumScore()) && (selPos < num)) {
                selPos++;
            }
            population[i] = auxPop.get(selPos);
            auxPop.remove(selPos);
            evaluationAux(auxPop);
        }
        // se genera la población para reproducción
        auxPopulation = new Individual[auxPop.size()];
        auxPopulation = auxPop.toArray(auxPopulation);
    }

    private Individual[] rouletteWheel2R() {
        double prob; // probabilidad de selección
        int selPos; // posición del seleccionado
        Individual[] tmp = new Individual[2];

        for (int i = 0; i < 2; i++) {
            prob = random.nextDouble();
            selPos = 0;
            while ((prob > auxPopulation[selPos].getCumScore()) && (selPos < 2)) {
                selPos++;
            }
            tmp[i] = auxPopulation[selPos];
        }
        return tmp;
    }

    public void reproduction() {
        Individual[] fathers = new Individual[2];
        Individual son1 = new Individual();
        Individual son2 = new Individual();
        int[] crossPoints = new int[chromeSize];

        // se cruzan los individuos elegidos en puntos al azar
        for (int i = 0; i < chromeSize; i++) {
            crossPoints[i] = random.nextInt(data.getGroupSize() + 1);
        }
        for (int i = populationSize - auxPopulation.length; i < populationSize; i += 2) {
            fathers = rouletteWheel2R();
            C1Operator(fathers[0], fathers[1], son1, son2, crossPoints);//
            // los individuos nuevos sustituyen a sus progenitores
            population[i] = son1;
            population[i + 1] = son2;
        }
    }

    private void C1Operator(Individual father1, Individual father2, Individual son1, Individual son2, int[] crossPoints) {
        Integer[] father1a = data.matrixToArray(father1.getGenes());
        Integer[] father2a = data.matrixToArray(father2.getGenes());
        int[][] sonGenes1 = new int[chromeSize][data.getGroupSize()];
        int[][] sonGenes2 = new int[chromeSize][data.getGroupSize()];
        Integer[] son1a;
        Integer[] son2a;
        int lastEmpty = 0;

        for (int i = 0; i < crossPoints.length; i++) {
            for (int j = 0; j < crossPoints[i]; j++) {
                sonGenes1[i][j] = father1.getGenes()[i][j];
                sonGenes2[i][j] = father2.getGenes()[i][j];
            }
        }
        son1a = data.matrixToArray(sonGenes1);
        son2a = data.matrixToArray(sonGenes2);
        for (int i = 0; i < father2a.length; i++) {
            if (!Arrays.asList(son1a).contains(father2a[i])) {
                for (int j = lastEmpty; j < son1a.length; j++) {
                    if (son1a[j] == 0) {
                        lastEmpty = j;
                        break;
                    }
                }
                son1a[lastEmpty] = father2a[i];
            }
        }
        lastEmpty = 0;
        for (int i = 0; i < father1a.length; i++) {
            if (!Arrays.asList(son2a).contains(father1a[i])) {
                for (int j = lastEmpty; j < son2a.length; j++) {
                    if (son2a[j] == 0) {
                        lastEmpty = j;
                        break;
                    }
                }
                son2a[lastEmpty] = father1a[i];
            }
        }
        sonGenes1 = data.arrayToMatrix(son1a, chromeSize, data.getGroupSize());
        sonGenes2 = data.arrayToMatrix(son2a, chromeSize, data.getGroupSize());
        son1.setGenes(sonGenes1);
        son2.setGenes(sonGenes2);
        son1.setRawFitness(fitness(son1.getGenes()));
        son2.setRawFitness(fitness(son2.getGenes()));
    }

    public void mutation() {
        double prob;
        boolean mutated;

        for (int i = 0; i < populationSize; i++) {
            mutated = false;
            prob = random.nextDouble();
            if (prob < mutationProbability) {
                int genRnd1 = random.nextInt(chromeSize);
                int genRnd2 = random.nextInt(chromeSize);
                int alleleRnd1 = random.nextInt(data.getGroupSize());
                int alleleRnd2 = random.nextInt(data.getGroupSize());
                int auxAllele = population[i].getGenes()[genRnd1][alleleRnd1];
                while (genRnd1 == genRnd2) {
                    genRnd2 = random.nextInt(chromeSize);
                }
                population[i].getGenes()[genRnd1][alleleRnd1] = population[i].getGenes()[genRnd2][alleleRnd2];
                population[i].getGenes()[genRnd2][alleleRnd2] = auxAllele;
                mutated = true;
            }
            if (mutated) {
                population[i].setRawFitness(fitness(population[i].getGenes()));
            }
        }
    }

    public Individual[] getPopulation() {
        return population;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public int getSelectionPercent() {
        return selectionPercent;
    }

    public int getBestPosition() {
        return bestPosition;
    }

}
