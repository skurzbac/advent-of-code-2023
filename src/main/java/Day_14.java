import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.IntStream;

public class Day_14 {

    private static boolean secondStar = true;
    private static final String INPUT = "https://adventofcode.com/2023/day/14/input";

    public static void main(String[] args) throws URISyntaxException, IOException {
        CookieHandler.setDefault(new CookieManager());

        HttpCookie sessionCookie = new HttpCookie("session", SessionCookie.SESSION_VALUE);
        sessionCookie.setPath("/");
        sessionCookie.setVersion(0);

        ((CookieManager) CookieHandler.getDefault()).getCookieStore().add(new URI("https://adventofcode.com"),
                sessionCookie);

//        var lines = HttpClient.newBuilder()
//                .cookieHandler(CookieHandler.getDefault())
//                .build()
//                .sendAsync(HttpRequest.newBuilder(URI.create(INPUT)).build(),
//                        HttpResponse.BodyHandlers.ofLines())
//                .thenApply(HttpResponse::body)
//                .join();

        var lines = Files.readString(Path.of("/Users/Stefan.Kurzbach/private-repos/advent-of-code-2023/src/main/java/Day_14.txt")).lines().toList();
        int[][] grid = new int[lines.size()][lines.get(0).length()];
        for (int y = 0; y < lines.size(); y++) {
            String line = lines.get(y);
            for (int x = 0; x < line.length(); x++) {
                grid[y][x] = line.charAt(x);
            }
        }

        if (secondStar) {
            long start = System.currentTimeMillis();
            for (int cycle = 0; cycle < 1000000000; cycle++) {
                for (int i = 0; i < lines.size(); i++) {
                    for (int j = 0; j < lines.size(); j++) {
                        if (!tiltNorth(grid, j))
                            break;
                    }
                }
//                print(grid, getLoads(grid));
                for (int i = 0; i < lines.size(); i++) {
                    for (int j = 0; j < lines.size(); j++) {
                        if (!tiltWest(grid, j))
                            break;
                    }
                }
//                print(grid, getLoads(grid));
                for (int i = 0; i < lines.size(); i++) {
                    for (int j = 0; j < lines.size(); j++) {
                        if (!tiltSouth(grid, j))
                            break;
                    }
                }
//                print(grid, getLoads(grid));
                for (int i = 0; i < lines.size(); i++) {
                    for (int j = 0; j < lines.size(); j++) {
                        if (!tiltEast(grid, j))
                            break;
                    }
                }
//                print(grid, getLoads(grid));
//                if (cycle == 10000) {
                //System.out.print(cycle);
                //System.out.print(" ");
                //calculate cycles per second
//                    var diff = System.currentTimeMillis() - start;
//                    if(diff == 0) {
//                        System.out.println("0");
//                        continue;
//                    }
//                    double cyclesPerSecond = (cycle / (diff / 1000));
//                    System.out.format("%.2f\n", cyclesPerSecond);
                var loads = getLoads(grid);
//                    print(grid, loads);
                System.out.println("cycle " + (cycle+1) % 93 + " " + IntStream.of(loads).sum());
//                    System.exit(0);
//                }
            }
        } else {
            for (int i = 0; i < lines.size(); i++) {
                for (int j = 0; j < lines.size(); j++) {
                    if (!tiltNorth(grid, j))
                        break;
                }
            }
        }

        var loads = getLoads(grid);

        print(grid, loads);
        System.out.println(IntStream.of(loads).sum());

    }

    private static int[] getLoads(int[][] grid) {
        int[] loads = new int[grid.length];
        for (int y = 0; y < grid.length; y++) {
            int distanceFromBottom = grid.length - y;
            loads[y] = IntStream.of(grid[y]).map(operand -> operand == 'O' ? distanceFromBottom : 0).sum();
        }
        return loads;
    }

    private static void print(int[][] grid, int[] loads) {
        for (int y = 0; y < grid.length; y++) {
            int[] row = grid[y];
            for (int x = 0; x < row.length; x++) {
                System.out.print((char) row[x]);
            }
            System.out.print(" " + loads[y]);
            System.out.println();
        }
        System.out.println();
    }

    private static boolean tiltNorth(int[][] grid, int start) {
        boolean changed = false;
        for (int y = start + 1; y < grid.length; y++) {
            int[] row = grid[y];
            int[] previousRow = grid[y - 1];
            for (int x = 0; x < row.length; x++) {
                if (previousRow[x] == '.' && row[x] == 'O') {
                    previousRow[x] = row[x];
                    row[x] = '.';
                    changed = true;
                }
            }
        }
        return changed;
    }

    private static boolean tiltWest(int[][] grid, int start) {
        boolean changed = false;
        for (int y = 0; y < grid.length; y++) {
            int[] row = grid[y];
            for (int x = start + 1; x < row.length; x++) {
                if (row[x - 1] == '.' && row[x] == 'O') {
                    row[x - 1] = row[x];
                    row[x] = '.';
                    changed = true;
                }
            }
        }
        return changed;
    }

    private static boolean tiltSouth(int[][] grid, int start) {
        boolean changed = false;
        for (int y = grid.length - 2 - start; y >= 0; y--) {
            int[] row = grid[y];
            int[] lastRow = grid[y + 1];
            for (int x = 0; x < row.length; x++) {
                if (lastRow[x] == '.' && row[x] == 'O') {
                    lastRow[x] = row[x];
                    row[x] = '.';
                    changed = true;
                }
            }
        }
        return changed;
    }

    private static boolean tiltEast(int[][] grid, int start) {
        boolean changed = false;
        for (int y = 0; y < grid.length; y++) {
            int[] row = grid[y];
            for (int x = row.length - 2 - start; x >= 0; x--) {
                if (row[x + 1] == '.' && row[x] == 'O') {
                    row[x + 1] = row[x];
                    row[x] = '.';
                    changed = true;
                }
            }
        }
        return changed;
    }
}
