import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Day_07 {

    private static boolean secondStar = true;
    private static final String INPUT = "https://adventofcode.com/2023/day/7/input";

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
                .join();

//        lines = Files.readString(Path.of("/Users/Stefan.Kurzbach/private-repos/advent-of-code-2023/src/main/java/Day_07.txt")).lines();

        List<Character> ranks = secondStar ? List.of('A', 'K', 'Q', 'T', '9', '8', '7', '6', '5', '4', '3', '2', 'J') : List.of('A', 'K', 'Q', 'J', 'T', '9', '8', '7', '6', '5', '4', '3', '2');

        //32T3K 765
        AtomicInteger counter = new AtomicInteger(0);
        var sum = lines.map(line -> {
            var originalHand = line.substring(0, 5);
            var bid = Integer.parseInt(line.substring(6));
            var type = secondStar ? typeWithJoker(originalHand) : type(originalHand);
            return new Hand(originalHand, bid, type);
        }).sorted(Comparator.comparingInt(Hand::type).thenComparing(Hand::cards, (a, b) -> {
            for (int i = 0; i < a.length(); i++) {
                var aRank = a.charAt(i);
                var bRank = b.charAt(i);
                if (aRank != bRank) {
                    return ranks.indexOf(bRank) - ranks.indexOf(aRank);
                }
            }
            return 0;
        })).map(hand -> {
            var rank = counter.incrementAndGet();
            System.out.println(rank + " --> " + hand);
            return rank * hand.bid;
        }).reduce(Integer::sum).orElseThrow();
        System.out.println("sum = " + sum);

    }

    private static int typeWithJoker(String originalHand) {
        var histogram = originalHand.codePoints().boxed().collect(Collectors.groupingBy(c -> c, Collectors.counting()));
        int numberOfDifferentRanks = histogram.size();
        int numberOfPairs = (int) histogram.values().stream().filter(v -> v == 2).count();
        int numberOfThrees = (int) histogram.values().stream().filter(v -> v == 3).count();
        int numberOfFours = (int) histogram.values().stream().filter(v -> v == 4).count();
        int numberOfJokers = histogram.getOrDefault((int) 'J', 0L).intValue();
        final int type;
        if (numberOfDifferentRanks == 1) {
            type = 7;
        } else if (numberOfFours == 1 && numberOfJokers >= 1) {
            type = 7;
        } else if (numberOfFours == 1) {
            type = 6;
        } else if (numberOfThrees == 1 && numberOfPairs == 1 && numberOfJokers >= 2) {
            type = 7;
        } else if (numberOfThrees == 1 && numberOfPairs == 1) {
            type = 5;
        } else if (numberOfThrees == 1 && numberOfJokers >= 1) {
            type = 6;
        } else if (numberOfThrees == 1) {
            type = 4;
        } else if (numberOfPairs == 2 && numberOfJokers == 2) {
            type = 6;
        } else if (numberOfPairs == 2 && numberOfJokers == 1) {
            type = 5;
        } else if (numberOfPairs == 2) {
            type = 3;
        } else if (numberOfPairs == 1 && numberOfJokers >= 1) {
            type = 4;
        } else if (numberOfPairs == 1) {
            type = 2;
        } else if (numberOfJokers == 1) {
            type = 2;
        } else {
            type = 1;
        }

        return type;
    }

    private static int type(String originalHand) {
        var histogram = originalHand.codePoints().boxed().collect(Collectors.groupingBy(c -> c, Collectors.counting()));
        int numberOfDifferentRanks = histogram.size();
        int numberOfPairs = (int) histogram.values().stream().filter(v -> v == 2).count();
        int numberOfThrees = (int) histogram.values().stream().filter(v -> v == 3).count();
        int numberOfFours = (int) histogram.values().stream().filter(v -> v == 4).count();
        final int type;
        if (numberOfDifferentRanks == 1) {
            type = 7;
        } else if (numberOfFours == 1) {
            type = 6;
        } else if (numberOfThrees == 1 && numberOfPairs == 1) {
            type = 5;
        } else if (numberOfThrees == 1) {
            type = 4;
        } else if (numberOfPairs == 2) {
            type = 3;
        } else if (numberOfPairs == 1) {
            type = 2;
        } else {
            type = 1;
        }
        return type;
    }

    record Hand(String cards, int bid, int type) {
    }
}
