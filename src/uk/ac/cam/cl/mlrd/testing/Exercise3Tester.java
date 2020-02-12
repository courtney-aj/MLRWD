package uk.ac.cam.cl.mlrd.testing;

import javafx.util.Pair;
import uk.ac.cam.cl.ac2154.exercises.Exercise3;
import uk.ac.cam.cl.mlrd.utils.BestFit;
import uk.ac.cam.cl.mlrd.utils.ChartPlotter;

import java.awt.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.lang.Math;

public class Exercise3Tester {

    static final Path dataDirectory = Paths.get("data/large_dataset");

    public static void main(String[] args) throws Exception {


        Exercise3 implementation = new Exercise3();

        Path lexiconFile = Paths.get("data/sentiment_lexicon");

        Set<Path> dataSet = new HashSet<>();

        DirectoryStream<Path> files = Files.newDirectoryStream(dataDirectory);
        for (Path item : files) {
            dataSet.add(item);
        }
        LinkedList<Pair<String, Double>> hold = implementation.getFreqs(dataSet, lexiconFile);
        /*for(Pair<String, Double> p : hold) {
            System.out.println(p);
        }*/

        LinkedList<String> mylex = new LinkedList<>();
        mylex.add(0,"love");
        mylex.add(0,"hate");
        mylex.add(0,"disappoint");
        mylex.add(0,"annoying");
        mylex.add(0,"classic");
        mylex.add(0,"slow");
        mylex.add(0,"fun");
        mylex.add(0,"beautiful");
        mylex.add(0,"great");
        mylex.add(0,"awful");


        LinkedList<BestFit.Point> points2 = new LinkedList<>();

        LinkedList<BestFit.Point> points = new LinkedList<>();
        /*for(Double i=0.0; i<10000.0; i++){
            points.add(new BestFit.Point(i+1.0, hold.get(i.intValue()).getValue()));
            if(mylex.contains(hold.get(i.intValue()).getKey())) {
                points2.add(new BestFit.Point(i + 1.0, hold.get(i.intValue()).getValue()));
            }
        }*/
        //ChartPlotter.plotLines(points, points2);
        System.out.println("Done");


        LinkedList<BestFit.Point> points3 = new LinkedList<>();
        /*for(double i = 0.0; i<10000.0; i++){
            points3.add(new BestFit.Point(java.lang.Math.log(i+1.0), java.lang.Math.log(hold.get((int) i).getValue())));
        }*/
        int i=0;

        Map<BestFit.Point, Double> series = new HashMap<>();
        /*for (BestFit.Point p : points3){
            series.put(p, hold.get(i).getValue());
            i++;
        }*/
        BestFit.Line line = BestFit.leastSquares(series);
        double grad = line.gradient;
        double yint = line.yIntercept;


        LinkedList<BestFit.Point> points4 = new LinkedList<>();
        points4.add(new BestFit.Point(0, yint));
        points4.add(new BestFit.Point(-yint/grad, 0));


        //ChartPlotter.plotLines(points3, points4);
        System.out.println(implementation.exFreq(0, grad, yint));

        double k = implementation.exFreq(0, grad, yint);
        double alpha = -grad;

        System.out.println("k = " + k);
        System.out.println("alpha = " + alpha);

        LinkedList<Pair<Integer, Integer>> uniqtok = implementation.HeapsLaw(dataSet, lexiconFile);
        LinkedList<BestFit.Point> total = new LinkedList<>();
        LinkedList<BestFit.Point> heapslaw = new LinkedList<>();

        double i2 = 0;
        for(Pair<Integer, Integer> p: uniqtok){
            heapslaw.add(new BestFit.Point(java.lang.Math.log(java.lang.Math.pow(2.0, i2)), java.lang.Math.log(p.getKey())));
            total.add(new BestFit.Point(java.lang.Math.log(java.lang.Math.pow(2.0, i2)),java.lang.Math.log(p.getValue())));
            i2++;
        }

        ChartPlotter.plotLines(heapslaw, total);
    System.out.println("DONE");

    }


}
