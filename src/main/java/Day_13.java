import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Day_13 {

    private static boolean secondStar = true;
    private static final String INPUT = "https://adventofcode.com/2023/day/13/input";

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

        var lines = Files.readString(Path.of("/Users/Stefan.Kurzbach/private-repos/advent-of-code-2023/src/main/java/Day_13.txt")).lines().toList();
        //split patterns by empty lines
        List<List<String>> patterns = new ArrayList<>();
        int patternStart = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                patterns.add(lines.subList(patternStart, i));
                patternStart = i + 1;
            }
        }
        patterns.add(lines.subList(patternStart, lines.size()));

        int sum = 0;
        for (List<String> pattern : patterns) {
            var mirrorPointsForPattern = mirrorPointsForPattern(pattern);
            if (mirrorPointsForPattern.isEmpty())
                throw new RuntimeException("No mirror points found for pattern: " + pattern);
            sum += mirrorPointsForPattern.get(0);
        }
        System.out.println(sum);

        if (secondStar) {
            sum = 0;
            for (List<String> pattern : patterns) {
                var mirrorPointsForPattern = mirrorPointsForSmudgedPattern(pattern);
                if (mirrorPointsForPattern.size() > 1) {
                    System.out.println(pattern);
                    System.out.println(mirrorPointsForPattern);
                }
                sum += mirrorPointsForPattern.get(0);
            }
            System.out.println(sum);
        }
    }

    private static List<Integer> mirrorPointsForPattern(List<String> lines) {
        boolean[][] grid = new boolean[lines.size()][lines.get(0).length()];
        for (int y = 0; y < lines.size(); y++) {
            String line = lines.get(y);
            for (int x = 0; x < line.length(); x++) {
                grid[y][x] = line.charAt(x) == '#';
            }
        }

        var horizontalMirrorPoints = horizontalMirrorPoints(grid);
        if (!horizontalMirrorPoints.isEmpty()) {
            return horizontalMirrorPoints.stream().map(i -> i + 1).toList();
        }
        return verticalMirrorPoints(grid).stream().map(i -> (i + 1) * 100).toList();
    }

    private static List<Integer> mirrorPointsForSmudgedPattern(List<String> lines) {
        int oldMirrorPoint = mirrorPointsForPattern(lines).get(0);

        boolean[][] grid = new boolean[lines.size()][lines.get(0).length()];
        for (int y = 0; y < lines.size(); y++) {
            String line = lines.get(y);
            for (int x = 0; x < line.length(); x++) {
                grid[y][x] = line.charAt(x) == '#';
            }
        }

        for (int smudgeIndex = 0; smudgeIndex < grid.length * grid[0].length; smudgeIndex++) {
            int x = smudgeIndex % grid[0].length;
            int y = smudgeIndex / grid[0].length;
            grid[y][x] = !grid[y][x];

            var mirrorPoints = verticalMirrorPoints(grid).stream().map(i -> (i + 1) * 100)
                    .filter(mp -> mp != oldMirrorPoint)
                    .toList();
            if (!mirrorPoints.isEmpty() && !mirrorPoints.contains(oldMirrorPoint)) {
                return mirrorPoints;
            }
            mirrorPoints = horizontalMirrorPoints(grid).stream().map(i -> i + 1)
                    .filter(mp -> mp != oldMirrorPoint)
                    .toList();
            if (!mirrorPoints.isEmpty() && !mirrorPoints.contains(oldMirrorPoint)) {
                return mirrorPoints;
            }
            grid[y][x] = !grid[y][x];
        }
        throw new RuntimeException("No mirror points found for smudged pattern: " + lines);
    }

    private static List<Integer> verticalMirrorPoints(boolean[][] grid) {
        List<Integer> mirrorPoints = new ArrayList<>();

        //iterate over first column and find possible vertical mirror points
        for (int y = 0; y < grid.length - 1; y++) {
            var mirroredVertically = isMirroredVertically(grid, 0, y);
            if (mirroredVertically) {
                mirrorPoints.add(y);
            }
        }

        for (int x = 0; x < grid[0].length; x++) {
            // check all mirror points
            int i = 0;
            while (i < mirrorPoints.size()) {
                var mirroredVertically = isMirroredVertically(grid, x, mirrorPoints.get(i));
                if (!mirroredVertically) {
                    mirrorPoints.remove(i);
                    i--;
                }
                i++;
            }
        }
        return mirrorPoints;
    }

    private static List<Integer> horizontalMirrorPoints(boolean[][] grid) {
        List<Integer> mirrorPoints = new ArrayList<>();

        //iterate over first row and find possible horizontal mirror points
        for (int x = 0; x < grid[0].length - 1; x++) {
            var mirroredVertically = isMirroredHorizontally(grid, 0, x);
            if (mirroredVertically) {
                mirrorPoints.add(x);
            }
        }

        for (int y = 1; y < grid.length; y++) {
            // check all mirror points
            int i = 0;
            while (i < mirrorPoints.size()) {
                var mirroredVertically = isMirroredHorizontally(grid, y, mirrorPoints.get(i));
                if (!mirroredVertically) {
                    mirrorPoints.remove(i);
                    i--;
                }
                i++;
            }
        }
        return mirrorPoints;
    }

    private static boolean isMirroredVertically(boolean[][] grid, int col, int mirrorPointY) {
        final int maxDistance;
        if (mirrorPointY < grid.length / 2) {
            maxDistance = mirrorPointY + 1;
        } else {
            maxDistance = grid.length - 1 - mirrorPointY;
        }
        for (int y = 0; y < maxDistance; y++) {
            int top = mirrorPointY - y;
            int bottom = mirrorPointY + y + 1;
            if (grid[top][col] != grid[bottom][col]) {
                return false;
            }
        }
        return true;
    }

    private static boolean isMirroredHorizontally(boolean[][] grid, int row, int mirrorPointX) {
        final int maxDistance;
        if (mirrorPointX < grid[0].length / 2) {
            maxDistance = mirrorPointX + 1;
        } else {
            maxDistance = grid[0].length - 1 - mirrorPointX;
        }
        for (int x = 0; x < maxDistance; x++) {
            int left = mirrorPointX - x;
            int right = mirrorPointX + x + 1;
            if (grid[row][left] != grid[row][right]) {
                return false;
            }
        }
        return true;
    }

}
