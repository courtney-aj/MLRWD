package uk.ac.cam.cl.ac2154.exercises;
import javafx.util.Pair;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise3 {

    public LinkedList<Pair<String, Double>> getFreqs (Set<Path> dataSet, Path lexiconFile) throws IOException {

        SortedMap<String, Double> freqs = new TreeMap<String, Double>();
        LinkedList<Pair<String, Double>> words = new LinkedList<>();
        Double total = 0.0;
        Double milestone = 1.0;
        LinkedList<Integer> unique = new LinkedList<Integer>();
        int uniqueTokens = 0;

        for (Path p : dataSet) {
            List<String> tokens = Tokenizer.tokenize(p);
            for (String token : tokens) {
                if (!freqs.containsKey(token)){
                    uniqueTokens ++;
                }
                freqs.merge(token, 1.0, Double::sum);
                if (total == milestone){
                    unique.add(uniqueTokens);
                    milestone *= 2.0;
                }
            }
        }
        for (String word : freqs.keySet()){
            words.add(new Pair(word, freqs.get(word)));
        }
        Collections.sort(words, Comparator.comparing(p -> -p.getValue()));
        return words;
    }

    public LinkedList<Pair<Integer,Integer>> HeapsLaw (Set<Path> dataSet, Path lexiconFile) throws IOException {

        SortedMap<String, Double> freqs = new TreeMap<>();
        int total = 0;
        Double milestone = 1.0;
        LinkedList<Pair<Integer, Integer>> unique = new LinkedList<>();
        int uniqueTokens = 0;

        for (Path p : dataSet) {
            List<String> tokens = Tokenizer.tokenize(p);
            for (String token : tokens) {
                if (!freqs.containsKey(token)){
                    uniqueTokens ++;
                }
                freqs.merge(token, 1.0, Double::sum);
                total++;
                if (milestone.intValue() == total){
                    unique.add(new Pair<Integer, Integer>(uniqueTokens, total));
                    milestone *= 2.0;
                }
            }
        }
        return unique;
    }

    public double exFreq(int rank, double grad, double yint){
        double logrank = java.lang.Math.log(rank + 1);
        double ans = java.lang.Math.exp(yint + logrank*grad);
        return ans;

    }


}
