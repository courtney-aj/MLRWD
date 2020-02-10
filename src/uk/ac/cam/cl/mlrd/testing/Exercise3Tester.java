package uk.ac.cam.cl.mlrd.testing;

import uk.ac.cam.cl.ac2154.exercises.Exercise3;
import uk.ac.cam.cl.ac2154.exercises.Exercise4;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.DataPreparation1;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.IExercise4;
import uk.ac.cam.cl.mlrd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrd.utils.DataSplit;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class Exercise3Tester {
    static final Path dataDirectory = Paths.get("data/sentiment_dataset");

    public static void main(String[] args) throws Exception {

        Path sentimentFile = dataDirectory.resolve("review_sentiment");

        Exercise3 implementation = new Exercise3();

        Path lexiconFile = Paths.get("data/sentiment_lexicon");
}

}
