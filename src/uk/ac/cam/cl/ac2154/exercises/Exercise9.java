package uk.ac.cam.cl.ac2154.exercises;

import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.util.*;

public class Exercise9 implements IExercise9 {
    @Override
    public HiddenMarkovModel<AminoAcid, Feature> estimateHMM(List<HMMDataStore<AminoAcid, Feature>> sequencePairs) throws IOException {
        Map<Feature, Map<Feature, Double>> transitionMatrix = new HashMap<>();
        Map<Feature, Map<AminoAcid, Double>> emissionMatrix = new HashMap<>();

        Feature lastState;

        for (Feature dt1 : Feature.values()){ //this literally just adds all possible transitions and emissions to the maps

            HashMap<Feature, Double> hold3 = new HashMap<>();
            transitionMatrix.put(dt1, hold3);

            HashMap<AminoAcid, Double> hold4 = new HashMap<>();
            emissionMatrix.put(dt1, hold4);

            for (Feature dt2 : Feature.values()) {
                transitionMatrix.get(dt1).put(dt2, 0.0);
            }
            for (AminoAcid dr2 : AminoAcid.values()) {
                if (!emissionMatrix.get(dt1).containsKey(dr2)) {
                    emissionMatrix.get(dt1).put(dr2, 0.0);
                }

            }
        }

        for (HMMDataStore<AminoAcid, Feature> Pairfile : sequencePairs) { //goes through all the files and counts transitions and emmissions
            lastState = Feature.START;
            int count = 0;
            for(Feature dt : Pairfile.hiddenSequence){
                if(count != 0) {
                    if (transitionMatrix.containsKey(dt)) {
                        transitionMatrix.get(lastState).merge(dt, 1.0, Double::sum);
                    } else {
                        System.out.println("ERROR. transitionmatrix dnc all keys??");
                    }
                }
                if (emissionMatrix.containsKey(dt)){
                    emissionMatrix.get(dt).merge(Pairfile.observedSequence.get(count), 1.0, Double::sum);
                }
                else{
                    System.out.println("ERROR. emissionMatrix dnc all keys??");
                }
                lastState = dt;
                count++;
            }
        }

        for (Feature dt : Feature.values()){ //translates to probabilities
            double total = 0.0;

            for (Feature dt2 : transitionMatrix.get(dt).keySet()){ //count total
                total += transitionMatrix.get(dt).get(dt2);
            }
            for (Feature dt2 : transitionMatrix.get(dt).keySet()){ //divide by total
                transitionMatrix.get(dt).put(dt2, (transitionMatrix.get(dt).get(dt2) == 0.0 ? 0.0 : transitionMatrix.get(dt).get(dt2)/total));
            }

            total = 0.0;
            for (AminoAcid dr : emissionMatrix.get(dt).keySet()){ //count total
                total += emissionMatrix.get(dt).get(dr);
            }
            for (AminoAcid dr : emissionMatrix.get(dt).keySet()){ //divide by total
                emissionMatrix.get(dt).put(dr, (emissionMatrix.get(dt).get(dr) == 0.0 ? 0.0 : emissionMatrix.get(dt).get(dr)/total));

            }
        }

        return new HiddenMarkovModel<>(transitionMatrix, emissionMatrix);
    }

    @Override
    public List<Feature> viterbi(HiddenMarkovModel<AminoAcid, Feature> model, List<AminoAcid> observedSequence) {
        List<Feature> results = new LinkedList<>();

        Map<Feature, Map<Feature, Double>> transitions = model.getTransitionMatrix();
        Map<Feature, Map<AminoAcid, Double>> emissions = model.getEmissionMatrix();
        List<AminoAcid> obSeq = new LinkedList<>(observedSequence);

        List<Map<Feature, Feature>> psi = new LinkedList<>();
        List<Map<Feature, Double>> delta = new LinkedList<>();

        Map<Feature, Double> holdDelta = new HashMap<>();
        Map<Feature, Feature> holdPsi;
        for (Feature dt : Feature.values()){
            holdDelta.put(dt, emissions.get(dt).get(obSeq.get(0)));
        }

        obSeq.remove(0);
        delta.add(holdDelta);
        int count = 1;
        double maxdelta = 0;
        double tempdelta = 0;
        Feature psiVal;

        for (AminoAcid roll : obSeq){
            holdDelta = new HashMap<>();
            holdPsi = new HashMap<>();
            for (Feature dt : Feature.values()){
                maxdelta = 0;
                psiVal = Feature.START;
                for (Feature dtLast : Feature.values()){
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
            for(Feature dt: holdDelta.keySet()){
                holdDelta.put(dt, holdDelta.get(dt)/probTotal);
            }
            //normalises the probs
            delta.add(holdDelta);
            psi.add(holdPsi);
            count++;
        }

        Collections.reverse(psi);
        psiVal = Feature.END;
        results.add(psiVal);
        for (Map<Feature, Feature> psiValue : psi){
            psiVal = psiValue.get(psiVal);
            results.add(psiVal);
        }
        Collections.reverse(results);

        return results;
    }

    @Override
    public Map<List<Feature>, List<Feature>> predictAll(HiddenMarkovModel<AminoAcid, Feature> model, List<HMMDataStore<AminoAcid, Feature>> testSequencePairs) throws IOException {
        Map<List<Feature>, List<Feature>> results = new HashMap<>();
        for (HMMDataStore<AminoAcid, Feature> pairFile : testSequencePairs){
            List<Feature> hold;
            hold = viterbi(model, pairFile.observedSequence);
            results.put(pairFile.hiddenSequence, hold);
        }
        return results;
    }

    @Override
    public double precision(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        double mempredTotal = 0;
        double correctPredMem = 0;
        for(List<Feature> tru : true2PredictedMap.keySet()){
            if(tru.size() != true2PredictedMap.get(tru).size()){
                System.out.println("DIFFERENT SIZE TRUE TO PREDICTED");
            }
            else for (int i = 0; i < tru.size(); i++){
                if (true2PredictedMap.get(tru).get(i) == Feature.MEMBRANE){
                    mempredTotal ++;
                    if(tru.get(i) == Feature.MEMBRANE){
                        correctPredMem++;
                    }
                }
            }
        }
        return correctPredMem/mempredTotal;
    }

    @Override
    public double recall(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        double memTotal = 0;
        double correctPredMem = 0;
        for(List<Feature> tru : true2PredictedMap.keySet()){
            if(tru.size() != true2PredictedMap.get(tru).size()){
                System.out.println("DIFFERENT SIZE TRUE TO PREDICTED");
            }
            else for (int i = 0; i < tru.size(); i++){
                if (tru.get(i) == Feature.MEMBRANE){
                    memTotal ++;
                    if(true2PredictedMap.get(tru).get(i) == Feature.MEMBRANE){
                        correctPredMem++;
                    }
                }
            }
        }
        return correctPredMem/memTotal;
    }

    @Override
    public double fOneMeasure(Map<List<Feature>, List<Feature>> true2PredictedMap) {
        double precision = precision(true2PredictedMap);
        double recall = recall(true2PredictedMap);

        return 2*(precision*recall)/(precision+recall);
    }


}
