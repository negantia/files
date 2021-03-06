package HTM;

import applet.HTMConfiguration;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.traces.Trace2DSimple;
import info.monitorenter.gui.chart.traces.painters.TracePainterDisc;

import java.awt.*;
import java.util.ArrayList;

public class ChartHandler {
    private Chart2D chart2D1;
    private Chart2D chart2D2;
    private HTMConfiguration cfg;
    Boolean showDistalSegmentsCount = false;
    Boolean drawTimeline = false;
    Boolean perm = false;
    Boolean act = false;
    Boolean predict = false;
    Boolean learn = false;
    Boolean over = false;
    Boolean adc = false;
    Boolean mdc = false;
    Boolean odc = false;
    Boolean bst = false;
    Boolean inp = false;

    ITrace2D traceA = new Trace2DSimple("Activity");
    ITrace2D traceL = new Trace2DSimple("Learn");
    ITrace2D traceP = new Trace2DSimple("Predictive");
    ITrace2D traceD = new Trace2DSimple("Dendrite Segments");
    ITrace2D traceS = new Trace2DSimple("Permanences");
    ITrace2D traceO = new Trace2DSimple("Overlaps");
    ITrace2D traceADC = new Trace2DSimple("Active Duty Cycle");
    ITrace2D traceMDC = new Trace2DSimple("Min Duty Cycle");
    ITrace2D traceODC = new Trace2DSimple("Overlap Duty Cycle");
    ITrace2D traceBST = new Trace2DSimple("Column Boost");
    ITrace2D traceINP = new Trace2DSimple("Inputs Graphic");
    ITrace2D traceTMLN = new Trace2DSimple("Progress in Time");

    public ChartHandler( Chart2D chart1, Chart2D chart2, HTMConfiguration configuration) {
        this.chart2D1 = chart1;
        this.chart2D2 = chart2;
        this.cfg = configuration;
        showDistalSegmentsCount = cfg.showDendritesGraphCheckBox.isSelected();
        perm = cfg.showSynapsesPermanenceCheckBox.isSelected();
        act = cfg.showActiveCellsCheckBox.isSelected();
        predict = cfg.showPredictiveCellsCheckBox.isSelected();
        learn = cfg.showLearningCellsCheckBox.isSelected();
        over = cfg.showOverlapsCheckBox.isSelected();
        adc = cfg.showActiveDutyCycleCheckBox.isSelected();
        mdc = cfg.showMinDutyCycleCheckBox.isSelected();
        odc = cfg.showOverlapsDutyCycleCheckBox.isSelected();
        bst = cfg.showBoostCheckBox.isSelected();
        inp = cfg.inputsGraphicsCheckBox.isSelected();
        drawTimeline = cfg.drawDendritesTimlineCheckBox.isSelected();

        this.chart2D1.removeAllTraces();
        //this.chart2D2.removeAllTraces();
        if (act) {
            chart2D1.addTrace(traceA);
            traceA.setColor(Color.CYAN);
            traceA.setTracePainter(new TracePainterDisc(4));
        }
        if (learn) {
            chart2D1.addTrace(traceL);
            traceL.setColor(Color.MAGENTA);
            traceL.setTracePainter(new TracePainterDisc(4));
        }
        if (predict) {
            chart2D1.addTrace(traceP);
            traceP.setColor(Color.BLUE);
            traceP.setTracePainter(new TracePainterDisc(4));
        }
        if (showDistalSegmentsCount) {
            chart2D1.addTrace(traceD);
            traceD.setTracePainter(new TracePainterDisc(4));
        }
        if (drawTimeline)
            chart2D2.addTrace(traceTMLN);
        if (perm) {
            chart2D1.addTrace(traceS);
            traceS.setColor(Color.BLUE);
            traceS.setTracePainter(new TracePainterDisc(4));
        }
        if (over) {
            chart2D1.addTrace(traceO);
            traceO.setColor(Color.RED);
            traceO.setTracePainter(new TracePainterDisc(4));
        }
        if (adc) {
            chart2D1.addTrace(traceADC);
            traceADC.setColor(Color.GREEN);
            traceADC.setTracePainter(new TracePainterDisc(4));
        }
        if (mdc) {
            chart2D1.addTrace(traceMDC);
            traceMDC.setColor(Color.GRAY);
            traceMDC.setTracePainter(new TracePainterDisc(4));
        }
        if (odc) {
            chart2D1.addTrace(traceODC);
            traceODC.setColor(Color.ORANGE);
            traceODC.setTracePainter(new TracePainterDisc(4));
        }
        if (bst) {
            chart2D1.addTrace(traceBST);
            traceBST.setColor(Color.DARK_GRAY);
            traceBST.setTracePainter(new TracePainterDisc(4));
        }
        if (inp) {
            chart2D1.addTrace(traceINP);
            traceINP.setColor(Color.RED);
            traceINP.setTracePainter(new TracePainterDisc(4));
        }
    }

    public void CollectData() {
        for(ITrace2D trace2D: chart2D1.getTraces()) {
            trace2D.removeAllPoints();
        }
        Integer time = cfg.crtx.r.time - 1 > 0 ? cfg.crtx.r.time - 1 : 0;
        if (inp) {
            for (int i = 0; i < cfg.crtx.r.region.xDimension; i++) {
                for(int j = 0; j < cfg.crtx.r.region.xDimension; j++) {
                    traceINP.addPoint(i+j, cfg.crtx.r.input(time, i, j));
                }
            }
        }
        String buf = "";
        int overalDSCount = 0;
        buf += "Cells Activity: \r\n" + "Timestep: " + cfg.crtx.r.totalTime + "\r\n";
        buf += "Inhibition Radius: " + cfg.crtx.r.region.inhibitionRadius + "\r\n";
        if (cfg.crtx.r.activeColumns.size() > 0)
            buf += "Active Columns: " + cfg.crtx.r.activeColumns.
                    get(cfg.crtx.r.time-1 > 0 ? cfg.crtx.r.time-1 : 0).size() + "\r\n";
//                            textPane1.setText(buf + region.dendriteSegments.toString() + "\r\n");
//                            textPane1.setText(buf + region.learnState.get(region.time).toString() + "\r\n");
//                            for(int i=0;i<region.xDimension*region.yDimension;i++)
//                                buf += region.overlap[i] + " ";
        for(int c = 0; c < cfg.crtx.r.region.xDimension*cfg.crtx.r.region.yDimension;c++) {
            if (over) {
                traceO.addPoint(c, cfg.crtx.r.region.columns.get(c).overlap);
            }
            if (adc) {
                traceADC.addPoint(c, cfg.crtx.r.region.columns.get(c).activeDutyCycle);
            }
            if (mdc) {
                traceMDC.addPoint(c, cfg.crtx.r.region.columns.get(c).minDutyCycle);
            }
            if (odc) {
                traceODC.addPoint(c, cfg.crtx.r.region.columns.get(c).overlapDutyCycle);
            }
            if (bst) {
                traceBST.addPoint(c, cfg.crtx.r.region.columns.get(c).boost);
            }
            for (int i = 0; i < cfg.crtx.r.region.cellsPerColumn; i++) {
                Boolean val;
                if (act) {
                    val = cfg.crtx.r.region.columns.get(c).cells.get(i).activeState.get(time);
                    traceA.addPoint(c, val ? i+1 * 1.0: 0.0);
                }
                if (learn) {
                    val = cfg.crtx.r.region.columns.get(c).cells.get(i).learnState.get(time);
                    traceL.addPoint(c, val ? i+1 * 1.0: 0.0);
                }
                if (predict) {
                    val = cfg.crtx.r.region.columns.get(c).cells.get(i).predictiveState.get(time);
                    traceP.addPoint(c, val ? i+1 * 1.0: 0.0);
                }

                Integer size = cfg.crtx.r.region.columns.get(c).cells.get(i).dendriteSegments.size();  //!!!
                overalDSCount += size;
                if (showDistalSegmentsCount) {
                    traceD.addPoint(c, i+1 * size);
                    buf += "C: " + c + " I: " + i + " N: " + size + " L: " +
                            cfg.crtx.r.region.columns.get(c).cells.get(i).learnState + " # ";
                }
            }
            if (perm) {
                Integer activeSynapses = 0;
                for (int s=0;s<cfg.crtx.r.region.xDimension*cfg.crtx.r.region.yDimension;s++) {
                    activeSynapses += cfg.crtx.r.region.columns.get(c).potentialSynapses.get(s).permanence >
                            cfg.crtx.r.region.connectedPerm ? 1: 0;
                }
                traceS.addPoint(c, activeSynapses);
            }
            buf += "\r\n";
        }
        if (drawTimeline)
            traceTMLN.addPoint(cfg.crtx.r.totalTime, overalDSCount);
        buf += "Overall Dendrite Segments Count: " + overalDSCount + "\r\n";
        cfg.textPane1.setText(buf);
    }
}
