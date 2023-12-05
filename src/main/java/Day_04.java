import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class Day_04 {

    private static boolean secondStar = true;
    private static final String INPUT = "https://adventofcode.com/2023/day/4/input";

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

        //var lines = Files.readString(Path.of("/Users/Stefan.Kurzbach/private-repos/advent-of-code-2023/src/main/java/Day_04.txt")).lines();

        if (!secondStar) {
            var sum = lines.map(Day_04::parseCard).mapToInt(Day_04::points).sum();
            System.out.println("sum = " + sum);
        } else {
            var wins = lines.map(Day_04::parseCard).map(Day_04::wins).toList();
            var numberOfCards = new ArrayList<Integer>(wins.size());
            IntStream.range(0, wins.size()).forEach(i -> numberOfCards.add(1));
            for (int i = 0; i < wins.size(); i++) {
                var win = wins.get(i);
                var copies = numberOfCards.get(i);
                for (int j = i + 1; j < i + win + 1; j++) {
                    numberOfCards.set(j, numberOfCards.get(j) + copies);
                }
            }
            System.out.println("numberOfCards = " + numberOfCards.stream().mapToInt(Integer::intValue).sum());
        }
    }

    private static int points(Card card) {
        var points = 0;
        for (var i = 0; i < card.winningNumbers().size(); i++) {
            var winningNumber = card.winningNumbers().get(i);
            if (card.myNumbers().contains(winningNumber)) {
                if (points == 0)
                    points = 1;
                else
                    points *= 2;
            }
        }
        return points;
    }

    private static int wins(Card card) {
        var wins = 0;
        for (var i = 0; i < card.winningNumbers().size(); i++) {
            var winningNumber = card.winningNumbers().get(i);
            if (card.myNumbers().contains(winningNumber)) {
                wins++;
            }
        }
        return wins;
    }

    private static Card parseCard(String line) {
        //Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53
        var winningNumbers = Arrays.stream(line.substring(line.indexOf(':') + 1, line.indexOf('|')).split("\\s+")).map(String::trim).filter(Predicate.not(String::isEmpty)).map(Integer::parseInt).toList();
        var myNumbers = Arrays.stream(line.substring(line.indexOf('|') + 1).split("\\s+")).map(String::trim).filter(Predicate.not(String::isEmpty)).map(Integer::parseInt).toList();
        return new Card(winningNumbers, myNumbers);
    }

    record Card(List<Integer> winningNumbers, List<Integer> myNumbers) {

    }
}
