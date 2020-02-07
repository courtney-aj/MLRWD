package uk.ac.cam.cl.ac2154.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise2;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;


public class Exercise2 implements IExercise2 {
    @Override
    public Map<Sentiment, Double> calculateClassProbabilities(Map<Path, Sentiment> trainingSet) throws IOException {

        double documents = trainingSet.size();
        double posDocuments = 0;

        for (Path p : trainingSet.keySet()){
            if (trainingSet.get(p) == Sentiment.POSITIVE){
                posDocuments++;
            }
        }

        Map<Sentiment, Double> classprob = new HashMap<>();
        classprob.put(Sentiment.POSITIVE, (posDocuments/documents));
        classprob.put(Sentiment.NEGATIVE, (1 - posDocuments/documents));
        return classprob;
    }

    @Override
    public Map<String, Map<Sentiment, Double>> calculateUnsmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {

        Map<String, Double> poswords = new HashMap<>();
        Map<String, Double> negwords = new HashMap();
        List<String> tokens = new LinkedList<>();
        int totalposwords = 0;
        int totalnegwords = 0;


        for (Path p : trainingSet.keySet()){
            tokens.clear();
            tokens = Tokenizer.tokenize(p);
            if (trainingSet.get(p) == Sentiment.POSITIVE){
                for (String token : tokens){
                    if (poswords.get(token) == null){
                        poswords.put(token, 1.0);
                    }
                    else{
                        poswords.put(token, poswords.get(token) + 1.0);
                    }
                    totalposwords++;
                }
            }
            else{
                for (String token : tokens){
                    if (negwords.get(token) == null){
                        negwords.put(token, 1.0);
                    }
                    else{
                        negwords.put(token, negwords.get(token) + 1.0);
                    }
                    totalnegwords++;
                }
            }
        }

        HashMap<Sentiment, Double> wordmap = new HashMap<>();
        Map<String, Map<Sentiment, Double>> results = new HashMap<>();


        for (String word : poswords.keySet()){
            wordmap = new HashMap<>();
            if (negwords.get(word) == null){
                wordmap.put(Sentiment.POSITIVE, java.lang.Math.log(poswords.get(word)/totalposwords));
                wordmap.put(Sentiment.NEGATIVE, java.lang.Math.log(0.0));
            }
            else {
                wordmap.put(Sentiment.POSITIVE, java.lang.Math.log(poswords.get(word)/totalposwords));
                wordmap.put(Sentiment.NEGATIVE, java.lang.Math.log(negwords.get(word)/totalnegwords));
            }
            results.put(word, wordmap);
        }

        for (String word : negwords.keySet()){
            wordmap = new HashMap<>();
            if (poswords.get(word) == null){
                wordmap.put(Sentiment.POSITIVE, java.lang.Math.log(0.0));
                wordmap.put(Sentiment.NEGATIVE, java.lang.Math.log(negwords.get(word)/totalnegwords));
                results.put(word, wordmap);
            }
        }

        return results;
    }

    @Override
    public Map<String, Map<Sentiment, Double>> calculateSmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {
        Map<String, Double> poswords = new HashMap<>();
        Map<String, Double> negwords = new HashMap();
        List<String> tokens = new LinkedList<>();
        int totalposwords = 0;
        int totalnegwords = 0;


        for (Path p : trainingSet.keySet()){
            tokens.clear();
            tokens = Tokenizer.tokenize(p);
            if (trainingSet.get(p) == Sentiment.POSITIVE){
                for (String token : tokens){
                    if (poswords.get(token) == null){
                        poswords.put(token, 1.0);
                    }
                    else{
                        poswords.put(token, poswords.get(token) + 1.0);
                    }
                    totalposwords++;
                }
            }
            else{
                for (String token : tokens){
                    if (negwords.get(token) == null){
                        negwords.put(token, 1.0);
                    }
                    else{
                        negwords.put(token, negwords.get(token) + 1.0);
                    }
                    totalnegwords++;
                }
            }
        }

        HashMap<Sentiment, Double> wordmap;
        Map<String, Map<Sentiment, Double>> results = new HashMap<>();

        double vocab = negwords.size();
        for (String w : poswords.keySet()){
            if (!negwords.containsKey(w)){
                vocab+=1.0;
            }
        }


        for (String word : poswords.keySet()){
            wordmap = new HashMap<>();
            if (negwords.get(word) == null){
                wordmap.put(Sentiment.POSITIVE, java.lang.Math.log((1.0 + poswords.get(word))/(totalposwords + vocab)));
                wordmap.put(Sentiment.NEGATIVE, java.lang.Math.log(1.0/(totalnegwords + vocab)));
            }
            else {
                wordmap.put(Sentiment.POSITIVE, java.lang.Math.log((1.0 + poswords.get(word))/(totalposwords + vocab)));
                wordmap.put(Sentiment.NEGATIVE, java.lang.Math.log((1.0 + negwords.get(word))/(totalnegwords + vocab)));
            }
            results.put(word, wordmap);
        }

        for (String word : negwords.keySet()){
            wordmap = new HashMap<>();
            if (poswords.get(word) == null){
                wordmap.put(Sentiment.POSITIVE, java.lang.Math.log(1.0/(totalposwords + vocab)));
                wordmap.put(Sentiment.NEGATIVE, java.lang.Math.log((1.0 + negwords.get(word))/(totalnegwords + vocab)));
                results.put(word, wordmap);
            }
        }

        return results;
    }

    @Override
    public Map<Path, Sentiment> naiveBayes(Set<Path> testSet, Map<String, Map<Sentiment, Double>> tokenLogProbs, Map<Sentiment, Double> classProbabilities) throws IOException {
        Double pPos;
        Double pNeg;
        List<String> tokens = new LinkedList<>();
        Map<Path, Sentiment> results = new HashMap<>();

        for (Path review : testSet){
            pPos = classProbabilities.get(Sentiment.POSITIVE);
            pNeg = classProbabilities.get(Sentiment.NEGATIVE);
            tokens.clear();
            tokens = Tokenizer.tokenize(review);
            for (String token : tokens) {
                if (tokenLogProbs.get(token) != null) {
                    pPos += tokenLogProbs.get(token).get(Sentiment.POSITIVE);
                    pNeg += tokenLogProbs.get(token).get(Sentiment.NEGATIVE);
                }
            }
            if (pPos > pNeg){
                results.put(review, Sentiment.POSITIVE);
            }
            else{
                results.put(review, Sentiment.NEGATIVE);
            }
        }

        return results;
    }



}
