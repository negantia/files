package HTM;

import java.util.ArrayList;
import java.util.List;

public class Cell {
    int index;
    //int column;

    /*
    Для каждой клетки мы вводим три различных состояния.
    Массивы activeState и predictiveState хранят в себе записи об активных состояниях и
    состояниях предсказания (предчувствия) для каждой из клеток в каждый из моментов времени.
    Массив learnState определяет какие клетки используются во время обучения.
     */
    ArrayList<Boolean> learnState;
    ArrayList<Boolean> predictiveState;
    /*
    activeState(c, i, t) это вклад от клетки
    колонки c с номером i во время t. Если это 1, то клетка
    активна при данном прямом входе и временном контексте.
     */
    ArrayList<Boolean> activeState;
    List<Segment> dendriteSegments;

    /*
   Список структур segmentUpdate. segmentUpdateList(c,i) это
   список изменений для клетки i в колонке c.
    */
    List<SegmentUpdate> segmentUpdateList;

    public Cell(int i)
    {
       // column = c;
        index = i;
        dendriteSegments = new ArrayList<Segment>();
        segmentUpdateList = new ArrayList<SegmentUpdate>();
        learnState = new ArrayList<Boolean>();
        predictiveState = new ArrayList<Boolean>();
        activeState = new ArrayList<Boolean>();
    }
}