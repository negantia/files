package HTM;

public class Synapse {
    public Double permanence;

    public Integer c;
    public Integer i;

    public Synapse(Integer c, Integer i, Double permanence) {
        this.permanence = permanence;
        this.c = c;
        this.i = i;
    }
}