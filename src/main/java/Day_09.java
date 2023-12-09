import one.util.streamex.LongStreamEx;
import one.util.streamex.StreamEx;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class Day_09 {

    private static boolean secondStar = true;
    private static final String INPUT = "https://adventofcode.com/2023/day/9/input";

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

//        var lines = Files.readString(Path.of("/Users/Stefan.Kurzbach/private-repos/advent-of-code-2023/src/main/java/Day_09.txt")).lines();

        if (secondStar) {
            var sum = lines.peek(s -> System.out.print(s + " (first) ")).map(line -> StreamEx.of(line.split("\\s+")).mapToLong(Long::parseLong)) //stream of long streams (sequences)
                    .map(differenceStreamSubtractingFirstValue()).peek(System.out::println).reduce(Long::sum).orElseThrow();
            System.out.println("sum = " + sum);

        } else {
            var sum = lines.peek(s -> System.out.print(s + " ")).map(line -> StreamEx.of(line.split("\\s+")).mapToLong(Long::parseLong)) //stream of long streams (sequences)
                    .map(differenceStreamSummingLastValue()).peek(System.out::println).reduce(Long::sum).orElseThrow();
            System.out.println("sum = " + sum);
        }
    }


    private static Function<LongStreamEx, Long> differenceStreamSummingLastValue() {
        return sequence -> {
            AtomicLong lastValue = new AtomicLong();
            AtomicBoolean allZero = new AtomicBoolean(true);
            var differences = sequence.peek(value -> {
                if (value != 0) allZero.set(false);
            }).peek(lastValue::set).pairMap((left, right) -> right - left).toArray();
            if (allZero.get()) {
                return 0L;
            } else {
                return lastValue.get() + differenceStreamSummingLastValue().apply(LongStreamEx.of(differences));
            }
        };
    }

    private static Function<LongStreamEx, Long> differenceStreamSubtractingFirstValue() {
        return sequence -> {
            AtomicLong firstValue = new AtomicLong(Long.MAX_VALUE);
            AtomicBoolean allZero = new AtomicBoolean(true);
            var differences = sequence.peek(value -> {
                if (value != 0) allZero.set(false);
            }).peek(newValue -> {
                if (firstValue.longValue() == Long.MAX_VALUE) {
                    firstValue.set(newValue);
                }
            }).pairMap((left, right) -> right - left).toArray();
            if (allZero.get()) {
                return 0L;
            } else {
                return firstValue.get() - differenceStreamSubtractingFirstValue().apply(LongStreamEx.of(differences));
            }
        };
    }
}
