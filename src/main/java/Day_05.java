import java.net.*;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.net.http.HttpClient.newBuilder;

public class Day_05 {

    private static boolean secondStar = true;
    private static final String INPUT = "https://adventofcode.com/2023/day/5/input";

    public static void main(String[] args) throws URISyntaxException {
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

//        stringList = Files.readString(Path.of("/Users/Stefan.Kurzbach/private-repos/advent-of-code-2023/src/main/java/Day_05.txt")).lines().toList();

        AtomicInteger currentLine = new AtomicInteger(0);

        //read line "seeds: 79 14 55 13"
        var seeds = Arrays.stream(stringList.get(currentLine.get()).substring(7).split(" ")).mapToLong(Long::parseLong).toArray();

        currentLine.addAndGet(3);
        var seedToSoil = nonManifestedLookup(stringList, currentLine);
        currentLine.addAndGet(2);
        var soilToFertilizer = nonManifestedLookup(stringList, currentLine);
        currentLine.addAndGet(2);
        var fertilizerToWater = nonManifestedLookup(stringList, currentLine);
        currentLine.addAndGet(2);
        var waterToLight = nonManifestedLookup(stringList, currentLine);
        currentLine.addAndGet(2);
        var lightToTemperature = nonManifestedLookup(stringList, currentLine);
        currentLine.addAndGet(2);
        var temperatureToHumidity = nonManifestedLookup(stringList, currentLine);
        currentLine.addAndGet(2);
        var humidityToLocation = nonManifestedLookup(stringList, currentLine);
        System.out.println(humidityToLocation);

        Map<Long, Long> seedToLocation = new HashMap<>();
        if (secondStar) {
            var result = IntStream.range(0, seeds.length / 2).parallel()
                    .mapToObj(i -> new Pair(seeds[i * 2], seeds[i * 2] + seeds[i * 2 + 1]))
                    .flatMapToLong(pair -> LongStream.range(pair.first, pair.second))
                    .mapToObj(seed -> lookupSeed(seedToSoil, seed, soilToFertilizer, fertilizerToWater, waterToLight, lightToTemperature, temperatureToHumidity, humidityToLocation))
                    .min(Comparator.comparingLong(Pair::second))
                    .get();
            System.out.println(result.second);
        } else {
            for (int i = 0; i < seeds.length; i++) {
                var seed = seeds[i];
                var result = lookupSeed(seedToSoil, seed, soilToFertilizer, fertilizerToWater, waterToLight, lightToTemperature, temperatureToHumidity, humidityToLocation);
                seedToLocation.put(seed, result.second);
            }
            long minLocation = seedToLocation.values().stream().min(Long::compareTo).get();
            long minSeed = seedToLocation.entrySet().stream().filter(entry -> entry.getValue() == minLocation).map(Map.Entry::getValue).min(Long::compareTo).get();
            System.out.println(minSeed);
        }
    }

    record Pair(long first, long second) {
    }

    private static Pair lookupSeed(Function<Long, Long> seedToSoil, long seed, Function<Long, Long> soilToFertilizer, Function<Long, Long> fertilizerToWater, Function<Long, Long> waterToLight, Function<Long, Long> lightToTemperature, Function<Long, Long> temperatureToHumidity, Function<Long, Long> humidityToLocation) {
        var soil = seedToSoil.apply(seed);
        var fertilizer = soilToFertilizer.apply(soil);
        var water = fertilizerToWater.apply(fertilizer);
        var light = waterToLight.apply(water);
        var temperature = lightToTemperature.apply(light);
        var humidity = temperatureToHumidity.apply(temperature);
        var location = humidityToLocation.apply(humidity);
        return new Pair(seed, location);
    }

    private static Function<Long, Long> nonManifestedLookup(List<String> stringList, AtomicInteger currentLine) {
        List<Range> ranges = new ArrayList<>();
        while (currentLine.get() < stringList.size() && !stringList.get(currentLine.get()).isEmpty()) {
            var line = stringList.get(currentLine.get());
            var destSourceRange = Arrays.stream(line.split(" ")).mapToLong(Long::parseLong).toArray();
            ranges.add(new Range(destSourceRange[0], destSourceRange[1], destSourceRange[2]));
            currentLine.incrementAndGet();
        }
        ranges.sort(Comparator.comparingLong(Range::source));

        return key -> {
            for (Range range : ranges) {
                var inRange = range.inRange(key);
                if (inRange) {
                    return range.dest + (key - range.source);
                }
            }
            return key;
        };
    }

    record Range(long dest, long source, long range) {

        public boolean inRange(Long key) {
            return key >= source && key < source + range;
        }
    }

}
