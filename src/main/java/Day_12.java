import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day_12 {

    private static boolean secondStar = true;
    private static final String INPUT = "https://adventofcode.com/2023/day/12/input";

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

        var lines = Files.readString(Path.of("/Users/Stefan.Kurzbach/private-repos/advent-of-code-2023/src/main/java/Day_12.txt")).lines();
        if(!secondStar) {
            var sum = SpringsReader.read(lines)
                    .map(Springs::arrangementCount)
                    .peek(System.out::println)
                    .reduce(Integer::sum).orElseThrow();
            System.out.println(sum);
        } else {
            var sum = SpringsReader.read(lines)
                    .map(Springs::unfold)
                    .map(Springs::arrangementCount)
                    .peek(System.out::println)
                    .parallel()
                    .reduce(Integer::sum).orElseThrow();
            System.out.println(sum);
        }
    }


    class SpringsReader {

        static Stream<Springs> read(Stream<String> lines) {
            return lines
                    .map(SpringsReader::parse);
        }

        private static Springs parse(String s) {
            //#.#.### 1,1,3
            var parts = s.split(" ");
            var row = parts[0].chars()
                    .mapToObj(c -> Spring.fromChar((char) c))
                    .toArray(Spring[]::new);
            var groups = Arrays.stream(parts[1].split(","))
                    .mapToInt(Integer::parseInt)
                    .toArray();
            var meta = new Meta(groups);
            return new Springs(row, meta);
        }
    }

    record Meta(int[] groups) {
    }

    enum Spring {
        OPERATIONAL,
        DAMAGED,
        UNKNOWN;

        public static Spring fromChar(char c) {
            switch (c) {
                case '.':
                    return OPERATIONAL;
                case '#':
                    return DAMAGED;
                default:
                    return UNKNOWN;
            }
        }
    }

    record Springs(Spring[] row, Meta meta) {
        int arrangementCount() {
            //no unknowns
            if (Arrays.stream(row).noneMatch(s -> s == Spring.UNKNOWN)) {
                return 1;
            }
            return Day_12.bruteForceArrangementCount(this);
        }

        int numberOfUnknowns() {
            return (int) Arrays.stream(row).filter(s -> s == Spring.UNKNOWN).count();
        }

        int expectedNumberOfGroups() {
            return meta.groups.length;
        }

        boolean matchesMeta() {
            int numberOfGroups = 0;
            for (int i = 0; i < row.length; i++) {
                if (row[i] == Spring.DAMAGED) {
                    if (numberOfGroups == meta.groups.length) {
                        return false;
                    }
                    int group = meta.groups[numberOfGroups++];
                    if (i + group - 1 >= row.length) {
                        return false;
                    }
                    for (int j = 1; j < group; j++) {
                        if (row[++i] != Spring.DAMAGED) {
                            return false;
                        }
                    }
                    if (i < row.length - 1 && row[++i] == Spring.DAMAGED) {
                        return false;
                    }
                }
            }
            return numberOfGroups == meta.groups.length;
        }

        public Springs withUnknownsReplacedBy(Spring[] guess) {
            var newRow = new Spring[row.length];
            int currentUnknown = 0;
            for (int i = 0; i < row.length; i++) {
                if (row[i] == Spring.UNKNOWN) {
                    newRow[i] = guess[currentUnknown++];
                } else {
                    newRow[i] = row[i];
                }
            }
            return new Springs(newRow, meta);
        }

        public Springs unfold() {
            List<Spring> newRow = new ArrayList<>(row.length * 5 + 4);
            var groups = IntStream.builder();
            for (int i = 0; i< 4;i++) {
                newRow.addAll(Arrays.asList(row));
                newRow.add(Spring.UNKNOWN);
                for (int j = 0; j < meta.groups.length; j++) {
                    groups.add(meta.groups[j]);
                }
            }
            for (int j = 0; j < meta.groups.length; j++) {
                groups.add(meta.groups[j]);
            }
            newRow.addAll(Arrays.asList(row));
            return new Springs(newRow.toArray(Spring[]::new), new Meta(groups.build().toArray()));
        }
    }

    private static int bruteForceArrangementCount(Springs springs) {
        var numberOfUnknowns = springs.numberOfUnknowns();
        Spring[] guess = new Spring[numberOfUnknowns];
        int numberOfCombinations = 1 << numberOfUnknowns;
        int numberOfMatches = 0;
        for (int i = 0; i < numberOfCombinations; i++) {
            generateGuess(guess, i);
            var matches = springs.withUnknownsReplacedBy(guess).matchesMeta();
            if (matches) {
                numberOfMatches++;
            }
        }
        return numberOfMatches;
    }

    private static void generateGuess(Spring[] guess, int currentGuess) {
        // 000 -> [OPERATIONAL, OPERATIONAL, OPERATIONAL]
        // 001 -> [OPERATIONAL, OPERATIONAL, DAMAGED]
        for (int i = 0; i < guess.length; i++) {
            // bitmask to array
            guess[i] = (currentGuess & (1 << i)) == 0 ? Spring.OPERATIONAL : Spring.DAMAGED;
        }
    }
}
