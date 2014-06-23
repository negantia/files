package HTM;

import java.io.FileInputStream;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class Region {
    public List<Column> columns;

    public Integer xDimension;
    public Integer yDimension;
    public Integer square;

    //Число клеток в каждой из колонок.
    public Integer cellsPerColumn;

    /*
    Средний размер входного (рецепторного) поля колонок
     */
    public Double inhibitionRadius;

    /*
    Минимальное число активных входов колонки для ее участия
    в шаге подавления.
     */
    public Integer minOverlap;
    /*
    Параметр контролирующий число колонок победителей
    после шага подавления.
    */
    public Integer desiredLocalActivity;

    /*
    Если значение перманентности синапса больше данного параметра, то он считается подключенным (действующим).
     */
    public Double connectedPerm;
    /*
    Количество значений перманентности синапсов, которые
    были увеличены при обучении.
     */
    public Double permanenceInc;
    /*
    Количество значений перманентности синапсов, которые
    были уменьшены при обучении.
    */
    public Double permanenceDec;
    /*
    Порог активации для сегмента. Если число активных
    подключенных синапсов в сегменте больше чем
    activationThreshold, данный сегмент считается активным.
     */
    public Integer activationThreshold;

    /*
   Начальное значение перманентности для синапсов
    */
    public Double initialPerm;
    /*
    Минимальная активность в сегменте для обучения.
     */
    public Integer minThreshold;
    /*
    Максимальное число синапсов добавляемых сегменту при
    обучении.
     */
    public Integer newSynapseCount;
    ////////////////////////////////////////////////////////
    // загрузка свойств из файла
    private final Logger logger = LogManager.getLogger(Region.class.getSimpleName());
    private final String SP_PROP_FILENAME = "htm.properties";
    private String filePropName = SP_PROP_FILENAME;

    //////////////////////////////////////////////////////////
    public Region(){

        //xDimension = 10;
       // yDimension = 20;

        //inhibitionRadius = averageReceptiveFieldSize();

        /*cellsPerColumn = 4;
        minOverlap = 50;
        desiredLocalActivity = 20;
        connectedPerm = 0.2;
        permanenceInc = 0.1;
        permanenceDec = 0.1;*/
    }


    public void addColumns(){
        columns = new ArrayList<Column>();

        for (int i = 0;i < xDimension; i++) {
            for (int j = 0;j < yDimension; j++) {
                columns.add(new Column(this, i, j));
            }
        }
        square = xDimension * yDimension;
    }
    /*
    neighbors(c) -  Список колонок находящихся в радиусе подавления
inhibitionRadius колонки c.
     */
    public List<Integer> neighbours(int c) {
        List<Integer> result = new ArrayList<Integer>();
        for(int i=0;i<columns.size();i++) {
            if ((Math.abs(columns.get(i).x - columns.get(c).x) < inhibitionRadius) &&
                    (Math.abs(columns.get(i).y - columns.get(c).y) < inhibitionRadius))
                result.add(i);
        }
        return result;
    }

    /*
   Для заданного списка колонок возвращает их k-ое максимальное значение
их перекрытий со входом
    */
    public Double kthScore(List<Integer> cols, Integer k){
        Double[] overlaps = new Double[cols.size()];
        for(int i=0; i<cols.size(); i++) {
            overlaps[i] = columns.get(cols.get(i)).overlap;
        }
        Arrays.sort(overlaps);
        return overlaps[overlaps.length-k];
    }

    /*
    Средний радиус подключенных рецептивных полей всех колонок. Размер
    подключенного рецептивного поля колонки определяется только по
    подключенным синапсам (у которых значение перманентности >=
    connectedPerm). Используется для определения протяженности
    латерального подавления между колонками.
     */

    public Double averageReceptiveFieldSize() {
        Double xDistance;
        Double yDistance;
        Double result = 0.0;
        for(int i = 0; i < columns.size();i++) {
            xDistance = 0.0;
            yDistance = 0.0;
            for(Synapse synapse : columns.get(i).potentialSynapses) {
                if (synapse.permanence > connectedPerm) {
                    Double xCalculated = Math.abs(columns.get(i).x.doubleValue() - synapse.c.doubleValue());
                    Double yCalculated = Math.abs(columns.get(i).y.doubleValue() - synapse.i.doubleValue());
                    xDistance = xDistance > xCalculated ? xDistance : xCalculated;
                    yDistance = yDistance > yCalculated ? yDistance : yCalculated;
                }
            }
            result = (Math.sqrt(xDistance*xDistance + yDistance*yDistance) + i * result ) / (i+1);
        }
        return result;
    }

    /*
    Возвращает максимальное число циклов активности для всех заданных
    колонок.
     */
    public Double maxDutyCycle(List<Integer> cols) {
        Double max = 0.0;
        for(Integer col: cols) {
            if (max < columns.get(col).activeDutyCycle);
            max = columns.get(col).activeDutyCycle;
        }
        return max;
    }

    double GetMinLocalActivity(int i){
        return kthScore(neighbours(i), desiredLocalActivity);
    }

    public void loadProperties() throws RegionInitializationException {
        Properties properties = new Properties();
        //this.xDimension = 20;
        try {
            properties.load(new FileInputStream(filePropName));
            for (String name : properties.stringPropertyNames()) {
                switch (name) {
                    case "desiredLocalActivity":
                        this.desiredLocalActivity = Integer.parseInt(properties.getProperty(name));
                        break;
                    case "minOverlap":
                        this.minOverlap = Integer.parseInt(properties.getProperty(name));
                        break;
                    case "connectedPerm":
                        this.connectedPerm = Double.parseDouble(properties.getProperty(name));
                        break;
                    case "permanenceInc":
                        this.permanenceInc = Double.parseDouble(properties.getProperty(name));
                        break;
                    case "permanenceDec":
                        this.permanenceDec = Double.parseDouble(properties.getProperty(name));
                        break;
                    case "cellsPerColumn":
                        this.cellsPerColumn = Integer.parseInt(properties.getProperty(name));
                        break;
                    case "activationThreshold":
                        this.activationThreshold = Integer.parseInt(properties.getProperty(name));
                        break;
                    case "initialPerm":
                        this.initialPerm = Double.parseDouble(properties.getProperty(name));
                        break;
                    case "minThreshold":
                        this.minThreshold = Integer.parseInt(properties.getProperty(name));
                        break;
                    case "newSynapseCount":
                        this.newSynapseCount = Integer.parseInt(properties.getProperty(name));
                        break;
                    case "xDimension":
                        this.xDimension = Integer.parseInt(properties.getProperty(name));
                        break;
                    case "yDimension":
                        this.yDimension = Integer.parseInt(properties.getProperty(name));
                        break;
                    default:
                        logger.error("Illegal property name: " + name);
                        break;
                }
            }
            /*if (!synPermBelowStimulusIncInited)
                synPermBelowStimulusInc = synPermConnected / 10.0;
            if (!synPermTrimThresholdInited)
                synPermTrimThreshold = synPermActiveInc / 2.0; */
        } catch (IOException e) {
            throw new RegionInitializationException("Cannot load properties file " + filePropName, e);
        } catch (NumberFormatException nfe) {
            throw new RegionInitializationException("Wrong property value in property file " + filePropName, nfe);
        }
    }


    /*
    private void checkProperties() throws RegionInitializationException {
        if (numColumns <= 0)
            throw new RegionInitializationException("Column dimensions must be non zero positive values");
        if (numInputs <= 0)
            throw new RegionInitializationException("Input dimensions must be non zero positive values");
        if (numActiveColumnsPerInhArea <= 0 && (localAreaDensity <= 0 || localAreaDensity > 0.5))
            throw new RegionInitializationException("Or numActiveColumnsPerInhArea > 0 or localAreaDensity > 0 " +
                    "and localAreaDensity <= 0.5");
        if (potentialPct <= 0 || potentialPct > 1)
            throw new RegionInitializationException("potentialPct must be > 0 and <= 1");
        potentialRadius = potentialRadius > numInputs ? numInputs : potentialRadius;
    }
    */
}
