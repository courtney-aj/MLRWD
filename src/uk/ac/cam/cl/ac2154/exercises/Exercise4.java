package uk.ac.cam.cl.ac2154.exercises;

import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise4;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Tokenizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.math.BigDecimal;

public class Exercise4 implements IExercise4 {

    public HashMap<String, boolean[]> lex = new HashMap<>();
    // boolean array is [isSTRONG, isPOSITIVE]

    @Override
    public Map<Path, Sentiment> magnitudeClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
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
                            PositiveCount += 1.0;
                        }
                        else{
                            PositiveCount += 2.0;
                        }
                    } else {
                        if (!intensityHold) {
                            NegativeCount += 1.0;
                        }
                        else {

                            NegativeCount += 2.0;
                        }
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
    public double signTest(Map<Path, Sentiment> actualSentiments, Map<Path, Sentiment> classificationA, Map<Path, Sentiment> classificationB) {
        double NULL = 0;
        double aPLUS = 0;
        double aMINUS = 0;
        BigDecimal n, k, p;
        boolean truthIsPos;
        boolean aIsPos;
        boolean bIsPos;

        for (Path path : actualSentiments.keySet()){
            truthIsPos = (actualSentiments.get(path) == Sentiment.POSITIVE);
            aIsPos = (classificationA.get(path) == Sentiment.POSITIVE);
            bIsPos = (classificationB.get(path) == Sentiment.POSITIVE);

            if (aIsPos == bIsPos){
                NULL++;
            }
            else if (aIsPos == truthIsPos){
                aPLUS++;
            }
            else{
                aMINUS++;
            }
        }

        n = BigDecimal.valueOf(Math.ceil(NULL/2)*2 + aMINUS + aPLUS);
        k = BigDecimal.valueOf(Math.ceil(NULL/2) + Math.min(aMINUS, aPLUS));

        p = BigDecimal.valueOf(0.0);

        for(int i = 0; i <= k.intValue(); i++ ){
            BigDecimal temp = BigDecimal.ONE;
            temp = temp.multiply(binomial(n.intValue(), i));
            //temp.multiply(BigDecimal.valueOf(0.5).pow(i));
            //temp.multiply(BigDecimal.valueOf(0.5).pow(n - i));
            //temp = temp.multiply(BigDecimal.valueOf(0.5).pow(n.intValue()));
            p = p.add(temp);
        }
        p = p.multiply(BigDecimal.valueOf(0.5).pow(n.intValue()));
        p = p.multiply(BigDecimal.valueOf(2.0));

        double pDouble = p.doubleValue();
        return pDouble;
    }

    static BigDecimal binomial(final int N, final int K) {
        BigDecimal ret = BigDecimal.ONE;
        for (int k = 0; k < K; k++) {
            ret = ret.multiply(BigDecimal.valueOf(N-k))
                    .divide(BigDecimal.valueOf(k+1));
        }
        return ret;
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
