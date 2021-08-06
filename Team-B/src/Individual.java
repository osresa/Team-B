
public class Individual {

    private int[][] genes;
    private float fitness; //función de aptitud
    private float rawFitness; //antes de transformarla
    private float score; //puntuación relativa (fitness/sumadaptación)
    private float cumScore; //puntuación acumulada para sorteos

    public Individual() {
    }

    public int[][] getGenes() {
        return genes;
    }

    public void setGenes(int[][] genes) {
        this.genes = genes;
    }

    public float getFitness() {
        return fitness;
    }

    public void setFitness(float fitness) {
        this.fitness = fitness;
    }

    public float getRawFitness() {
        return rawFitness;
    }

    public void setRawFitness(float rawFitness) {
        this.rawFitness = rawFitness;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public float getCumScore() {
        return cumScore;
    }

    public void setCumScore(float cumScore) {
        this.cumScore = cumScore;
    }

}
