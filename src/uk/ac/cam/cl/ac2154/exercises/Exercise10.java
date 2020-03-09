package uk.ac.cam.cl.ac2154.exercises;

import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise10;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Exercise10 implements IExercise10 {
    @Override
    public Map<Integer, Set<Integer>> loadGraph(Path graphFile) throws IOException {
        Map<Integer, Set<Integer>> graph = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(graphFile.toString()));

        String line = reader.readLine();

        while (line != null) {
            String[] splitLine = line.split(" ");
            int nodeA = Integer.parseInt(splitLine[0]);
            int nodeB = Integer.parseInt(splitLine[1]);

            graph.putIfAbsent(nodeA, new HashSet<Integer>());
            graph.putIfAbsent(nodeB, new HashSet<Integer>());

            graph.get(nodeA).add(nodeB);
            graph.get(nodeB).add(nodeA);

            line = reader.readLine();
        }

        reader.close();

        return graph;
    }

    @Override
    public Map<Integer, Integer> getConnectivities(Map<Integer, Set<Integer>> graph) {
        HashMap<Integer, Integer> connectivities = new HashMap<>();

        for (Map.Entry<Integer, Set<Integer>> entry : graph.entrySet()){
            connectivities.put(entry.getKey(), entry.getValue().size());
        }

        return connectivities;
    }

    @Override
    public int getDiameter(Map<Integer, Set<Integer>> graph) {
        int maxD = 0;

        //dijkstra's algorithm on each node.
        for (int nodeA = 0; nodeA < graph.size(); nodeA++) {
            int d = dijk(graph, nodeA);
            if (d > maxD){
                maxD = d;
            }
        }
        return maxD;
    }

    int dijk(Map<Integer, Set<Integer>> graph, int nodeA) {
        int nNodes = Collections.max(graph.keySet())+1;
        boolean[] visNodes = new boolean[nNodes];
        int[] distances = new int[nNodes];
        Arrays.fill(visNodes, false);
        Arrays.fill(distances, Integer.MAX_VALUE);
        distances[nodeA] = 0;
        int currentNode = nodeA;
        boolean allVisited = false;
        int maxD = 0;

        while (!allVisited) {
            for (int neighbour : graph.getOrDefault(currentNode, new HashSet<>())) {
                if (!visNodes[neighbour]) {
                    distances[neighbour] = Math.min(distances[currentNode] + 1, distances[neighbour]);
                }
            }

            visNodes[currentNode] = true;
            if (distances[currentNode] > maxD) {
                maxD = distances[currentNode];
            }

            int newNode = currentNode;
            int minD = Integer.MAX_VALUE;
            for (int jNode = 0; jNode < nNodes; jNode++) {
                if (!visNodes[jNode] && distances[jNode] < minD) {
                    newNode = jNode;
                    minD = distances[jNode];
                }
            }

            if (newNode == currentNode) {
                allVisited = true;
            }
            else {
                currentNode = newNode;
            }
        }

        return maxD;
    }
}
