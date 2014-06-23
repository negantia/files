package HTM;

import applet.ExtensionGUI;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;


public class Cortex {
    public Integer time = 0;
    public Integer totalTime = 0;

    // Список всех колонок
    public Region region =  new Region();

    // Список потенциальных синапсов и их значений перманентности  (для всего региона , для каждой колонки)
    // public LinkedList<LinkedList<Synapse>> potentialSynapses = new LinkedList<LinkedList<Synapse>>();

    public List<Integer[][]> inputBits;

        /*Список индексов колонок – победителей благодаря прямым
        входным данным. (Выход пространственного группировщика)
       */
    public List<List<Integer>> activeColumns;

    public enum State {
        active,
        learn
    }

    /////////////////////////////////////////////////////////////////////////
    //  Реализация
    /////////////////////////////////////////////////////////////////////////

    public Cortex() {

    }

    /////////////////////////////////////////////////////////////////////////

    public Integer input(Integer t, Integer j, Integer k) {
        //return Math.sin(j+k+totalTime) > 0 ? 1 : 0;
        //return rnd.nextInt(2);
        if (ExtensionGUI.Input == null)
            return t % 2 > 0 ? rnd.nextInt(2) : Math.sin(j+k+totalTime) > 0 ? 1 : 0;
        else {
            byte[] buffer = ExtensionGUI.Input;
            int l = buffer.length;
            int width = l / region.xDimension;
            int height = 256 / region.yDimension;
            int amount = 0;
            for (int i = j * width; i < (j+1)*width; i++) {
                if ((k+1)*height - 128 < buffer[j] && buffer[j] > k*height - 128)
                    amount ++;
            }
            return amount > width / 10 ? 1 : 0;
        }
    }

   /*
   Вычисляет интервальное среднее того, как часто колонка c была активной
    после подавления.
    */
    public Double updateActiveDutyCycle(Integer c) {
        Double value = 0.0;
        for(Integer idx: activeColumns.get(time))
            if (c.equals(idx)) {
                value = 1.0;
                break;
            }
        return (value + totalTime.floatValue() * region.columns.get(c).activeDutyCycle) / (totalTime.floatValue() + 1.0);
    }

    ////////////////
    boolean[][] get2DcolsANDcellsAtT(State state ,Integer t){
        boolean[][] list = new boolean[region.columns.size()][];
        for (int col = 0 ; col < region.columns.size(); col++){
            list[col] = new boolean[region.columns.get(col).cells.size()];
            for (int icell = 0; icell < region.columns.get(col).cells.size(); icell++)
            {
                if (state.equals(State.active)){
                    list[col][icell] = region.columns.get(col).cells.get(icell).activeState.get(t);
                }
                else{
                    list[col][icell] = region.columns.get(col).cells.get(icell).learnState.get(t);
                }

            }
        }
        return list;
    }

    /*
    Эта процедура возвращает true, если число подключенных синапсов
    сегмента s, которые активны благодаря заданным состояниям в момент t,
    больше чем activationThreshold. Вид состояний state может быть
    activeState, или learnState.
     */
    public Boolean segmentActive(Segment s, Integer t, State state) {

        boolean[][] list = get2DcolsANDcellsAtT(state, t);

        Integer counter = 0;
        for(Synapse syn: s.synapses){

            if(list[syn.c][syn.i] && syn.permanence > region.connectedPerm) {
                counter++;
            }
        }
        return counter > region.activationThreshold;
    }

    /*
    Для данной клетки i колонки c, возвращает индекс сегмента такого, что
    segmentActive(s,t, state) равно true. Если активны несколько сегментов,
    то сегментам последовательностей отдается предпочтение. В противном
    случае предпочтение отдается сегментам с наибольшей активностью.
     */
    public Integer[] getActiveSegment(Integer c, Integer i, Integer t, State state) {
        List<Segment> activeSegments = new ArrayList<Segment>();
        for(Segment segment: region.columns.get(c).cells.get(i).dendriteSegments) {
            if (segmentActive(segment, t, state))
                activeSegments.add(segment);
        }

        if (activeSegments.size() == 1) {
            return new Integer[]{c, i, region.columns.get(c).cells.get(i).dendriteSegments.indexOf(activeSegments.get(0))};

        } else {
            for(Segment seg: activeSegments) {
                if (seg.sequenceSegment)
                    return new Integer[]{c, i, region.columns.get(c).cells.get(i).dendriteSegments.indexOf(seg)};

            }

            boolean[][] list= get2DcolsANDcellsAtT(state, t);
            Integer maxActivity = 0;
            Integer result = -1;
            for(int j = 0; j < activeSegments.size(); j++) {
                Integer counter = 0;
                for(Synapse syn: activeSegments.get(j).synapses) {
                    if(list[syn.c][syn.i] && syn.permanence > region.connectedPerm) {
                        counter++;
                    }
                }
                if (maxActivity < counter) {
                    maxActivity = counter;
                    result = j;
                }
            }
            return new Integer[]{c, i, result};
        }
    }

    /*
    Для данной клетки i колонки c в момент t, находит сегмент с самым
    большим числом активных синапсов. Т.е. она ищет наилучшее
    соответствие. При этом значения перманентности синапсов допускаются и
    ниже порога connectedPerm. Число активных синапсов допускается ниже
    порога activationThreshold, но должно быть выше minThreshold. Данная
    процедура возвращает индекс сегмента. А если такого не обнаружено, то
    возвращается -1.
     */
    public Integer[] getBestMatchingSegment(Integer c, Integer i, Integer t) {

        boolean[][] list = get2DcolsANDcellsAtT(State.active, t);
        Integer maxActivity = 0;
        Integer result = -1;
        for(int j = 0; j < region.columns.get(c).cells.get(i).dendriteSegments.size(); j++) {
            Integer counter = 0;
            Segment segment = region.columns.get(c).cells.get(i).dendriteSegments.get(j);

            for(Synapse syn: segment.synapses) {
                if(list[syn.c][syn.i]) {
                    counter++;
                }
            }
            if (maxActivity < counter) {
                maxActivity = counter;
                result = j;
            }
        }
        return maxActivity > region.minThreshold ? new Integer[]{c, i, result} : new Integer[]{c, i, -1};
    }

    /*
    Для данной колонки возвращает клетку с самым соответствующим входу
    сегментом (как это определено выше). Если такой клетки нет, то
    возвращается клетка с минимальным числом сегментов.
     */
    public Integer[] getBestMatchingCell(Integer c, Integer t) {
        Integer minSegments = null;
        Integer cellIndex = -1;
        Integer minSegmentsCellIndex = -1;

        boolean[][] list = get2DcolsANDcellsAtT(State.active, t);
        Integer maxActivity = 0;
        Integer result = -1;

        for(int i=0;i< region.cellsPerColumn;i++) {
            for(int j = 0; j < region.columns.get(c).cells.get(i).dendriteSegments.size() ; j++) {
                Integer counter = 0;
                Segment segment = region.columns.get(c).cells.get(i).dendriteSegments.get(j);

                for(Synapse syn: segment.synapses) {
                    if(list[syn.c][syn.i]) {
                        counter++;
                    }
                }
                if (maxActivity < counter) {
                    maxActivity = counter;
                    result = j;
                    cellIndex = i;
                }
            }
            if (minSegments == null || minSegments > region.columns.get(c).cells.get(i).dendriteSegments.size()) {
                minSegments = region.columns.get(c).cells.get(i).dendriteSegments.size();
                minSegmentsCellIndex = i;
            }
        }
        return maxActivity > region.minThreshold ? new Integer[]{c, cellIndex, result} : new Integer[]{c, minSegmentsCellIndex, -1};
    }

    /*
    Возвращает структуру данных segmentUpdate, содержащую список
    предлагаемых изменений для сегмента s. Пусть activeSynapses список
    активных синапсов у исходных клеток которых activeState равно 1 в
    момент времени t. (Этот список будет пустым если s равно -1 при не
    существующем сегменте.) newSynapses это опциональный параметр, по
    умолчанию равный false. А если newSynapses равно true, тогда число
    синапсов, равное newSynapseCount - count(activeSynapses),
    добавляется к активным синапсам activeSynapses. Такие синапсы
    случайно выбираются из числа клеток, у которых learnState равно 1 в
    момент времени t.
     */
    public SegmentUpdate getSegmentActiveSynapses(Integer c, Integer i, Integer t, Integer s, Boolean newSynapses) {
        List<Synapse> activeSynapses = new ArrayList<>();
        if (s >= 0) {
            for(Synapse syn: region.columns.get(c).cells.get(i).dendriteSegments.get(s).synapses) {
                if (region.columns.get(syn.c).cells.get(syn.i).activeState.get(t)) {
                     activeSynapses.add(syn);
                }
            }
        }
        if (newSynapses) {
            Random r = new Random();
            List<Integer[]> learningCells = new ArrayList<>();
            for (int j = 0; j < region.columns.size(); j++) {
                for (int k = 0; k < region.columns.get(j).cells.size(); k++) {
                    if (region.columns.get(j).cells.get(k).learnState.get(t) && !(c.equals(j) && i.equals(k))) {
                        learningCells.add(new Integer[]{j, k});
                    }
                }
            }
            for (int k=0; k < region.newSynapseCount - activeSynapses.size(); k++) {
                Integer[] idx;
                idx = learningCells.get(r.nextInt(learningCells.size()));
                activeSynapses.add(new Synapse(idx[0], idx[1], region.initialPerm));
            }
        }
        return new SegmentUpdate(new Integer[]{c, i, s}, activeSynapses);
    }

    /*
        Эта функция проходит по всему списку	segmentUpdate	и усиливает
    каждый сегмент. Для каждого элемента segmentUpdate делаются
    следующие изменения. Если positiveReinforcement равно true, тогда
    синапсы из списка activelist увеличивают значения своих перманентностей
    на величину permanenceInc. Все остальные синапсы уменьшают свои
    перманентности на величину permanenceDec. Если же
    positiveReinforcement равно false, тогда синапсы из списка активных
    уменьшают свою перманентность на величину permanenceDec. После
    этого шага любым синапсам из segmentUpdate, которые только что
    появились, добавляется значение initialPerm.
     */
    public void adaptSegments(List<SegmentUpdate> segmentList, Boolean positiveReinforcement) {
        for(SegmentUpdate segUpd: segmentList) {
            // System.out.print(segUpd.segmentIndex[2] + "\r\n");
            if (segUpd.segmentIndex[2] < 0) {
                Segment newSegment = new Segment();
                for(Synapse syn: segUpd.activeSynapses) {
                    newSegment.synapses.add(syn);
                }
                newSegment.sequenceSegment = segUpd.sequenceSegment;
                region.columns.get(segUpd.segmentIndex[0]).cells.get(segUpd.segmentIndex[1]).dendriteSegments.add(newSegment);
            } else {
                Segment seg =  region.columns.get(segUpd.segmentIndex[0]).cells.get(segUpd.segmentIndex[1]).dendriteSegments.get(segUpd.segmentIndex[2]);
                seg.sequenceSegment = segUpd.sequenceSegment;
                for(Synapse syn: seg.synapses) {
                    if (segUpd.activeSynapses.contains(syn)) {
                        if (positiveReinforcement)
                            syn.permanence += region.permanenceInc;
                        else
                            syn.permanence -= region.permanenceDec;
                    } else {
                        if (positiveReinforcement)
                            syn.permanence -= region.permanenceDec;
                        else
                            syn.permanence += region.permanenceInc;
                    }
                }
                for(Synapse syn: segUpd.activeSynapses) {
                    if (!seg.synapses.contains(syn)) {
                        seg.synapses.add(syn);
                    }
                }
            }
        }
    }

     //////////////////////////////////////////////////////////////////

    // main phases

    /*
    Еще до того как получить любые входные данные, регион должен быть проинициализирован, а для этого надо создать начальный список потенциальных синапсов
    для каждой колонки.
    Он будет состоять из случайного множества входных битов, выбранных из пространства входных данных.
    Каждый входной бит будет представлен синапсом с некоторым случайным значением перманентности.
    Эти значения выбираются по двум критериям.
        Во-первых, эти случайные значения должны быть из малого диапазона около connectedPerm
    (пороговое значение – минимальное значение перманентности при котором синапс считается «действующим» («подключенным»)).
    Это позволит потенциальным синапсам стать подключенными (или отключенными) после небольшого числа обучающих итераций.
        Во-вторых, у каждой колонки есть геометрический центр ее входного региона и значения перманентности должны увеличиваться по направлению
    к этому центру (т.е. у центра колонки значения перманентности ее синапсов должны быть выше).
     */

    Random rnd = new Random();

    void initSynapses(Column column){
            //TODO: Region's dimensions used to initialize proximal synapses - may be incorrect
            for(int i = 0; i < region.square; i++) {
                Integer dimX = rnd.nextInt(region.xDimension);
                Integer dimY = rnd.nextInt(region.yDimension);
                Double perm = region.connectedPerm + region.connectedPerm / 2.0 - (rnd.nextDouble()/10.0);
                Double adjustment = Math.sqrt((((column.x - dimX))^2 +((column.y - dimY))^2)/(region.xDimension + region.yDimension));

                column.potentialSynapses.add(new Synapse(dimX, dimY, Math.max(perm - adjustment, 0.0)));
            }
    }

    public void SInitialization() {

        for (int i = 0;i < region.xDimension; i++) {
            for (int j = 0;j < region.yDimension; j++) {
                region.columns.add(new Column(region, i, j));
            }
        }

        activeColumns = new ArrayList<List<Integer>>();
        inputBits = new ArrayList<Integer[][]>();
        inputBits.add(new Integer[region.xDimension][region.yDimension]);

        for(Column c: region.columns){
            initSynapses(c);
        }

        for(Column c: region.columns)
            for (Cell i: c.cells){
                i.learnState.add(false);
                i.activeState.add(false);
                i.predictiveState.add(false);
            }

        region.inhibitionRadius = region.averageReceptiveFieldSize();
        ///////////////////////////
        // loadProperties();
        // checkProperties();
    }

    public Double updateOverlapDutyCycle(Integer c) {
        Double value = 0.0;
        if (region.columns.get(c).overlap > region.minOverlap) {
            value = 1.0;
        }
        return (value + totalTime * region.columns.get(c).overlapDutyCycle) / (totalTime + 1);
    }

    /*
    Фаза 1: Перекрытие (Overlap)
    Первая фаза вычисляет значение перекрытия каждой колонки с заданным входным вектором (данными).
    Перекрытие для каждой колонки это просто число действующих синапсов подключенных к активным входным битам,
    умноженное на фактор ускорения («агрессивности») колонки.
    Если полученное число будет меньше minOverlap, то мы устанавливаем значение перекрытия в ноль.
     */

    public void SOverlap() {
        for(int c = 0; c < region.square; c++) {
            region.columns.get(c).overlap = 0.0;
            ////
            //??
            region.columns.get(c).connectedSynapses = region.columns.get(c).connectedSynapses();
            ////
            for(Synapse synapse: region.columns.get(c).connectedSynapses) {
                region.columns.get(c).overlap += input(time, synapse.c, synapse.i);
            }
            if (region.columns.get(c).overlap < region.columns.get(c).minOverlap)
                region.columns.get(c).overlap = 0.0;
            else
                region.columns.get(c).overlap *= region.columns.get(c).boost;
        }
    }

    /*
    Фаза 2: Ингибирование (подавление)
    На второй фазе вычисляется какие из колонок остаются победителями после применения взаимного подавления.
    Параметр desiredLocalActivity контролирует число колонок, которые останутся победителями.
     */
    public void SInhibition() {
        activeColumns.add(new ArrayList<Integer>());
        for(int i = 0;i < region.square; i++) {
            Double minLocalActivity = region.GetMinLocalActivity(i);

            double overlap =  region.columns.get(i).overlap;
            if (overlap > 0.0 && overlap >= minLocalActivity) {
                activeColumns.get(time).add(i);
            }
        }
    }

    /*
    Фаза 3:
        Здесь обновляются значения перманентности всех синапсов, если это необходимо, равно как и фактор ускорения («агрессивности»)
    колонки вместе с ее радиусом подавления.
        Для победивших колонок, если их синапс был активен, его значение перманентности увеличивается,
    а иначе – уменьшается. Значения перманентности ограничены промежутком от 0.0 до 1.0 .
     */
    public void SLearning() {
        for(Integer c: activeColumns.get(time)) {
            for(Synapse s: region.columns.get(c).potentialSynapses) {
                if (input(time, s.c, s.i) > 0) {
                    s.permanence += region.permanenceInc;
                    s.permanence = Math.min(s.permanence, 1.0);
                } else {
                    s.permanence -= region.permanenceDec;
                    s.permanence = Math.max(s.permanence, 0.0);
                }
            }
        }

    /*
        Имеется два различных механизма ускорения помогающих колонке обучать свои соединения (связи).
        Если колонка не побеждает достаточно долго (что измеряется в activeDutyCycle), то увеличивается ее общий фактор ускорения.
        Альтернативно, если подключенные синапсы колонки плохо перекрываются с любыми входными данными достаточно долго (что измеряется
        в overlapDutyCycle), увеличиваются их значения перманентности.
    */
        int i = 0;
        for(Column c: region.columns){
            c.minDutyCycle = 0.01 * region.maxDutyCycle(region.neighbours(i));
            c.activeDutyCycle = updateActiveDutyCycle(i);
            c.boost = c.boostFunction();
            c.overlapDutyCycle = updateOverlapDutyCycle(i);

            if (c.overlapDutyCycle < c.minDutyCycle) {
                c.increasePermanences(0.1*region.connectedPerm);
            }
            i++;
        }

        region.inhibitionRadius = region.averageReceptiveFieldSize();
    }

    /*
    Фаза 1:
    На первой фазе вычисляются активные состояния (значения activeState) для каждой клетки из победивших колонок.
    Из этих колонок далее выбирается одна клетка на колонку для обучения (learnState).
    Логика здесь следующая: если текущий прямой вход снизу был предсказан какой-либо из клеток (т.е. ее параметр predictiveState был равен 1
    благодаря какому-то ее латеральному сегменту), тогда эти клетки становятся активными .
    Если этот сегмент стал активным из-за клеток выбранных для обучения (learnState ==1), тогда такая клетка также выбирается для обучения.
    Если же текущий прямой вход снизу не был предсказан, тогда все клетки становятся активными и кроме того, клетка, лучше всего соответствующая
    входным данным, выбирается для обучения, причем ей добавляется новый латеральный дендритный сегмент.
     */
    public void TCellStates() {
        for(Integer c: activeColumns.get(time)) {
            Boolean buPredicted = false;
            Boolean lcChosen = false;            

            for(int i = 0; i < region.cellsPerColumn; i++) {
                if (region.columns.get(c).cells.get(i).predictiveState.get(time-1 > 0 ? time-1 : 0)){

                    Integer[] s = getActiveSegment(c, i, time-1 > 0 ? time-1 : 0, State.active);
                    if (s[2] >= 0 && region.columns.get(s[0]).cells.get(s[1]).dendriteSegments.get(s[2]).sequenceSegment) {

                        buPredicted = true;
                        region.columns.get(c).cells.get(i).activeState.set(time,true);

                        if (segmentActive(region.columns.get(s[0]).cells.get(s[1]).dendriteSegments.get(s[2]), time-1 > 0 ? time-1 : 0, State.learn)) {
                            lcChosen = true;
                            region.columns.get(c).cells.get(i).learnState.set(time, true);
                        }
                    }
                }
            }

            if (!buPredicted) {
                for(int i = 0; i < region.cellsPerColumn; i++) {
                    region.columns.get(c).cells.get(i).activeState.set(time, true);
                }
            }

            if (!lcChosen) {
                Integer[] lc = getBestMatchingCell(c, time-1 > 0 ? time-1 : 0);
                region.columns.get(c).cells.get(lc[1]).learnState.set(time, true);
                if (time-1 >= 0) {
                    SegmentUpdate sUpdate = getSegmentActiveSynapses(c, lc[1], time-1, lc[2], true);
                    sUpdate.sequenceSegment = true;
                    region.columns.get(c).cells.get(lc[1]).segmentUpdateList.add(sUpdate);
                }
            }
        }
    }

    /*
        Фаза 2:
    Вторая фаза вычисляет состояния предсказания (предчувствия активации) для каждой клетки.
    Каждая клетка включает свое состояние предчувствия (параметр predictiveState), если любой из ее латеральных дендритных сегментов становится активным,
     т.е. достаточное число его горизонтальных (боковых, латеральных) соединений становятся активными благодаря прямому входу.
     В этом случае клетка ставит в очередь на отложенное исполнение следующий ряд своих изменений:
        а) усиление активных сейчас латеральных сегментов и
        б) усиление сегментов которые могли бы предсказать данную активацию, т.е. сегментов которые соответствуют (возможно, пока слабо)
        активности на предыдущем временном шаге.
    */
    public void TPredictiveStates() {
        for(int c = 0; c < region.square; c++) {
            for(int i = 0; i < region.cellsPerColumn; i++)
                for(int s = 0; s < region.columns.get(c).cells.get(i).dendriteSegments.size();s++) {
                    if (segmentActive(region.columns.get(c).cells.get(i).dendriteSegments.get(s), time, State.active)) {
                        region.columns.get(c).cells.get(i).predictiveState.set(time,true);
                        //a
                        SegmentUpdate activeUpdate = getSegmentActiveSynapses(c, i, time, s, false);
                        region.columns.get(c).cells.get(i).segmentUpdateList.add(activeUpdate);
                        //б
                        Integer[] predSegment = getBestMatchingSegment(c, i, time-1 > 0 ? time-1 : 0);
                        SegmentUpdate predUpdate = getSegmentActiveSynapses(c, i, time-1 > 0 ? time-1 : 0, predSegment[2], true);
                        region.columns.get(c).cells.get(i).segmentUpdateList.add(predUpdate);
                    }

                }
        }
    }

    /*
        Фаза 3:
    Третья и последняя фаза занимается обучением.
    В этой фазе происходит реальное обновление сегментов (которое было поставлено в очередь на исполнение)
    в том случае если колонка клетки активирована прямым входом и эта клетка выбрана в качестве кандидатки для обучения .
    В противном случае, если клетка по каким-либо причинам перестала предсказывать, мы ослабляем ее латеральные сегменты
     */
    public void TLearning() {
        for(int c = 0; c < region.square; c++) {
            for(int i = 0; i < region.cellsPerColumn; i++) {
                if(region.columns.get(c).cells.get(i).learnState.get(time)) {
                    adaptSegments(region.columns.get(c).cells.get(i).segmentUpdateList, true);
                } else if (!region.columns.get(c).cells.get(i).predictiveState.get(time) &&
                        region.columns.get(c).cells.get(i).predictiveState.get(time-1 > 0 ? time-1 : 0)) {
                    adaptSegments(region.columns.get(c).cells.get(i).segmentUpdateList, false);
                }
                region.columns.get(c).cells.get(i).segmentUpdateList.clear();
            }
        }
    }

    public void timestep() {
        time++;
        totalTime++;

        if (totalTime > 2) {
            time--;
            activeColumns.remove(time-2);
            for(Column c : region.columns)
                for (Cell i : c.cells ){
                    i.predictiveState.remove(time-2);
                    i.learnState.remove(time-2);
                    i.activeState.remove(time-2);
                }
        }

        for(Column c : region.columns)
            for (Cell i : c.cells ){
                i.predictiveState.add(false);
                i.learnState.add(false);
                i.activeState.add(false);
            }
    }
}
