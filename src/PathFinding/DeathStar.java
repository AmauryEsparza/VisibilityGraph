/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PathFinding;

/**
 *
 * @author alexhuleatt, mikevoth
 */
public class DeathStar {

    /**
     * This method, when given an adjacency/cost matrix, a start, and end,
     * returns the sequence of vertices with the lowest possible cost to get from the start to end.
     *
     * @param adj_matrix is the adjacency/cost matrix
     * @param start is the beginning vertex
     * @param finish is the ending vertex
     * @return the sequence of vertices to travel through, or null if no path
     * exists
     */
    public static int[] findPath(int[][] adj_matrix, int start, int finish, int[] heuristics) {
        IntDoubleHeap to_evaluate = new IntDoubleHeap(adj_matrix.length); //Uses a simple heap as a priority queue
        int[] min_costs = new int[adj_matrix.length]; //the minimum cost to get to a vertex
        int[] min_index = new int[adj_matrix.length]; //the vertex's lowest cost neighbor
        boolean[] closed_set = new boolean[adj_matrix.length];
        to_evaluate.add(start, 0); //add the start to the queue

        for (int i = 0; i < min_costs.length; i++) { //initialize the two min arrays
            min_index[i] = -1;
            min_costs[i] = Integer.MAX_VALUE;
        }
        min_costs[start] = 0;
        int count = 0;
        while (!to_evaluate.isEmpty()) { //evaluate until there are no more vertices to evaluate
            count++;
            int current = to_evaluate.pop(); //pop the lowest cost vertex out
            System.out.println("Currently evaluating: " + current);
            if (current == finish) { //if at the end, finish right fucking now
                System.out.println(count);
                return cleanup(min_index, adj_matrix, finish, start);
            }
            closed_set[current] = true;
            for (int i = 0; i < adj_matrix[current].length; i++) { //visit all neighbors
                if (!closed_set[i]) {
                    int dis = adj_matrix[current][i];
                    int cost = dis + min_costs[current]; //total cost to visit node i from current
                    if (adj_matrix[current][i] > 0 && (min_costs[i] > cost || min_index[i] == -1)) {
                        min_costs[i] = cost;
                        min_index[i] = current;
                        to_evaluate.add(i, cost + heuristics[i]);
                    }
                }
            }
        }
        
        return null;
    }

    /**
     *
     * @param indices is the list representing the best vertex to travel to from
     * any other vertex
     * @param adj_matrix is the adjacency/cost matrix
     * @param finish is the end position
     * @param start is the beginning position
     * @return the ultimate path
     */
    private static int[] cleanup(int[] indices, int[][] adj_matrix, int finish, int start) {
        int current = finish;
        int[] path = new int[adj_matrix.length];
        int index = 0;
        while (current != start) {
            index++;
            path[adj_matrix.length - index] = current;
            current = indices[current];
        }
        int[] final_path = new int[index];
        System.arraycopy(path, path.length - index, final_path, 0, index);
        return final_path;
    }
}
