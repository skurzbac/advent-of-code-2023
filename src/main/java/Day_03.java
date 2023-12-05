import java.io.IOException;
import java.net.*;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.net.http.HttpClient.newBuilder;

public class Day_03 {

    private static boolean secondStar = true;
    private static final String INPUT = "https://adventofcode.com/2023/day/3/input";

    public static void main(String[] args) throws URISyntaxException, IOException {
        CookieHandler.setDefault(new CookieManager());

        HttpCookie sessionCookie = new HttpCookie("session", SessionCookie.SESSION_VALUE);
        sessionCookie.setPath("/");
        sessionCookie.setVersion(0);

        ((CookieManager) CookieHandler.getDefault()).getCookieStore().add(new URI("https://adventofcode.com"),
                sessionCookie);

        var stringList = newBuilder()
                .cookieHandler(CookieHandler.getDefault())
                .build()
                .sendAsync(HttpRequest.newBuilder(URI.create(INPUT)).build(),
                        HttpResponse.BodyHandlers.ofLines())
                .thenApply(HttpResponse::body)
                .thenApply(Stream::toList)
                .join();

        //stringList = Files.readString(Path.of("/Users/Stefan.Kurzbach/private-repos/advent-of-code-2023/src/main/java/Day_03.txt")).lines().toList();

        int x = stringList.get(0).length();
        int y = stringList.size();

        int[][] map = new int[y][x];
        for (int i = 0; i < y; i++) {
            String line = stringList.get(i);
            for (int j = 0; j < x; j++) {
                var c = line.charAt(j);
                boolean isDigit = Character.isDigit(c);
                boolean isDot = c == '.';
                boolean isStar = c == '*';
                if (isDigit) {
                    map[i][j] = Character.getNumericValue(c);
                } else {
                    if (isDot) map[i][j] = -1;
                    else if (secondStar && isStar) map[i][j] = -2;
                    else map[i][j] = -2;
                }
            }
        }

        int sumOfParts = 0;
        Map<Long, Integer> gearNumbers = new HashMap<>();
        Map<Long, Integer> gearRatios = new HashMap<>();
        for (int i = 0; i < y; i++) {
            int start = -1;
            for (int j = 0; j < x; j++) {
                if (map[i][j] > 0 && start == -1) {
                    start = j;
                } else if ((map[i][j] < 0 || j == x - 1) && start != -1) {
                    int partNumber = 0;
                    int range = j - start;
                    if (j == x - 1 && map[i][j] >= 0) range++;
                    for (int k = 0; k < range; k++) {
                        partNumber += (map[i][start + k] * (int) Math.pow(10, range - k - 1));
                    }
                    if (!secondStar && isEnginePart(map, i, start, range)) {
                        sumOfParts += partNumber;
                        System.out.println(partNumber);
                    } else if (secondStar && isEnginePart(map, i, start, range)) {
                        long gearNumber = getGearNumber(map, i, start, range);
                        System.out.println(gearNumber + " --> " + partNumber);
                        if (gearNumbers.containsKey(gearNumber)) {
                            gearRatios.put(gearNumber, gearNumbers.get(gearNumber) * partNumber);
                        } else {
                            gearNumbers.put(gearNumber, partNumber);
                        }
                    }
                    start = -1;
                }

            }
        }
        if (secondStar) {
            System.out.println(gearRatios.values().stream().reduce(Integer::sum).orElse(0));
        } else
            System.out.println(sumOfParts);
    }

    private static boolean isEnginePart(int[][] map, int i, int start, int range) {
        return getGearNumber(map, i, start, range) != -1;
    }

    private static long getGearNumber(int[][] map, int i, int start, int range) {
        for (int j = -1; j < range + 1; j++) {
            if (i - 1 >= 0 && start + j < map[i].length && start + j >= 0 && map[i - 1][start + j] == -2) {
                return gearNumberAt(i - 1, start + j);
            }
            if (start + j < map[i].length && start + j >= 0 && map[i][start + j] == -2) {
                return gearNumberAt(i, start + j);
            }
            if (i + 1 < map.length && start + j < map[i].length && start + j >= 0 && map[i + 1][start + j] == -2) {
                return gearNumberAt(i + 1, start + j);
            }
        }
        return -1;
    }

    private static long gearNumberAt(int i, int j) {
        // hash of i and j
        return i * 10000L + j;
    }
}
