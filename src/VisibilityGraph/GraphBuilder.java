package VisibilityGraph;

import java.util.Arrays;

/**
 * The backbone, arms, legs, and spleen of the pathfinder. This is responsible
 * for constructing the adjacency matrix used to construct the path. Is super
 * sexy
 *
 * @author amauryesparza
 */
public class GraphBuilder {

    private final Point[] waypoints;
    private int waypoint_index;

    private final Point[] obstacles;
    private int obstacle_index;

    private final boolean[] insideCorners;

    private int last_object_index;

    private final int length;
    private final int height;

    private final int[][] map;

    public static int MAX_OBSTACLES = 2000;
    public static int MAX_WAYPOINTS = 4000;

    public static double octile_constant = .41421356237;
    public static final double octile_multiplier = 1.6;

    private int[][] adj_matrix;
    private int [][] complete_matrix;

    public GraphBuilder(int length, int height) {
        this.length = length;
        this.height = height;
        waypoints = new Point[MAX_WAYPOINTS];
        waypoint_index = 0;

        obstacles = new Point[MAX_OBSTACLES];
        obstacle_index = 0;

        insideCorners = new boolean[MAX_WAYPOINTS];

        map = new int[length][height];

        last_object_index = 0;
    }

    /**
     * Adds an obstacle to be later evaluated.
     *
     * @param p the position of the obstacle
     */
    public void addObstacle(Point p) {
        if (!isValid(p.x, p.y)) {
            return;
        }
        map[p.x][p.y] = 1;
        obstacles[obstacle_index] = p;
        obstacle_index++;
    }

    /**
     * @param a the index of the first waypoint to check
     * @param b the index of the second waypoint to check
     * @return true if the waypoint a is visible to waypoint b, false otherwise.
     */
    private boolean isVisible(int a, int b) {
        if (adj_matrix[a][b] == -1) {
            return false;
        }
        if (adj_matrix[a][b] > 0) {
            return true;
        }
        Point p1 = waypoints[a];
        Point p2 = waypoints[b];
        
        if (bresenham(p1, p2) || (Math.abs(p1.x - p2.x) == 1 && Math.abs(p1.y - p2.y) == 1)) {
            int distance = manhattan(waypoints[a], waypoints[b]);
            adj_matrix[a][b] = distance;
            adj_matrix[b][a] = distance;
            complete_matrix[a][b] = distance;
            complete_matrix[b][a] = distance;
            return true;
        } else {
            adj_matrix[a][b] = -1;
            adj_matrix[b][a] = -1;
        }
        return false;
    }

    public boolean bresenham(Point p1, Point p2) {
        int x1 = p1.x;
        int y1 = p1.y;
        int x2 = p2.x;
        int y2 = p2.y;
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = (x1 < x2) ? 1 : -1;
        int sy = (y1 < y2) ? 1 : -1;
        int err = dx - dy;
        while (true) {
            if (map[x1][y1] == 1) {
                return false;
            }
            if (x1 == x2 && y1 == y2) {
                break;
            }
            int e2 = err << 1;
            if (e2 > -dy) {
                err = err - dy;
                x1 = x1 + sx;
            }
            if (map[x1][y1] == 1) {
                return false;
            }           
            if (x1 == x2 && y1 == y2) {
                break;
            }
            if (e2 < dx) {
                err = err + dx;
                y1 = y1 + sy;
            }
        }
        return true;
    }

    /**
     * Finds the Manhattan distance between the two given points.
     *
     * @param p1 first coordinate
     * @param p2 second coordinate
     * @return the Manhattan distance between the points.
     */
    private int manhattan(Point p1, Point p2) {
        if (p1.x > p2.x) {
            if (p1.y > p2.y) {
                return p1.x - p2.x + p1.y - p2.y;
            } else {
                return p1.x - p2.x + p2.y - p1.y;
            }
        } else {
            if (p1.y > p2.y) {
                return p2.x - p1.x + p1.y - p2.y;
            } else {
                return p2.x - p1.x + p2.y - p1.y;
            }
        }
    }

    /**
     * Checks all newly added obstacles for waypoints, adds any found, then
     * rebuilds the adjacency matrix. You *must* ensure that you *only* update
     * when an entire contiguous block of obstacles has been fully explored.
     * Else, visibility checking will fail. If you want to be able to add
     * objects incrementally and update simultaneously, use the reset() method
     * before updating.
     */
    public void buildMatrix() {
        for (int i = last_object_index; i < obstacle_index; i++) {
            int x = obstacles[i].x;
            int y = obstacles[i].y;
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    int temp_x = x + j;
                    int temp_y = y + k;
                    if (isValid(temp_x, temp_y) && map[temp_x][temp_y] == 0) {
                        if (isOutsideCorner(temp_x, temp_y)) {
                            map[temp_x][temp_y] = 2;
                            waypoints[waypoint_index] = new Point(temp_x, temp_y);
                            waypoint_index++;
                        } else if (isInsideCorner(temp_x, temp_y)) {
                            map[temp_x][temp_y] = 2;
                            insideCorners[waypoint_index] = true;
                            waypoints[waypoint_index] = new Point(temp_x, temp_y);
                            waypoint_index++;
                        }
                    }
                }
            }
        }
        last_object_index = obstacle_index;
        adj_matrix = new int[waypoint_index + 2][waypoint_index + 2];
        complete_matrix = new int[waypoint_index + 2][waypoint_index + 2];
    }

    private boolean isOutsideCorner(int x, int y) {
        if (x > 0 && y > 0 && map[x - 1][y - 1] == 1) {
            if (map[x - 1][y] != 1 && map[x][y - 1] != 1) {
                return true;
            }
        }
        if (x + 1 < length && y > 0 && map[x + 1][y - 1] == 1) {
            if (map[x + 1][y] != 1 && map[x][y - 1] != 1) {
                return true;
            }
        }
        if (x + 1 < length && y + 1 < height && map[x + 1][y + 1] == 1) {
            if (map[x + 1][y] != 1 && map[x][y + 1] != 1) {
                return true;
            }
        }
        if (x > 0 && y + 1 < height && map[x - 1][y + 1] == 1) {
            if (map[x - 1][y] != 1 && map[x][y + 1] != 1) {
                return true;
            }
        }
        return false;
    }

    private boolean isInsideCorner(int x, int y) {
        if (isValid(x, y - 1) && map[x][y - 1] == 1) {
            if (isValid(x + 1, y) && map[x + 1][y] == 1 && map[x + 1][y - 1] != 1) {
                return true;
            }
            if (isValid(x - 1, y) && map[x - 1][y] == 1 && map[x - 1][y - 1] != 1) {
                return true;
            }
        }
        if (isValid(x, y + 1) && map[x][y + 1] == 1) {
            if (isValid(x + 1, y) && map[x + 1][y] == 1 && map[x + 1][y + 1] != 1) {
                return true;
            }
            if (isValid(x - 1, y) && map[x - 1][y] == 1 && map[x - 1][y + 1] != 1) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return true if the coordinates are inside the boundaries of the map,
     * else false
     */
    private boolean isValid(int x, int y) {
        return (x < length && x >= 0 && y < height && y >= 0);
    }

    /**
     *
     * @return All assigned waypoints. The length does not represent the number
     * of waypoints.
     */
    public Point[] getWayPoints() {
        return waypoints;
    }

    /**
     *
     * @return The number of found waypoints.
     */
    public int num_waypoints() {
        return waypoint_index;
    }

    /**
     * getIndex(getWaypoint(x)) produces x if x is a valid waypoint, the
     * opposite is true.
     *
     * @param val The index of the waypoint in the waypoints array.
     * @return The position of the waypoint.
     */
    public Point getWaypoint(int val) {
        return waypoints[val];
    }

    /**
     * Given two points, finds a near-optimal path between them using the
     * already-made adjacency matrix.
     *
     * @param start the initial position
     * @param finish the desired ending location
     * @return a list of waypoints describing where to go. From each waypoint,
     * it is guaranteed that the next is visible.
     */
    public Point[] getPath(Point start, Point finish) {
        waypoints[waypoint_index] = start;
        waypoints[waypoint_index + 1] = finish;
        waypoint_index += 2; //temporary index adjustment
        int[] path = findPath(); //find the path
        waypoint_index -= 2; //replace
        if (path == null) { //if null, return null
            return null;
        }
        Point[] final_path = new Point[path.length]; //convert the path from indices to positions
        for (int i = 0; i < path.length; i++) {
            final_path[i] = waypoints[path[i]];
        }
        final_path[path.length - 1] = finish;

        return final_path;
    }

    private int[] findPath() {
        //Zero out the old path values
        int start = waypoint_index - 2;
        int finish = waypoint_index - 1;
        for (int i = 0; i < waypoint_index; i++) {
            adj_matrix[start][i] = 0;
            adj_matrix[i][start] = 0;
            adj_matrix[finish][i] = 0;
            adj_matrix[i][finish] = 0;
        }

        IntDoubleHeap to_evaluate = new IntDoubleHeap(waypoint_index); //Uses a simple heap as a priority queue
        double[] f_costs = new double[adj_matrix.length]; //the minimum cost to get to a vertex
        double[] g_costs = new double[adj_matrix.length];
        int[] min_index = new int[adj_matrix.length]; //the vertex's lowest cost neighbor
        boolean[] closed_set = new boolean[adj_matrix.length];
        boolean[] isInOpenSet = new boolean[adj_matrix.length];

        int count = 0;
        int current;
        to_evaluate.add(start, 0); //add the start to the queue
        f_costs[start] = 0;
        g_costs[start] = 0;

        while (!to_evaluate.isEmpty()) { //evaluate until there are no more vertices to evaluate
            count++;
            current = to_evaluate.pop(); //pop the lowest f-cost
            if (current == finish) { //if at the end, finish right now
                System.out.println(count);
                return cleanup(min_index, adj_matrix, finish, start);
            }
            closed_set[current] = true;
            for (int i = 0; i < adj_matrix[current].length; i++) { //visit all neighbors
                if (!closed_set[i] && isVisible(i, current)) {
                    System.out.println("Visible (" + i + "," + current+ ")");
                    double cost = adj_matrix[i][current] + g_costs[current]; //total cost to visit node i from current
                    if (!isInOpenSet[i] || cost < g_costs[i]) {
                        g_costs[i] = cost;
                        f_costs[i] = cost + octile(waypoints[i], waypoints[finish]) * octile_multiplier; //uses octile search heuristic times two.
                        min_index[i] = current;
                        if (!isInOpenSet[i]) {
                            to_evaluate.add(i, f_costs[i]);
                            isInOpenSet[i] = true;
                        }
                    }
                }
            }
        }
        return null;
    }

    public double octile(Point p, Point p2) {
        int x = Math.abs(p2.x - p.x);
        int y = Math.abs(p2.y - p.y);
        return Math.max(x, y) + octile_constant * Math.min(x, y);
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
    
    public int[][] getAdjMatriz(){
    
        for(int i = 0; i < waypoint_index + 2; i++){
            System.out.println("WayPoints " + i + "(" + waypoints[i].x + "," + waypoints[i].y + ")");
        }
        for(int i =0; i < adj_matrix.length; i++){
            for(int j = 0; j < adj_matrix[0].length; j++){
                System.out.print("(" + i + "," + j + "): " + adj_matrix[i][j]+"\t");
            }
            System.out.println();
        }
        return adj_matrix;
    }
}
