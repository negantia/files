package HTM;

import java.util.List;

public class SegmentUpdate {

    public Integer[] segmentIndex;
    public List<Synapse> activeSynapses;
    public Boolean sequenceSegment = false;

    public SegmentUpdate(Integer[] segmentIndex, List<Synapse> activeSynapses) {
        this.segmentIndex = segmentIndex;
        this.activeSynapses = activeSynapses;
    }
}
