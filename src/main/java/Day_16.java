import org.assertj.core.util.Lists;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.stream.IntStream;

public class Day_16 {

    private static boolean secondStar = true;
    private static final String INPUT = "https://adventofcode.com/2023/day/16/input";

    public static void main(String[] args) throws URISyntaxException, IOException {
        CookieHandler.setDefault(new CookieManager());

        HttpCookie sessionCookie = new HttpCookie("session", SessionCookie.SESSION_VALUE);
        sessionCookie.setPath("/");
        sessionCookie.setVersion(0);

        ((CookieManager) CookieHandler.getDefault()).getCookieStore().add(new URI("https://adventofcode.com"),
                sessionCookie);

        var lines = HttpClient.newBuilder()
                .cookieHandler(CookieHandler.getDefault())
                .build()
                .sendAsync(HttpRequest.newBuilder(URI.create(INPUT)).build(),
                        HttpResponse.BodyHandlers.ofLines())
                .thenApply(HttpResponse::body)
                .join()
                .toList();

//        var lines = Files.readString(Path.of("/Users/Stefan.Kurzbach/private-repos/advent-of-code-2023/src/main/java/Day_16.txt")).lines().toList();
        grid = new int[lines.size()][lines.get(0).length()];
        visited = new int[lines.size()][lines.get(0).length()];

        for (int y = 0; y < lines.size(); y++) {
            String line = lines.get(y);
            for (int x = 0; x < line.length(); x++) {
                grid[y][x] = line.charAt(x);
            }
        }
        //        sanityCheck();

        if (secondStar) {
            var max1 = IntStream.range(0, grid.length - 1).map(y -> {
                        clearVisited();
                        var v1 = getVisitedCount(new Ray(0, y, dir_right));
                        var v2 = getVisitedCount(new Ray(grid[y].length - 1, y, dir_left));
                        return Math.max(v1, v2);
                    })
                    .peek(System.out::println)
                    .max().orElseThrow();
            var max2 = IntStream.range(0, grid[0].length - 1).map(x -> {
                        clearVisited();
                        var v1 = getVisitedCount(new Ray(x, 0, dir_down));
                        var v2 = getVisitedCount(new Ray(x, grid.length - 1, dir_up));
                        return Math.max(v1, v2);
                    })
                    .peek(System.out::println)
                    .max().orElseThrow();
            System.out.println(Math.max(max1, max2));
        } else {
            clearVisited();
            var visitedCount = getVisitedCount(new Ray(0, 0, dir_right));
            System.out.println(visitedCount);
        }
    }

    private static void clearVisited() {
        for (int y2 = 0; y2 < grid.length; y2++) {
            for (int x = 0; x < grid[0].length; x++) {
                visited[y2][x] = -1;
            }
        }
    }

    private static int getVisitedCount(Ray initial) {
        List<Ray> alreadyBeenThere = Lists.newArrayList();
        Queue<Ray> rays = new ArrayDeque<>();
        rays.add(initial);
        int steps = 0;
        while (!rays.isEmpty() && steps++ < 1000000000) {
//            if (steps % 10000000 == 0)
//                System.out.println(rays.size());
            Ray ray = rays.remove();
            if (alreadyBeenThere.contains(ray)) {
                continue;
            } else {
                alreadyBeenThere.add(ray.copy());
            }
            int x = ray.x;
            int y = ray.y;
            int dir = ray.dir;
            visit(x, y);
            if (isSplit(x, y, dir)) {
                int[][] splitMoves = getSplitMoves(x, y, dir);
                for (int j = 0; j < moves.length; j++) {
                    int[] move = moves[j];
                    if (move == splitMoves[0] || move == splitMoves[1]) {
                        var splitRay = new Ray(move[0] + x, move[1] + y, move_dir[j]);
                        if (splitRay.inBounds()) {
                            rays.add(splitRay);
                        }
                    }
                }
            } else if (isMirror(x, y)) {
                int mirroredDirection = mirroredDirection(x, y, dir);
                int[] move = moves[mirroredDirection];
                ray.dir = mirroredDirection;
                ray.x += move[0];
                ray.y += move[1];
                if (ray.inBounds()) {
                    rays.add(ray);
                }
            } else {
                int[] move = moves[dir];
                ray.x += move[0];
                ray.y += move[1];
                if (ray.inBounds()) {
                    rays.add(ray);
                }
            }
        }
        int visitedCount = 0;
        for (int[] row : visited) {
            for (int i : row) {
//                System.out.print(i == -1 ? "." : "#");
                if (i == 0) {
                    visitedCount++;
                }
            }
//            System.out.println();
        }
//        System.out.println(steps);
//        System.out.println(rays.size());
        return visitedCount;
    }

    static class Ray {
        int x;
        int y;
        int dir;

        Ray(int x, int y, int dir) {
            this.x = x;
            this.y = y;
            this.dir = dir;
        }

        public boolean inBounds() {
            return x >= 0 && x < grid[0].length && y >= 0 && y < grid.length;
        }

        @Override
        public int hashCode() {
            return x + y * 10000 + dir * 100000000;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Ray other) {
                return other.x == x && other.y == y && other.dir == dir;
            }
            return false;
        }

        public Ray copy() {
            return new Ray(x, y, dir);
        }
    }

    private static void visit(int x, int y) {
        visited[y][x] = 0;
    }

    private static void sanityCheck() {
        assert isFree(2, 2);

        assert isSplit(1, 0, dir_left);
        assert isSplit(1, 0, dir_right);
        assert !isSplit(1, 0, dir_up);
        assert !isSplit(1, 0, dir_down);

        assert isSplit(2, 1, dir_up);
        assert isSplit(2, 1, dir_down);
        assert !isSplit(2, 1, dir_left);
        assert !isSplit(2, 1, dir_right);

        assert getSplitMoves(1, 0, dir_left)[0] == go_up;
        assert getSplitMoves(1, 0, dir_left)[1] == go_down;
        assert getSplitMoves(1, 0, dir_right)[0] == go_up;
        assert getSplitMoves(1, 0, dir_right)[1] == go_down;

        assert getSplitMoves(2, 1, dir_up)[0] == go_left;
        assert getSplitMoves(2, 1, dir_up)[1] == go_right;
        assert getSplitMoves(2, 1, dir_down)[0] == go_left;
        assert getSplitMoves(2, 1, dir_down)[1] == go_right;

        assert getSplitMoves(1, 7, dir_up)[0] == go_left;
        assert getSplitMoves(1, 7, dir_up)[1] == go_right;
        assert getSplitMoves(1, 7, dir_down)[0] == go_left;
        assert getSplitMoves(1, 7, dir_down)[1] == go_right;

        assert isMirror(4, 1);
        assert isMirror(4, 6);

        assert mirroredDirection(4, 6, dir_left) == dir_down;
        assert mirroredDirection(4, 6, dir_down) == dir_left;
        assert mirroredDirection(4, 6, dir_up) == dir_right;
        assert mirroredDirection(4, 6, dir_right) == dir_up;

        assert mirroredDirection(4, 1, dir_left) == dir_up;
        assert mirroredDirection(4, 1, dir_up) == dir_left;
        assert mirroredDirection(4, 1, dir_down) == dir_right;
        assert mirroredDirection(4, 1, dir_right) == dir_down;
    }

    private static int mirroredDirection(int x, int y, int dir) {
        if (isMirrorLeftBottom(x, y)) {
            if (dir == dir_left) {
                return dir_down;
            } else if (dir == dir_down) {
                return dir_left;
            } else if (dir == dir_up) {
                return dir_right;
            } else if (dir == dir_right) {
                return dir_up;
            }
        } else if (isMirrorLeftTop(x, y)) {
            if (dir == dir_left) {
                return dir_up;
            } else if (dir == dir_down) {
                return dir_right;
            } else if (dir == dir_up) {
                return dir_left;
            } else if (dir == dir_right) {
                return dir_down;
            }
        }
        throw new IllegalArgumentException();
    }

    private static boolean isMirror(int x, int y) {
        return isMirrorLeftTop(x, y) || isMirrorLeftBottom(x, y);
    }

    private static boolean isMirrorLeftTop(int x, int y) {
        return grid[y][x] == '\\';
    }

    private static boolean isMirrorLeftBottom(int x, int y) {
        return grid[y][x] == '/';
    }

    private static boolean isSplit(int x, int y, int dir) {
        return split_v(x, y, dir) || split_h(x, y, dir);
    }

    private static int[][] getSplitMoves(int x, int y, int dir) {
        if (split_v(x, y, dir)) {
            return new int[][]{go_up, go_down};
        } else if (split_h(x, y, dir)) {
            return new int[][]{go_left, go_right};
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static int[][] visited;

    static int[][] grid;

    private static boolean split_v(int x, int y, int dir) {
        return dir % 2 == 0 && grid[y][x] == '|';
    }

    private static boolean split_h(int x, int y, int dir) {
        return dir % 2 == 1 && grid[y][x] == '-';
    }

    private static boolean isFree(int x, int y) {
        return grid[y][x] == '.';
    }


    static int dir_left = 0;
    static int dir_up = 1;
    static int dir_right = 2;
    static int dir_down = 3;

    private static int[] go_left = {-1, 0};
    private static int[] go_up = {0, -1};
    private static int[] go_right = {1, 0};
    private static int[] go_down = {0, 1};

    static int[] move_dir = {dir_left, dir_up, dir_right, dir_down};
    static int[][] moves = {go_left, go_up, go_right, go_down};
}
