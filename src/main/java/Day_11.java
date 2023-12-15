import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Day_11 {

    private static boolean secondStar = true;
    private static final String INPUT = "https://adventofcode.com/2023/day/11/input";

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

        var lines = Files.readString(Path.of("/Users/Stefan.Kurzbach/private-repos/advent-of-code-2023/src/main/java/Day_11.txt")).lines().toList();

        //find empty rows and columns
        boolean[] emptyRow = new boolean[lines.size()];
        boolean[] emptyColumn = new boolean[lines.get(0).length()];
        Arrays.fill(emptyColumn, true);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.matches("\\.+")) {
                emptyRow[i] = true;
            }
            for (int j = 0; j < line.length(); j++) {
                if (line.charAt(j) != '.') {
                    emptyColumn[j] = false;
                }
            }
        }

        long[] numberOfEmptyRows = new long[lines.size()];
        long currentNumberOfEmptyRows = 0;
        var offset = secondStar ? 999999 : 1;
        for (int i = 0; i < emptyRow.length; i++) {
            if (emptyRow[i]) {
                currentNumberOfEmptyRows = currentNumberOfEmptyRows + offset;
            }
            numberOfEmptyRows[i] = currentNumberOfEmptyRows;
        }

        long[] numberOfEmptyColumns = new long[lines.get(0).length()];
        long currentNumberOfEmptyColumns = 0;
        for (int j = 0; j < emptyColumn.length; j++) {
            if (emptyColumn[j]) {
                currentNumberOfEmptyColumns = currentNumberOfEmptyColumns + offset;
            }
            numberOfEmptyColumns[j] = currentNumberOfEmptyColumns;
        }

        //read coordinates of galaxies, adding space between them if a row or column is empty
        List<Coordinate> coordinates = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            for (int j = 0; j < line.length(); j++) {
                if (line.charAt(j) == '#') {
                    coordinates.add(new Coordinate(j + numberOfEmptyColumns[j], i + numberOfEmptyRows[i]));
                }
            }
        }

        long sum = 0;
        for (int i = 0; i < coordinates.size(); i++) {
            Coordinate first = coordinates.get(i);
            for (int j = i + 1; j < coordinates.size(); j++) {
                Coordinate second = coordinates.get(j);
                //calculate distance
                long xDistance = Math.abs(first.x - second.x);
                long yDistance = Math.abs(first.y - second.y);
                long distance = xDistance + yDistance;
                sum += distance;
            }
        }
        System.out.println(sum);
    }

    record Coordinate(long x, long y) {
    }
}
