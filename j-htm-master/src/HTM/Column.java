package HTM;

import java.util.List;
import java.util.ArrayList;

public class Column{

    public Integer x;
    public Integer y;
    List<Cell> cells;

   // public Segment proximalSegment;

    boolean isActive;
    Region region;
    /*
    overlap(c)  -   Значение перекрытия для колонки с в пространственном
группировщике для данного конкретного входа.
     */
    public double overlap;


    /*
    minOverlap -  Минимальное число активных входов колонки для ее участия
в шаге подавления.
     */
    public double minOverlap;
    /*
    boost(c) - Значение ускорения («агрессивности») для колонки c
вычисленное во время обучения – используется для
увеличения значения перекрытия для малоактивных
колонок
     */
    public double boost;
    /*
    potentialSynapses(c) - Список потенциальных синапсов и их значений
 перманентности
     */
    List<Synapse> potentialSynapses;
    /*
    connectedSynapses(c) - Подмножество потенциальных синапсов
potentialSynapses(c) у которых значение
перманентности больше чем connectedPerm. То есть это
прямые входные биты, которые подключены к колонке c.
     */
    List<Synapse> connectedSynapses;
    /*
    activeDutyCycle(c) Интервальное среднее показывающее как часто колонка c
    была активна после подавления
    */
    public double activeDutyCycle;
    /*
    overlapDutyCycle(c) Интервальное среднее показывающее как часто колонка c
    имела существенное значение перекрытия (т.е. большее чем
                                            minOverlap) со своим входом (то есть за последние 1000
    итераций).
    */
    public double overlapDutyCycle;
    /*
    minDutyCycle(c) Переменная представляющая минимальную желательную
    частоту активации (firing) для клетки. Если эта частота клетки
    упадет ниже данного значения, то она будет ускорена
            (boosted). Это значение определяется как 1% от
    максимальной частоты активации соседей клетки
    */
    public double minDutyCycle;

    public Column(Region region,Integer x, Integer y) {
        this.x = x;
        this.y = y;
        this.isActive = false;
        this.region = region;
        this.overlap = 0.0;
        this.boost = 1.0;
        this.activeDutyCycle = 0.0;
        this.overlapDutyCycle = 0.0;
        this.minDutyCycle = 0.0;

        cells = new ArrayList<Cell>();
        for (int i = 0; i < region.cellsPerColumn; i++)
        {
            cells.add(new Cell(i));
        }
        //proximalSegment = new Segment();

        potentialSynapses = new ArrayList<Synapse>();

        connectedSynapses = new ArrayList<Synapse>();

    }


    public List<Synapse> connectedSynapses() {
        List<Synapse> result = new ArrayList<>();
        for(Synapse synapse: this.potentialSynapses){
            if (synapse.permanence > region.connectedPerm)
                result.add(synapse);
        }
        return result;
    }

    /*
    Увеличивает значение перманентности всех синапсов колонки c на
    коэффициент умножения s.
     */
    public void increasePermanences(Double s) {
        for(Synapse syn: this.potentialSynapses) {
            syn.permanence = Math.min(syn.permanence + syn.permanence * s, 1.0) ;
        }
    }

    /*
    Возвращает значение ускорения колонки c. Это вещественное значение
    >= 1. Если activeDutyCyle(c) больше minDutyCycle(c), то значение
    ускорения = 1. Ускорение начинает линейно увеличиваться как только
    activeDutyCyle колонки падает ниже minDutyCycle.
     */
    public Double boostFunction() {
        return activeDutyCycle > minDutyCycle ? 1.0 : (1.0 + minDutyCycle * 100);
    }


}
