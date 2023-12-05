import java.net.*;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import static java.net.http.HttpClient.newBuilder;

public class Day_02 {

    private static boolean secondStar = true;
    private static final String INPUT = "https://adventofcode.com/2023/day/2/input";

    private static final int MAX_RED = 12;
    private static final int MAX_GREEN = 13;
    private static final int MAX_BLUE = 14;

    public static void main(String[] args) throws URISyntaxException {
        CookieHandler.setDefault(new CookieManager());

        HttpCookie sessionCookie = new HttpCookie("session", SessionCookie.SESSION_VALUE);
        sessionCookie.setPath("/");
        sessionCookie.setVersion(0);

        ((CookieManager) CookieHandler.getDefault()).getCookieStore().add(new URI("https://adventofcode.com"),
                sessionCookie);

        newBuilder()
                .cookieHandler(CookieHandler.getDefault())
                .build()
                .sendAsync(HttpRequest.newBuilder(URI.create(INPUT)).build(),
                        HttpResponse.BodyHandlers.ofLines())
                .thenApply(HttpResponse::body)
                .thenApply(lines -> lines.peek(line -> System.out.print(line + " --> ")).mapToInt(Day_02::gamePower).peek(System.out::println).sum())
                .thenAccept(System.out::println)
                .join();
    }

    private static Integer gamePower(String game) {
        //e.g. game = Game 15: 1 blue, 6 green, 14 red; 3 red, 1 blue, 6 green; 4 green; 1 blue, 5 green, 2 red; 2 blue, 1 green, 6 red; 4 red, 8 green, 1 blue
        var endIndex = game.indexOf(":");
        int gameId = Integer.parseInt(game.substring(5, endIndex));
        String roundsString = game.substring(endIndex + 2);
        List<Round> rounds = Arrays.stream(roundsString.split("; ")).map(Day_02::parseRound).toList();
        if (secondStar) {
            Round maxRound = rounds.stream().reduce(new Round(0, 0, 0), (round1, round2) -> new Round(Math.max(round1.blue(), round2.blue()), Math.max(round1.green(), round2.green()), Math.max(round1.red(), round2.red())));
            return maxRound.blue * maxRound.green * maxRound.red;
        } else {
            boolean impossibleGame = rounds.stream().anyMatch(round -> round.red() > MAX_RED || round.green() > MAX_GREEN || round.blue() > MAX_BLUE);
            if (impossibleGame) {
                return 0;
            }
            return gameId;
        }
    }

    private static Round parseRound(String round) {
        var colors = round.split(", ");
        int red = 0;
        int green = 0;
        int blue = 0;
        for (String colorString : colors) {
            int count = Integer.parseInt(colorString.split(" ")[0]);
            String color = colorString.split(" ")[1];
            switch (color) {
                case "red" -> red = count;
                case "green" -> green = count;
                case "blue" -> blue = count;
            }
        }
        return new Round(blue, green, red);
    }

    record Round(int blue, int green, int red) {
    }
}
