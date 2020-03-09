package uk.ac.cam.cl.ac2154.exercises;

import uk.ac.cam.cl.mlrd.exercises.social_networks.IExercise12;

import java.util.*;

public class Exercise12 implements IExercise12 {
    @Override
    public List<Set<Integer>> GirvanNewman(Map<Integer, Set<Integer>> graph, int minimumComponents) {
        Double tolerance = 0.000001;
        List<Set<Integer>> clusters = getComponents(graph);

        while (clusters.size() < minimumComponents && getNumberOfEdges(graph) > 0) {
            Map<Integer, Map<Integer, Double>> betweenness = getEdgeBetweenness(graph);
            List<Integer[]> highestEdges = new ArrayList<>();
            Double highestval = 0.0;

            for (int v : graph.keySet()) {
                for (int w : graph.keySet()) {
                    Double val = betweenness.get(v).get(w);
                    if (val > highestval + tolerance) {
                        highestEdges = new ArrayList<>();
                        highestEdges.add(new Integer[] {v, w});
                        highestval = val;
                    }
                    else if (Math.abs(highestval - val) < tolerance) {
                        highestEdges.add(new Integer[] {v, w});
                    }
                }
            }

            for (Integer[] edge : highestEdges) {
                graph.get(edge[0]).remove(edge[1]);
            }

            clusters = getComponents(graph);
        }

        return clusters;
    }

    @Override
    public int getNumberOfEdges(Map<Integer, Set<Integer>> graph) {
        int nNodes = 0;
        for (int nodeA : graph.keySet()) {
            for (int nodeB : graph.get(nodeA)) {
                if (nodeB > nodeA) {
                    nNodes++;
                }
            }
        }
        return nNodes;
    }

    @Override
    public List<Set<Integer>> getComponents(Map<Integer, Set<Integer>> graph) {
        List<Set<Integer>> components = new ArrayList<>();
        Set<Integer> visitedNodes = new HashSet<>();
        Stack<Integer> stack = new Stack<>();

        for (int jNode : graph.keySet()) {
            if (!visitedNodes.contains(jNode)) {
                Set<Integer> component = new HashSet<>();
                stack.push(jNode);

                while (!stack.empty()) {
                    Integer currentNode = stack.pop();
                    component.add(currentNode);

                    for (int nextNode : graph.get(currentNode)) {
                        if (!visitedNodes.contains(nextNode) && !stack.contains(nextNode)) {
                            stack.push(nextNode);
                        }
                    }
                    visitedNodes.add(currentNode);
                }
                components.add(component);
            }
        }

        return components;
    }

    @Override
    public Map<Integer, Map<Integer, Double>> getEdgeBetweenness(Map<Integer, Set<Integer>> graph) {
        Set<Integer> V                          = graph.keySet();
        Queue<Integer> Q                        = new LinkedList<>();
        Stack<Integer> S                        = new Stack<>();
        Map<Integer, Integer> dist              = new HashMap<>();
        Map<Integer, List<Integer>> Pred        = new HashMap<>();
        Map<Integer, Double> sigma              = new HashMap<>();
        Map<Integer, Double> delta              = new HashMap<>();
        Map<Integer, Map<Integer, Double>> cB  = new HashMap<>();
        for (int v : V) {
            Map<Integer, Double> row = new HashMap<>();
            for (int w : V) {
                row.put(w, (double) 0);
            }
            cB.put(v, row);
        }

        for (int s : V) {
            // single source shortest-paths problem

            //initialization
            for (int w : V) {
                Pred.put(w, new ArrayList<>());
            }
            for (int t : V) {
                dist.put(t, -1);
                sigma.put(t, (double) 0);
            }
            dist.put(s, 0);
            sigma.put(s, (double) 1);
            Q.add(s);

            while (!Q.isEmpty()) {
                Integer v = Q.remove();
                S.push(v);
                for (int w : graph.get(v)) {
                    // path discovery
                    if (dist.get(w) == -1) {
                        dist.put(w, dist.get(v)+1);
                        Q.add(w);
                    }

                    // path counting
                    if (dist.get(w) == dist.get(v)+1) {
                        sigma.put(w, sigma.get(w) + sigma.get(v));
                        Pred.get(w).add(v);
                    }
                }
            }

            // accumulation
            for (int v : V) {
                delta.put(v, (double) 0);
            }
            while (!S.empty()) {
                int w = S.pop();
                for (int v : Pred.get(w)) {
                    Double c = (sigma.get(v)/sigma.get(w)) * (1 + delta.get(w));
                    cB.get(v).put(w, cB.get(v).get(w)+c);
                    delta.put(v, delta.get(v) + c);
                }
            }
        }

        return cB;
    }
}
