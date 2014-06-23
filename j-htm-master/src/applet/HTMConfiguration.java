package applet;

import HTM.CortexThread;
import HTM.RegionInitializationException;
import info.monitorenter.gui.chart.Chart2D;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class HTMConfiguration {
    private JTextField textField1;
    private JTextField textField2;
    private JPanel mainPanel;
    private JButton runCortexButton;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    private JTextField textField7;
    private JTextField textField8;
    private JTextField textField9;
    private JTextField textField10;
    private JTextField textField11;
    private JTextField textField12;
    public JTextPane textPane1;
    private Chart2D chart2D1;
    private JButton stopCortexButton;
    public JCheckBox showDendritesGraphCheckBox;
    public JCheckBox showSynapsesPermanenceCheckBox;
    public JCheckBox showActiveCellsCheckBox;
    public JCheckBox showPredictiveCellsCheckBox;
    public JCheckBox showLearningCellsCheckBox;
    public JCheckBox showOverlapsCheckBox;
    public JCheckBox showActiveDutyCycleCheckBox;
    public JCheckBox showMinDutyCycleCheckBox;
    public JCheckBox showBoostCheckBox;
    public JCheckBox showOverlapsDutyCycleCheckBox;
    private JButton makeStepButton;
    private JButton showExtendedGUIButton;
    public JCheckBox inputsGraphicsCheckBox;
    private JTabbedPane tabbedPane1;
    private Chart2D chart2D2;
    private JPanel casmiPanel;
    public JCheckBox drawDendritesTimlineCheckBox;
    private JButton LoadPropertiesFromFile;

    public CortexThread crtx = new CortexThread();
    static HTMConfiguration panel;

    public HTMConfiguration () {
        runCortexButton.addActionListener(new RunCortexButtonListener());
        stopCortexButton.addActionListener(new StopCortexButtonListener());
        makeStepButton.addActionListener(new MakeStepButtonListener());
        showExtendedGUIButton.addActionListener(new ShowExtendedGUIListener());
        LoadPropertiesFromFile.addActionListener(new LoadPropertiesButtonGUIListener());
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("HTMConfiguration");
        panel = new HTMConfiguration();
        frame.setContentPane(panel.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void InitCortex() {
        //crtx = new CortexThread();
        try {
            crtx.r.region.desiredLocalActivity = new Integer(textField1.getText());
            crtx.r.region.minOverlap = new Integer(textField2.getText());
            crtx.r.region.connectedPerm = new Double(textField3.getText());
            crtx.r.region.permanenceInc = new Double(textField4.getText());
            crtx.r.region.permanenceDec = new Double(textField5.getText());
            crtx.r.region.cellsPerColumn = new Integer(textField6.getText());
            crtx.r.region.activationThreshold = new Integer(textField7.getText());
            crtx.r.region.initialPerm = new Double(textField8.getText());
            crtx.r.region.minThreshold = new Integer(textField9.getText());
            crtx.r.region.newSynapseCount = new Integer(textField10.getText());
            crtx.r.region.xDimension = new Integer(textField11.getText());
            crtx.r.region.yDimension = new Integer(textField12.getText());
        } catch (Exception e) {
            System.out.print(e.getMessage());
        }
        crtx.r.region.addColumns();
        crtx.Init(chart2D1, chart2D2, panel);
    }

    public class LoadPropertiesButtonGUIListener implements ActionListener  {
        //crtx = new CortexThread();
        public void actionPerformed (ActionEvent event) {
            try{
                crtx.r.region.loadProperties();
                textField1.setText(crtx.r.region.desiredLocalActivity.toString());
                textField2.setText(crtx.r.region.minOverlap.toString());
                textField3.setText(crtx.r.region.connectedPerm.toString());
                textField4.setText(crtx.r.region.permanenceInc.toString());
                textField5.setText(crtx.r.region.permanenceDec.toString());
                textField6.setText(crtx.r.region.cellsPerColumn.toString());
                textField7.setText(crtx.r.region.activationThreshold.toString());
                textField8.setText(crtx.r.region.initialPerm.toString());
                textField9.setText(crtx.r.region.minThreshold.toString());
                textField10.setText(crtx.r.region.newSynapseCount.toString());
                textField11.setText(crtx.r.region.xDimension.toString());
                textField12.setText(crtx.r.region.yDimension.toString());
            }
            catch (RegionInitializationException e){
                    System.out.println("caught " + e);
            }
        }

    }

    public class RunCortexButtonListener implements ActionListener {
        public void actionPerformed (ActionEvent event) {
            if (!crtx.isRunning()) {
                InitCortex();
                crtx.start();
            }
            else
                crtx.Continue();
        }
    }

    private class StopCortexButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            crtx.Quit();
        }
    }

    private class MakeStepButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (crtx.isRunning())
                crtx.MakeStep();
            else {
                InitCortex();
                crtx.MakeStep();
            }
        }
    }

    private class ShowExtendedGUIListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFrame frame = new JFrame("Extended GUI");
            ExtensionGUI panel = new ExtensionGUI();
            frame.setContentPane(panel.extensionGUI);
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);

//              CasmiApplet.launch(crtx.region);
        }
    }
}
