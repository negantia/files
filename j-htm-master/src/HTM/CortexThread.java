package HTM;

import applet.HTMConfiguration;
import info.monitorenter.gui.chart.Chart2D;

public class CortexThread extends Thread{
    public Cortex r;// = new Cortex();
    public ChartHandler chartHandler;
    private Boolean runs = false;
    private Boolean pause = false;
    private Boolean makeStep = false;

    public CortexThread () {
       r = new Cortex();
    }

    public void Init(Chart2D chart1, Chart2D chart2, HTMConfiguration configuration) {
        r.SInitialization();
        chartHandler = new ChartHandler(chart1, chart2, configuration);
    }

    public void run() {
        this.runs = true;
        while (runs) {
            if (!pause) {
                r.SOverlap();
                r.SInhibition();
                r.SLearning();
                r.TCellStates();
                r.TPredictiveStates();
                r.TLearning();
                chartHandler.CollectData();
                r.timestep();
                if (makeStep) {
                    pause = true;
                    makeStep = false;
                }
            }
        }
    }

    public Boolean isRunning() {
        return runs;
    }
    public Boolean isPaused() {
        return pause;
    }

    public void MakeStep() {
        if (!runs)
            this.start();
        this.pause = false;
        this.makeStep = true;
    }

    public void Continue() {
        this.pause = false;
    }

    public void Quit() {
        this.runs = false;
    }
}
