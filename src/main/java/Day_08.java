import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Day_08 {

    private static boolean secondStar = true;
    private static final String INPUT = "https://adventofcode.com/2023/day/8/input";

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
                .thenApply(Stream::toList)
                .join();

        //var lines = Files.readString(Path.of("/Users/Stefan.Kurzbach/private-repos/advent-of-code-2023/src/main/java/Day_08.txt")).lines().toList();
        var instructions = lines.get(0).codePoints().map(c -> 'R' == c ? 1 : 0).toArray();
        Map<String, String> leftMap = new HashMap<>();
        Map<String, String> rightMap = new HashMap<>();
        for (int i = 0; i < lines.size(); i++) {
            if (i < 2) continue;
            String line = lines.get(i);
            var node = line.substring(0, line.indexOf('=')).trim();
            var left = line.substring(line.indexOf('(') + 1, line.indexOf(',')).trim();
            var right = line.substring(line.indexOf(',') + 1, line.indexOf(')')).trim();
            leftMap.put(node, left);
            rightMap.put(node, right);
        }
        if (secondStar) {
            List<String> currentNodes = leftMap.keySet().stream().filter(s -> s.endsWith("A")).toList();
            System.out.println("currentNodes = " + currentNodes);
            int i = 0;
            List<Integer> cycles = new ArrayList<>(currentNodes.size());
            while (!currentNodes.isEmpty()) {
                var instruction = instructions[i % instructions.length];
                if (instruction == 0) {
                    //left
                    currentNodes = currentNodes.stream().map(leftMap::get).toList();
                } else {
                    //right
                    currentNodes = currentNodes.stream().map(rightMap::get).toList();
                }
                i++;
                var done = currentNodes.stream().filter(s -> s.endsWith("Z")).toList();
                if (!done.isEmpty()) {
                    cycles.add(i);
                    System.out.println("done = " + done);
                    System.out.println("cycles = " + cycles);
                    currentNodes = new ArrayList<>(currentNodes);
                    currentNodes.removeAll(done);
                }
            }
            System.out.println("cycles = " + cycles);
            //calculate lcm of cycles
            long lcm = cycles.get(0);
            for (int j = 1; j < cycles.size(); j++) {
                lcm = lcm(lcm, cycles.get(j));
            }
            System.out.println("lcm = " + lcm);
            System.out.println("numberOfSteps = " + i);
        } else {
            String currentNode = "AAA";
            int i = 0;
            while (!currentNode.equals("ZZZ")) {
                var instruction = instructions[(i) % (instructions.length)];
                if (instruction == 0) {
                    //left
                    currentNode = leftMap.get(currentNode);
                } else {
                    //right
                    currentNode = rightMap.get(currentNode);
                }
                System.out.println(currentNode);
                i++;
            }
            System.out.println("numberOfSteps = " + i);
        }
    }

    private static long lcm(long lcm, long integer) {
        return lcm * integer / gcd(lcm, integer);
    }

    private static long gcd(long lcm, long integer) {
        return lcm == 0 ? integer : gcd(integer % lcm, lcm);
    }

}
