package uk.ac.cam.cl.ac2154.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.List;
import java.util.HashMap;


public class Exercise1 implements IExercise1 {


    public HashMap<String, boolean[]> lex = new HashMap<>();
    // boolean array is [isSTRONG, isPOSITIVE]

    @Override
    public Map<Path, Sentiment> simpleClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
        ReadLexicon(lexiconFile); //Reads lexicon in lexiconFile to Lex
        boolean[] tempValue = {false,false};
        boolean intensityHold;
        boolean polarityHold;
        int PositiveCount = 0;
        int NegativeCount = 0;
        HashMap<Path, Sentiment> results = new HashMap<Path, Sentiment>();

        for (Path p : testSet){

            PositiveCount = 0;
            NegativeCount = 0;
            List<String> tokens = Tokenizer.tokenize(p);
            for (String token : tokens){
                tempValue = lex.get(token);
                if (tempValue != null) {
                    intensityHold = tempValue[0];
                    polarityHold = tempValue[1];
                    if (polarityHold) {
                        PositiveCount++;
                    } else {
                        NegativeCount++;
                    }
                }
            }
            if (PositiveCount >= NegativeCount){
                results.put(p, Sentiment.POSITIVE);
            }
            else{
                results.put(p, Sentiment.NEGATIVE);
            }
        }

        return results;
    }

    @Override
    public double calculateAccuracy(Map<Path, Sentiment> trueSentiments, Map<Path, Sentiment> predictedSentiments) {
        double correct = 0;
        double total = 0;
        for (Path p : trueSentiments.keySet()){
            total++;
            if (trueSentiments.get(p) == predictedSentiments.get(p)){
                correct++;
            }
        }
        return (correct/total);
    }

    @Override
    public Map<Path, Sentiment> improvedClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
        ReadLexicon(lexiconFile); //Reads lexicon in lexiconFile to Lex
        boolean[] tempValue = {false,false};
        boolean intensityHold;
        boolean polarityHold;
        int PositiveCount = 0;
        int NegativeCount = 0;
        HashMap<Path, Sentiment> results = new HashMap<Path, Sentiment>();

        for (Path p : testSet){

            PositiveCount = 0;
            NegativeCount = 0;
            List<String> tokens = Tokenizer.tokenize(p);
            for (String token : tokens){
                tempValue = lex.get(token);
                if (tempValue != null) {
                    intensityHold = tempValue[0];
                    polarityHold = tempValue[1];
                    if (polarityHold) {
                        if (!intensityHold) {
                            PositiveCount += 0.15; //weight weak ones 10x smaller
                        }
                        else{
                            PositiveCount++;
                        }
                    } else {
                        if (!intensityHold) {
                            NegativeCount += 0.15; //weight weak ones 10x smaller
                        }
                        else {

                            NegativeCount++;
                        }
                    }
                }
            }
            if (PositiveCount > NegativeCount){
                results.put(p, Sentiment.POSITIVE);
            }
            else{
                results.put(p, Sentiment.NEGATIVE);
            }
        }

        return results;
    }

    public void InsertToLex(String Entry){
        //format Entry should be "word=awful intensity=strong polarity=negative"

        int indexStart = Entry.indexOf("word=") + 5;
        int indexEnd = Entry.indexOf(' ', indexStart);

        String word = Entry.substring(indexStart, indexEnd);

        indexStart = Entry.indexOf('=', indexStart) + 1;

        boolean Intensity = (Entry.toCharArray()[indexStart] == 's');

        indexStart = Entry.indexOf('=', indexStart) + 1;

        boolean Polarity = (Entry.toCharArray()[indexStart] == 'p');

        boolean[] hold = {Intensity, Polarity};
        lex.put(word, hold);


    }
    public void ReadLexicon(Path filepath) throws FileNotFoundException {
        // pass the path to the file as a parameter
        File file = new File(filepath.toString());
        Scanner sc = new Scanner(file);

        while (sc.hasNextLine()){
            InsertToLex(sc.nextLine());
        }

    }

}
