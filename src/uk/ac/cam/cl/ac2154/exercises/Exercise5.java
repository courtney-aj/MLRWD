package uk.ac.cam.cl.ac2154.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise5;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.ac2154.exercises.Exercise2;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Array;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Exercise5 implements IExercise5 {

    @Override
    public List<Map<Path, Sentiment>> splitCVRandom(Map<Path, Sentiment> dataSet, int seed) {
        List<Map<Path,Sentiment>> results = new LinkedList<>();
        List<Path> paths = new ArrayList<>(dataSet.keySet());

        Collections.shuffle(paths, new Random(seed));

        double sizeofSubFold = dataSet.size()/10;
        int startSubFold = 0;
        int endSubFold = ((int) sizeofSubFold);

        for(double i=0.0; i < 10.0; i++){
            startSubFold = (int) (i*sizeofSubFold);
            endSubFold = (int) ((i+1.0)*sizeofSubFold);
            Map<Path, Sentiment> fold = new HashMap<>(paths.subList(startSubFold, endSubFold).stream()
                    .collect(Collectors.toMap(Function.identity(), dataSet::get))); //maps path, Sentiment from dataSet
            results.add(fold);
        }
        return results;
    }

    @Override
    public List<Map<Path, Sentiment>> splitCVStratifiedRandom(Map<Path, Sentiment> dataSet, int seed) {
        List<Map<Path,Sentiment>> results = new LinkedList<>();
        List<Path> pathsPos = new ArrayList<>();
        List<Path> pathsNeg = new ArrayList<>();
        for (Path p: dataSet.keySet()) {
            if(!pathsNeg.contains(p) && !pathsPos.contains(p)) {
                if (dataSet.get(p) == Sentiment.POSITIVE) {
                    pathsPos.add(p);
                } else if (dataSet.get(p) == Sentiment.NEGATIVE) {
                    pathsNeg.add(p);
                }
            }
        }

        Collections.shuffle(pathsPos, new Random(seed));
        Collections.shuffle(pathsNeg, new Random(seed));
        System.out.println(pathsNeg.size());
        System.out.println(pathsPos.size());
        double sizeofSubFoldPos = pathsPos.size()/10; //not going to assume equal
        double sizeofSubFoldNeg = pathsNeg.size()/10;
        int startSubFoldPos = 0;
        int startSubFoldNeg = 0;
        int endSubFoldPos = ((int) sizeofSubFoldPos);
        int endSubFoldNeg = ((int) sizeofSubFoldNeg);

        for(double i=0.0; i < 10.0; i++){
            startSubFoldPos = (int) (i*sizeofSubFoldPos);
            endSubFoldPos = (int) ((i+1.0)*sizeofSubFoldPos);
            Map<Path, Sentiment> fold = new HashMap<>(pathsPos.subList(startSubFoldPos, endSubFoldPos).stream()
                    .collect(Collectors.toMap(Function.identity(), dataSet::get))); //maps path, Sentiment from dataSet

            startSubFoldNeg = (int) (i*sizeofSubFoldNeg);
            endSubFoldNeg = (int) ((i+1.0)*sizeofSubFoldNeg);
            fold.putAll(pathsNeg.subList(startSubFoldNeg, endSubFoldNeg).stream()
                    .collect(Collectors.toMap(Function.identity(), dataSet::get))); //maps path, Sentiment from dataSet

            results.add(fold);
        }
        for(Map<Path, Sentiment> fold : results){
            int totalPos = 0;
            int totalNeg = 0;
            for(Path p : fold.keySet()){
                if(fold.get(p) == Sentiment.POSITIVE){
                    totalPos++;
                }
                else{
                    totalNeg++;
                }
            }
            System.out.println(totalNeg);
            System.out.println(totalPos);
        }
        return results;
    }

    @Override
    public double[] crossValidate(List<Map<Path, Sentiment>> folds) throws IOException {
        double[] scores = new double[10];

        Exercise2 implementation = new Exercise2();
        for(int i=0; i<10;i++) {
            Map<Path, Sentiment> trainingSet = new HashMap<>();
            for (int j = 0; j < 10; j++) {
                if (j != i) {
                    trainingSet.putAll(folds.get(j));
                }
            } // adds all but ith fold to trainingSet
            Set<Path> testSet = folds.get(i).keySet();
            Map<Path, Sentiment> truthTest = folds.get(i);
            Map<String, Map<Sentiment, Double>> tokenLogProbs = implementation.calculateSmoothedLogProbs(trainingSet);
            Map<Sentiment, Double> classProbabilites = implementation.calculateClassProbabilities(trainingSet);

            Map<Path, Sentiment> results = implementation.naiveBayes(testSet, tokenLogProbs, classProbabilites);
            scores[i] = checkAccuracy(results, truthTest);
        }

        return scores;
    }

    public double checkAccuracy(Map<Path,Sentiment> results, Map<Path, Sentiment> truth){
        double correct = 0.0;
        for (Path p : truth.keySet()){
            if (results.get(p) == truth.get(p)){
                correct++;
            }
        }
        return (correct / truth.size());
    }

    @Override
    public double cvAccuracy(double[] scores) {
        double result = 0;
        for(double i : scores){
            result += i;
        }
        return (result/10);
    }

    @Override
    public double cvVariance(double[] scores) {
        double variance = 0;
        double avg = cvAccuracy(scores);
        for(double i: scores){
            variance += (i - avg)*(i - avg);
        }
        variance /= 10;
        return variance;
    }
}
