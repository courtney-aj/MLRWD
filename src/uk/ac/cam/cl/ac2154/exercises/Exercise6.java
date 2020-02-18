package uk.ac.cam.cl.ac2154.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise6;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.NuancedSentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise6 implements IExercise6 {
    @Override
    public Map<NuancedSentiment, Double> calculateClassProbabilities(Map<Path, NuancedSentiment> trainingSet) throws IOException {

        double documents = trainingSet.size();
        double posDocuments = 0;
        double negDocuments = 0;

        for (Path p : trainingSet.keySet()){
            if (trainingSet.get(p) == NuancedSentiment.POSITIVE){
                posDocuments++;
            }
            else if (trainingSet.get(p) == NuancedSentiment.NEGATIVE){
                negDocuments++;
            }
        }

        Map<NuancedSentiment, Double> classprob = new HashMap<>();
        classprob.put(NuancedSentiment.POSITIVE, (posDocuments/documents));
        classprob.put(NuancedSentiment.NEGATIVE, (negDocuments/documents));
        classprob.put(NuancedSentiment.NEUTRAL, 1 - (posDocuments+negDocuments)/documents);
        return classprob;
    }

    @Override
    public Map<String, Map<NuancedSentiment, Double>> calculateNuancedLogProbs(Map<Path, NuancedSentiment> trainingSet) throws IOException {
        Map<String, Double> poswords = new HashMap<>();
        Map<String, Double> negwords = new HashMap<>();
        Map<String, Double> neuwords = new HashMap<>();
        List<String> tokens = new LinkedList<>();
        int totalposwords = 0;
        int totalnegwords = 0;
        int totalneuwords = 0;


        for (Path p : trainingSet.keySet()){
            tokens.clear();
            tokens = Tokenizer.tokenize(p);
            if (trainingSet.get(p) == NuancedSentiment.POSITIVE){
                for (String token : tokens){
                    poswords.merge(token, 1.0, Double::sum);
                    totalposwords++;
                }
            }
            else if(trainingSet.get(p) == NuancedSentiment.NEGATIVE){
                for (String token : tokens){
                    negwords.merge(token, 1.0, Double::sum);
                    totalnegwords++;
                }
            }
            else{
                for (String token : tokens){
                    neuwords.merge(token, 1.0, Double::sum);
                    totalneuwords++;
                }
            }
        }

        HashMap<NuancedSentiment, Double> wordmap;
        Map<String, Map<NuancedSentiment, Double>> results = new HashMap<>();

        double vocab = negwords.size();
        for (String w : poswords.keySet()){
            if (!negwords.containsKey(w)){
                vocab+=1.0;
            }
        }
        for (String w : neuwords.keySet()){
            if (!negwords.containsKey(w) && !poswords.containsKey(w)){
                vocab+=1.0;
            }
        }
        Set<String> totalwords = new HashSet<>();
        totalwords.addAll(poswords.keySet());
        totalwords.addAll(negwords.keySet());
        totalwords.addAll(neuwords.keySet());

        for (String word : totalwords){

            wordmap = new HashMap<>();

            wordmap.put(NuancedSentiment.POSITIVE, java.lang.Math.log(
                                                    (1.0 + ((poswords.get(word) == null) ? 0.0 : poswords.get(word)))
                                                    /(totalposwords + vocab)));
            wordmap.put(NuancedSentiment.NEGATIVE, java.lang.Math.log(
                                                    (1.0 + ((negwords.get(word) == null) ? 0.0 : negwords.get(word)))
                                                    /(totalnegwords + vocab)));
            wordmap.put(NuancedSentiment.NEUTRAL, java.lang.Math.log(
                                                    (1.0 + ((neuwords.get(word) == null) ? 0.0 : neuwords.get(word)))
                                                    /(totalneuwords + vocab)));

            results.put(word, wordmap);
        }


        return results;
    }

    @Override
    public Map<Path, NuancedSentiment> nuancedClassifier(Set<Path> testSet, Map<String, Map<NuancedSentiment, Double>> tokenLogProbs, Map<NuancedSentiment, Double> classProbabilities) throws IOException {
        Double pPos;
        Double pNeg;
        Double pNeu;

        List<String> tokens = new LinkedList<>();
        Map<Path, NuancedSentiment> results = new HashMap<>();

        for (Path review : testSet){
            pPos = classProbabilities.get(NuancedSentiment.POSITIVE);
            pNeg = classProbabilities.get(NuancedSentiment.NEGATIVE);
            pNeu = classProbabilities.get(NuancedSentiment.NEUTRAL);
            tokens.clear();
            tokens = Tokenizer.tokenize(review);
            for (String token : tokens) {
                if (tokenLogProbs.get(token) != null) {
                    pPos += tokenLogProbs.get(token).get(NuancedSentiment.POSITIVE);
                    pNeg += tokenLogProbs.get(token).get(NuancedSentiment.NEGATIVE);
                    pNeu += tokenLogProbs.get(token).get(NuancedSentiment.NEUTRAL);
                }
            }
            if (pPos > pNeg && pPos > pNeu){
                results.put(review, NuancedSentiment.POSITIVE);
            }
            else if (pNeg > pNeu){
                results.put(review, NuancedSentiment.NEGATIVE);
            }
            else{
                results.put(review, NuancedSentiment.NEUTRAL);
            }
        }

        return results;
    }

    @Override
    public double nuancedAccuracy(Map<Path, NuancedSentiment> trueSentiments, Map<Path, NuancedSentiment> predictedSentiments) {
        double correct = 0.0;
        for (Path p : trueSentiments.keySet()){
            if (predictedSentiments.get(p) == trueSentiments.get(p)){
                correct++;
            }
        }
        return (correct / trueSentiments.size());
    }

    @Override
    public Map<Integer, Map<Sentiment, Integer>> agreementTable(Collection<Map<Integer, Sentiment>> predictedSentiments) {

        Map<Integer, Map<Sentiment, Integer>> results = new HashMap<>();
        Map<Sentiment, Integer> one = new HashMap<>();
        Map<Sentiment, Integer> two = new HashMap<>();
        Map<Sentiment, Integer> three = new HashMap<>();
        Map<Sentiment, Integer> four = new HashMap<>();

        results.put(1, one);
        results.put(2, two);
        results.put(3, three);
        results.put(4, four);

        for (Map person : predictedSentiments){ //Map<Integer, Sentiment>
            for (int i = 1; i < 5; i++){
                results.get(i).merge((Sentiment) person.get(i), 1, Integer::sum);
                }
            }
        return results;
    }

    @Override
    public double kappa(Map<Integer, Map<Sentiment, Integer>> agreementTable) {
        double P_a = 0;
        double P_e = 0;
        Set<Integer> N = agreementTable.keySet();
        double n_i = 133.0;
        double n_i_j;
        double k;


        {   //Calculate P_e
            Sentiment j = Sentiment.POSITIVE;
            {
                double tempVal = 0;
                for (int i : N) {
                    n_i = 0;
                    for (Sentiment s : agreementTable.get(i).keySet()){
                        n_i += agreementTable.get(i).getOrDefault(s, 0);
                    }

                    n_i_j = agreementTable.get(i).getOrDefault(j, 0);
                    tempVal += (n_i_j / n_i);
                }
                tempVal /= N.size();
                tempVal *= tempVal;
                P_e += tempVal;
            }

            j = Sentiment.NEGATIVE;
            {
                double tempVal = 0;
                for (int i : N) {
                    n_i = 0;
                    for (Sentiment s : agreementTable.get(i).keySet()){
                        n_i += agreementTable.get(i).getOrDefault(s, 0);
                    }
                    n_i_j = agreementTable.get(i).getOrDefault(j, 0);
                    tempVal += (n_i_j / n_i);
                }
                tempVal /= N.size();
                tempVal *= tempVal;
                P_e += tempVal;
            }
        }

        { //Calculate P_a
            for (int i : N) {
                double tempVal = 0;
                Sentiment j = Sentiment.POSITIVE;{
                    n_i_j = agreementTable.get((int) i).getOrDefault(j, 0);
                    tempVal += n_i_j * (n_i_j - 1);
                }
                j = Sentiment.NEGATIVE;{
                    n_i_j = agreementTable.get((int) i).getOrDefault(j, 0);
                    tempVal += n_i_j * (n_i_j - 1);
                }
                n_i = 0;
                for (Sentiment s : agreementTable.get(i).keySet()){
                    n_i += agreementTable.get(i).getOrDefault(s, 0);
                }
                tempVal /= (n_i)*(n_i - 1);
                P_a += tempVal;
            }
            P_a /= N.size();
        }

        k = (P_a - P_e)/(1 - P_e);
        return k;
    }
}
