import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class Day_15 {

    private static boolean secondStar = true;
    private static final String INPUT = "https://adventofcode.com/2023/day/15/input";

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

//        lines = List.of("rn=1,cm-,qp=3,cm=2,q\np-,pc=4,ot=9,ab=5,pc-,pc=6,ot=7").stream();

        var line = lines.map(l -> l.replaceAll("\n", "")).findFirst().orElseThrow();

        var steps = line.split(",");

        if (secondStar) {
            Map<Integer, Stack<Lens>> lenses = HashMap.newHashMap(256);
            for (int i = 0; i < 256; i++) {
                lenses.put(i, new Stack<>());
            }
            Arrays.stream(steps).forEach(s -> {
                if (s.endsWith("-")) {
                    var label = s.substring(0, s.length() - 1);
                    int hash = hashAOC(label);
                    var lensesForBox = lenses.get(hash);
                    lensesForBox.remove(new Lens(label, -1));
                } else {
                    var label = s.substring(0, s.length() - 2);
                    int hash = hashAOC(label);
                    var lensesForBox = lenses.get(hash);
                    var focalStrength = Integer.parseInt(s.substring(s.length() - 1));
                    var lens = new Lens(label, focalStrength);
                    var i = lensesForBox.indexOf(lens);
                    if (i != -1) {
                        lensesForBox.set(i, lens);
                    } else
                        lensesForBox.push(lens);
                }
            });
            long sum = 0;
            for (int boxNumber = 0; boxNumber < 256; boxNumber++) {
                var lensesForBox = lenses.get(boxNumber);
                System.out.println(lensesForBox);
                for (int slotNumber = 0; slotNumber < lensesForBox.size(); slotNumber++) {
                    var focalStrength = lensesForBox.get(slotNumber).focalStrength;
                    sum += focalStrength * (slotNumber + 1) * (boxNumber + 1);
                }
            }
            System.out.println(sum);
        } else
            Arrays.stream(steps).map(s -> hashAOC(s)).peek(System.out::println).reduce(Integer::sum).ifPresent(System.out::println);
    }

    //Determine the ASCII code for the current character of the string.
    //Increase the current value by the ASCII code you just determined.
    //Set the current value to itself multiplied by 17.
    //Set the current value to the remainder of dividing itself by 256.
    private static int hashAOC(String s) {
        return s.chars().reduce(0, (a, b) -> ((a + b) * 17) % 256);
    }

    static class Lens {
        String name;
        int focalStrength;

        public Lens(String name, int focalStrength) {
            this.name = name;
            this.focalStrength = focalStrength;
        }

        @Override
        public String toString() {
            return name + "=" + focalStrength;
        }

        public boolean equals(Object o) {
            if (o instanceof Lens) {
                return ((Lens) o).name.equals(name);
            }
            return false;
        }
    }

}
