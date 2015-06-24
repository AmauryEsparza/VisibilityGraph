/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package VisibilityGraph;

/**
 * A quick and dirty array-based heap implementation, designed for use as a
 * priority queue. 
 *
 * @author amauryesparza
 */
public class IntDoubleHeap {

    private final int capacity;
    private int index;
    private final int[] points;
    private final double[] costs;
    
    public IntDoubleHeap(int capacity) {
        this.capacity = capacity;
        points = new int[capacity];
        costs = new double[capacity];
        index = 0;
    }

    @Override
    public String toString() {
        String str = "[";
        for (int i = 0; i < index-1; i++) {
            str += points[i] + ":" + costs[i]  + " ";
        }
        if (index > 0) str += points[index-1] + ":" + costs[index-1];
        str += "]";
        return str;
    }
    

    public void add(int p, double cost) {
        points[index] = p;
        costs[index] = cost;
        int temp_index = index;
        if (index < capacity) {
            index++;
        }
        while (true) {
            int parent_index = (temp_index - 1) / 2;
            double parent_cost = costs[parent_index];
            if (cost < parent_cost) {
                swap(temp_index, parent_index);
                temp_index = parent_index;
            } else {
                return;
            }
        }
    }

    public int pop() {
        int min_point = points[0];
        index--;
        if (index <= 0) return min_point;
        points[0] = points[index];
        costs[0] = costs[index];

        int temp_index = 0;
        double current_cost = costs[0];
        while (true) {
            int left_index = temp_index * 2 + 1;
            int right_index = temp_index * 2 + 2;
            double min_cost = current_cost;
            int min_index = 0;
            if (left_index < index && costs[left_index] < min_cost) {
                min_cost = costs[left_index];
                min_index = left_index;
            }
            if (right_index < index && costs[right_index] < min_cost) {
                min_cost = costs[right_index];
                min_index = right_index;
            }
            if (min_cost < current_cost) {
                swap(temp_index, min_index);
                temp_index = min_index;
            } else {
                return min_point;
            }

        }
    }

    public boolean isEmpty() {
        return (index == 0);
    }
    
    private void swap(int a, int b) {
        int temp_pair = points[a];
        double temp_cost = costs[a];

        points[a] = points[b];
        points[b] = temp_pair;
        
        costs[a] = costs[b];
        costs[b] = temp_cost;
    }
}
