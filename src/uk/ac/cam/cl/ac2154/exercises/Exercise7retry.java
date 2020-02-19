package uk.ac.cam.cl.ac2154.exercises;


import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise7retry implements IExercise7 {

    @Override
    public HiddenMarkovModel<DiceRoll, DiceType> estimateHMM(Collection<Path> sequenceFiles) throws IOException {

        Map<DiceType, Map<DiceType, Double>> transitionMatrix = new HashMap<>();
        Map<DiceType, Map<DiceRoll, Double>> emissionMatrix = new HashMap<>();

        DiceType lastState;

        List<DiceType> diceTypes = new LinkedList<>();
        diceTypes.add(DiceType.START);
        diceTypes.add(DiceType.END);
        diceTypes.add(DiceType.FAIR);
        diceTypes.add(DiceType.WEIGHTED);

        List<DiceRoll> diceRolls = new LinkedList<>();
        diceRolls.add(DiceRoll.ONE);
        diceRolls.add(DiceRoll.TWO);
        diceRolls.add(DiceRoll.THREE);
        diceRolls.add(DiceRoll.FOUR);
        diceRolls.add(DiceRoll.FIVE);
        diceRolls.add(DiceRoll.SIX);
        diceRolls.add(DiceRoll.START);
        diceRolls.add(DiceRoll.END);

        for (DiceType dt1 : diceTypes){
            if(!transitionMatrix.containsKey(dt1)) {
                HashMap<DiceType, Double> hold3 = new HashMap<>();
                transitionMatrix.put(dt1, hold3);
            }
            if(!emissionMatrix.containsKey(dt1)){
                HashMap<DiceRoll, Double> hold4 = new HashMap<>();
                emissionMatrix.put(dt1, hold4);
            }
            for (DiceType dt2 : diceTypes) {
                if (!transitionMatrix.get(dt1).containsKey(dt2)) {
                    transitionMatrix.get(dt1).put(dt2, 0.0);
                }
            }
            for (DiceRoll dr2 : diceRolls) {
                if (!emissionMatrix.get(dt1).containsKey(dr2)) {
                    emissionMatrix.get(dt1).put(dr2, 0.0);
                }

            }
        }

        Map<DiceType, Map<DiceType, Integer>> transitions = new HashMap<>();
        Map<DiceType, Map<DiceRoll, Integer>> emissions = new HashMap<>();

        for (Path p : sequenceFiles) {
            lastState = DiceType.START;
            new HMMDataStore<>(new LinkedList<DiceRoll>(), new LinkedList<DiceType>()); //might be able to be moved to outside the loop
            HMMDataStore<DiceRoll, DiceType> DiceFile = HMMDataStore.loadDiceFile(p);
            int count = 0;
            for(DiceType dt : DiceFile.hiddenSequence){
                if(count != 0){
                    if (transitions.containsKey(dt)){
                        transitions.get(lastState).merge(dt, 1, Integer::sum);
                    }
                    else{
                        Map<DiceType, Integer> hold = new HashMap<DiceType, Integer>();
                        hold.put(dt, 1);
                        transitions.put(lastState, hold);
                    }
                    if (emissions.containsKey(dt)){
                        emissions.get(dt).merge(DiceFile.observedSequence.get(count), 1, Integer::sum);
                    }
                    else{
                        Map<DiceRoll, Integer> holdem = new HashMap<DiceRoll, Integer>();
                        holdem.put(DiceFile.observedSequence.get(count), 1);
                        emissions.put(dt, holdem);
                    }
                    lastState = dt;
                }
                count++;
            }
        }

        //The transition matrix (A) consists of the estimates of transition probabilities.
        for (DiceType dt : transitions.keySet()){
            double totaldttrans = 0.0;

            if(!transitionMatrix.containsKey(dt)) {
                HashMap<DiceType, Double> temp1 = new HashMap<>();
                transitionMatrix.put(dt, temp1);
            }

            for (DiceType dt2 : transitions.get(dt).keySet()){
                totaldttrans += transitions.get(dt).get(dt2);
            }
            for (DiceType dt2 : transitions.get(dt).keySet()){
                transitionMatrix.get(dt).put(dt2, transitions.get(dt).get(dt2)/totaldttrans);
            }
        }
        for (DiceType dt : emissions.keySet()){

            if(!emissionMatrix.containsKey(dt)){
                HashMap<DiceRoll, Double> temp2 = new HashMap<>();
                emissionMatrix.put(dt, temp2);
            }
            double totaldtemiss = 0.0;
            for (DiceRoll dr : emissions.get(dt).keySet()){
                totaldtemiss += emissions.get(dt).get(dr);
            }
            for (DiceRoll dr : emissions.get(dt).keySet()){

                emissionMatrix.get(dt).put(dr, emissions.get(dt).get(dr)/totaldtemiss);

            }
        }



        //The emission probabilities (B) measure how probable it is to obtain a particular roll given the dice type.

        HiddenMarkovModel<DiceRoll, DiceType> results = new HiddenMarkovModel<>(transitionMatrix, emissionMatrix);

        return results;
    }

}
