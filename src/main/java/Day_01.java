import java.net.*;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.regex.Pattern;

import static java.net.http.HttpClient.newBuilder;

public class Day_01 {

    private static boolean secondStar = false;

    private static final Pattern PATTERN_FIRST = Pattern.compile("(?=(\\d)).");
    private static final Pattern PATTERN_SECOND = Pattern.compile("(?=(\\d|one|two|three|four|five|six|seven|eight|nine)).");
    private static final String INPUT = "https://adventofcode.com/2023/day/1/input";

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
                .thenApply(lines -> lines.peek(System.out::print).mapToInt(Day_01::calibrationValue).peek(System.out::println).sum())
                .thenAccept(System.out::println)
                .join();
    }

    private static Integer calibrationValue(String s) {
        //parse using regex
        if (secondStar) {
            return fromMatch(PATTERN_SECOND.matcher(s).results().map(matchResult -> matchResult.group(1)).map(Day_01::parseMatch).toList());
        } else {
            return fromMatch(PATTERN_FIRST.matcher(s).results().map(matchResult -> matchResult.group(1)).map(Day_01::parseMatch).toList());
        }
    }

    private static Integer parseMatch(String s) {
        return switch (s) {
            case "first" -> 1;
            case "two" -> 2;
            case "three" -> 3;
            case "four" -> 4;
            case "five" -> 5;
            case "six" -> 6;
            case "seven" -> 7;
            case "eight" -> 8;
            case "nine" -> 9;
            default -> Integer.parseInt(s);
        };
    }

    private static Integer fromMatch(List<Integer> matches) {
        var firstMatch = matches.get(0);
        var lastMatch = matches.get(matches.size() - 1);
        return firstMatch * 10 + lastMatch;
    }
}
