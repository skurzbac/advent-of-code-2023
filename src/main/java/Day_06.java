import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class Day_06 {

    private static boolean secondStar = true;
    private static final String INPUT = "https://adventofcode.com/2023/day/6/input";

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
//                .thenApply(Stream::toList)
//                .join();

        var lines = Files.readString(Path.of("/Users/Stefan.Kurzbach/private-repos/advent-of-code-2023/src/main/java/Day_06.txt")).lines().toList();

        var durations = parse(lines.get(0));
        var recordDistances = parse(lines.get(1));
        long product = 1;
        for (int i = 0; i < durations.size(); i++) {
            //game i
            var duration = durations.get(i);
            var recordDistance = recordDistances.get(i);
            long winningTimesPushingButton = 0;
            for (long pushTime = 1; pushTime < duration; pushTime++) {
                var distance = distanceForTime(pushTime, duration);
                if (distance > recordDistance) {
                    winningTimesPushingButton++;
                }
            }
            System.out.println("winningTimesPushingButton = " + winningTimesPushingButton);
            product *= winningTimesPushingButton;
        }
        System.out.println("product = " + product);
    }

    private static long distanceForTime(long pushTime, long duration) {
        return (duration - pushTime) * pushTime;
    }

    private static List<Long> parse(String line) {
        return Arrays.stream(line.substring(line.indexOf(':') + 1).split("\\s+")).map(String::trim).filter(Predicate.not(String::isEmpty)).map(Long::parseLong).toList();
    }

}
