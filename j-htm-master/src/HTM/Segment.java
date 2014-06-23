package HTM;

import java.util.ArrayList;
import java.util.List;

public class  Segment {
    public List<Synapse> synapses;
    public Boolean sequenceSegment = false;

    public Segment()
    {
        synapses = new ArrayList<Synapse>();
        sequenceSegment = false;
    }
}
