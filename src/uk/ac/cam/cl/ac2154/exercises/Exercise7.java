package uk.ac.cam.cl.ac2154.exercises;


import uk.ac.cam.cl.mlrd.exercises.markov_models.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise7 implements IExercise7 {

    @Override
    public HiddenMarkovModel<DiceRoll, DiceType> estimateHMM(Collection<Path> sequenceFiles) throws IOException {

        Map<DiceType, Map<DiceType, Double>> transitionMatrix = new HashMap<>();
        Map<DiceType, Map<DiceRoll, Double>> emissionMatrix = new HashMap<>();

        DiceType lastState;

        for (DiceType dt1 : DiceType.values()){ //this literally just adds all possible transitions and emissions to the maps

            HashMap<DiceType, Double> hold3 = new HashMap<>();
            transitionMatrix.put(dt1, hold3);

            HashMap<DiceRoll, Double> hold4 = new HashMap<>();
            emissionMatrix.put(dt1, hold4);
            
            for (DiceType dt2 : DiceType.values()) {
                    transitionMatrix.get(dt1).put(dt2, 0.0);
            }
            for (DiceRoll dr2 : DiceRoll.values()) {
                if (!emissionMatrix.get(dt1).containsKey(dr2)) {
                    emissionMatrix.get(dt1).put(dr2, 0.0);
                }

            }
        }

        for (Path p : sequenceFiles) { //goes through all the files and counts transitions and emmissions
            lastState = DiceType.START;
            HMMDataStore<DiceRoll, DiceType> DiceFile = HMMDataStore.loadDiceFile(p);
            int count = 0;
            for(DiceType dt : DiceFile.hiddenSequence){
                if(count != 0) {
                    if (transitionMatrix.containsKey(dt)) {
                        transitionMatrix.get(lastState).merge(dt, 1.0, Double::sum);
                    } else {
                        System.out.println("ERROR. transitionmatrix dnc all keys??");
                    }
                }
                if (emissionMatrix.containsKey(dt)){
                    emissionMatrix.get(dt).merge(DiceFile.observedSequence.get(count), 1.0, Double::sum);
                }
                else{
                    System.out.println("ERROR. emissionMatrix dnc all keys??");
                }
                lastState = dt;
                count++;
            }
        }

        for (DiceType dt : DiceType.values()){ //translates to probabilities
            double total = 0.0;

            for (DiceType dt2 : transitionMatrix.get(dt).keySet()){ //count total
                total += transitionMatrix.get(dt).get(dt2);
            }
            for (DiceType dt2 : transitionMatrix.get(dt).keySet()){ //divide by total
                transitionMatrix.get(dt).put(dt2, (transitionMatrix.get(dt).get(dt2) == 0.0 ? 0.0 : transitionMatrix.get(dt).get(dt2)/total));
            }

            total = 0.0;
            for (DiceRoll dr : emissionMatrix.get(dt).keySet()){ //count total
                total += emissionMatrix.get(dt).get(dr);
            }
            for (DiceRoll dr : emissionMatrix.get(dt).keySet()){ //divide by total
                emissionMatrix.get(dt).put(dr, (emissionMatrix.get(dt).get(dr) == 0.0 ? 0.0 : emissionMatrix.get(dt).get(dr)/total));

            }
        }

        return new HiddenMarkovModel<>(transitionMatrix, emissionMatrix);
    }

}
