package uk.ac.cam.cl.ac2154.exercises;

import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise8 implements IExercise8 {
    @Override
    public List<DiceType> viterbi(HiddenMarkovModel<DiceRoll, DiceType> model, List<DiceRoll> observedSequence) {
        List<DiceType> results = new LinkedList<>();

        Map<DiceType, Map<DiceType, Double>> transitions = model.getTransitionMatrix();
        Map<DiceType, Map<DiceRoll, Double>> emissions = model.getEmissionMatrix();
        List<DiceRoll> obSeq = new LinkedList<>(observedSequence);

        List<Map<DiceType, DiceType>> psi = new LinkedList<>();
        List<Map<DiceType, Double>> delta = new LinkedList<>();

        Map<DiceType, Double> holdDelta = new HashMap<>();
        Map<DiceType, DiceType> holdPsi;
        for (DiceType dt : DiceType.values()){
            holdDelta.put(dt, emissions.get(dt).get(obSeq.get(0)));
        }

        obSeq.remove(0);
        delta.add(holdDelta);
        int count = 1;
        double maxdelta = 0;
        double tempdelta = 0;
        DiceType psiVal;

        for (DiceRoll roll : obSeq){
            holdDelta = new HashMap<>();
            holdPsi = new HashMap<>();
            for (DiceType dt : DiceType.values()){
                maxdelta = 0;
                psiVal = DiceType.START;
                for (DiceType dtLast : DiceType.values()){
                    tempdelta = (delta.get(count - 1).get(dtLast))*(transitions.get(dtLast).get(dt))*(emissions.get(dt).get(roll));
                    if (tempdelta > maxdelta){
                        maxdelta = tempdelta;
                        psiVal = dtLast;
                    }
                }
                holdDelta.put(dt, maxdelta);
                holdPsi.put(dt, psiVal);
            }
            double probTotal = 0.0;
            for(Double prob: holdDelta.values()){
                probTotal+=prob;
            }
            for(DiceType dt: holdDelta.keySet()){
                holdDelta.put(dt, holdDelta.get(dt)/probTotal);
            }
            //normalises the probs
            delta.add(holdDelta);
            psi.add(holdPsi);
            count++;
        }

        Collections.reverse(psi);
        psiVal = DiceType.END;
        results.add(psiVal);
        for (Map<DiceType, DiceType> psiValue : psi){
            psiVal = psiValue.get(psiVal);
            results.add(psiVal);
        }
        Collections.reverse(results);

        return results;
    }

    @Override
    public Map<List<DiceType>, List<DiceType>> predictAll(HiddenMarkovModel<DiceRoll, DiceType> model, List<Path> testFiles) throws IOException {
        Map<List<DiceType>, List<DiceType>> results = new HashMap<>();
        for (HMMDataStore<DiceRoll, DiceType> dicefile : HMMDataStore.loadDiceFiles(testFiles)){
            List<DiceType> hold;
            hold = viterbi(model, dicefile.observedSequence);
            results.put(dicefile.hiddenSequence, hold);
        }
        return results;
    }

    @Override
    public double precision(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
        double weightpredTotal = 0;
        double correctPredWeight = 0;
        for(List<DiceType> tru : true2PredictedMap.keySet()){
            if(tru.size() != true2PredictedMap.get(tru).size()){
                System.out.println("DIFFERENT SIZE TRUE TO PREDICTED");
            }
            else for (int i = 0; i < tru.size(); i++){
                if (true2PredictedMap.get(tru).get(i) == DiceType.WEIGHTED){
                    weightpredTotal ++;
                    if(tru.get(i) == DiceType.WEIGHTED){
                        correctPredWeight++;
                    }
                }
            }
        }
        return correctPredWeight/weightpredTotal;
    }

    @Override
    public double recall(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
        double weightTotal = 0;
        double correctPredWeight = 0;
        for(List<DiceType> tru : true2PredictedMap.keySet()){
            if(tru.size() != true2PredictedMap.get(tru).size()){
                System.out.println("DIFFERENT SIZE TRUE TO PREDICTED");
            }
            else for (int i = 0; i < tru.size(); i++){
                if (tru.get(i) == DiceType.WEIGHTED){
                    weightTotal ++;
                    if(true2PredictedMap.get(tru).get(i) == DiceType.WEIGHTED){
                        correctPredWeight++;
                    }
                }
            }
        }
        return correctPredWeight/weightTotal;
    }

    @Override
    public double fOneMeasure(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
        double precision = precision(true2PredictedMap);
        double recall = recall(true2PredictedMap);

        return 2*(precision*recall)/(precision+recall);
    }
}
