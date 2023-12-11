import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Day_10 {

    private static boolean secondStar = true;
    private static final String INPUT = "https://adventofcode.com/2023/day/10/input";

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

        var grid = Files.readString(Path.of("/Users/Stefan.Kurzbach/private-repos/advent-of-code-2023/src/main/java/Day_10.txt")).lines().toArray(String[]::new);

        var start = findStart(grid);
        var loop = findLoop(grid, start);
        System.out.println(Math.ceil(loop.size() / 2.0));

        if (secondStar) {
            // replace start with a pipe
            replaceStart(grid);

            var pipes = new ArrayList<>(loop.stream().map(s -> new Coordinate(s.x, s.y)).toList());
            pipes.add(new Coordinate(start.x - 1, start.y));
            int numberInside = 0;
            for (int i = 0; i < grid.length; i++) {
                String line = grid[i];
                // ray tracing
                int numberOfIntersections = 0;
                char lastIntersection = 0;
                for (int j = 0; j < line.length(); j++) {
                    boolean onPipe = lastIntersection != 0;
                    if (pipes.contains(new Coordinate(j, i))) {
                        var currentPipe = line.charAt(j);
                        if (!onPipe) {
                            if (currentPipe != '|') {
                                lastIntersection = currentPipe;
                            } else {
                                numberOfIntersections++;
                            }
                        }
                        if (onPipe) {
                            if (lastIntersection == 'F' && currentPipe == 'J' || lastIntersection == 'L' && currentPipe == '7') {
                                //no u-turn
                                lastIntersection = 0;
                                numberOfIntersections++;
                            } else if (currentPipe != '-') {
                                lastIntersection = 0;
                            }
                        }
                        System.out.print(prettyPrint(currentPipe));
                    } else {
                        if (numberOfIntersections % 2 == 1) {
                            numberInside++;
                            System.out.print("I");
                        } else {
                            System.out.print("O");
                        }
                    }
                }
                System.out.println();
            }
            System.out.println(numberInside);
        }
    }

    private static char prettyPrint(char currentPipe) {
        return switch (currentPipe) {
            case '7' -> '┐';
            case 'F' -> '┌';
            case 'J' -> '┘';
            case 'L' -> '└';
            case '|' -> '│';
            case '-' -> '─';
            default -> currentPipe;
        };
    }

    private static List<Step> findLoop(String[] grid, Step start) {
        Step next = start;
        List<Step> steps = new ArrayList<>();
        do {
            steps.add(next);
            Direction nextDirection = next.nextDirection;
            if (nextDirection == Direction.TOP) {
                next = stepTop(grid, next.x, next.y);
            } else if (nextDirection == Direction.RIGHT) {
                next = stepRight(grid, next.x, next.y);
            } else if (nextDirection == Direction.BOTTOM) {
                next = stepBottom(grid, next.x, next.y);
            } else if (nextDirection == Direction.LEFT) {
                next = stepLeft(grid, next.x, next.y);
            }
            if (next == null) {
                throw new RuntimeException("No next step");
            }
        } while (next.nextDirection != null);
        return steps;
    }


    private static Step findStart(String[] grid) {
        for (int y = 0; y < grid.length; y++) {
            String line = grid[y];
            for (int x = 0; x < line.length(); x++) {
                char c = line.charAt(x);
                if (c == 'S') {
                    return firstStep(grid, x, y);
                }
            }
        }
        throw new RuntimeException("No start found");
    }

    private static Step firstStep(String[] grid, int x, int y) {
        var stepTop = stepTop(grid, x, y);
        if (stepTop != null) {
            return stepTop;
        }

        var stepRight = stepRight(grid, x, y);
        if (stepRight != null) {
            return stepRight;
        }

        var stepBottom = stepBottom(grid, x, y);
        if (stepBottom != null) {
            return stepBottom;
        }

        var stepLeft = stepLeft(grid, x, y);
        if (stepLeft != null) {
            return stepLeft;
        }

        throw new RuntimeException("No valid step");
    }

    private static void replaceStart(String[] grid) {
        for (int y = 0; y < grid.length; y++) {
            String line = grid[y];
            for (int x = 0; x < line.length(); x++) {
                char c = line.charAt(x);
                if (c == 'S') {
                    var newC = replacementChar(grid, x, y);
                    grid[y] = grid[y].substring(0, x) + newC + grid[y].substring(x + 1);
                }
            }
        }
    }

    private static char replacementChar(String[] grid, int x, int y) {
        var stepTop = stepTop(grid, x, y);
        var stepRight = stepRight(grid, x, y);
        var stepBottom = stepBottom(grid, x, y);
        var stepLeft = stepLeft(grid, x, y);
        if (stepTop != null && stepRight != null) {
            return 'L';
        } else if (stepRight != null && stepBottom != null) {
            return 'F';
        } else if (stepBottom != null && stepLeft != null) {
            return '7';
        } else if (stepLeft != null && stepTop != null) {
            return 'J';
        } else if (stepTop != null && stepBottom != null) {
            return '|';
        } else if (stepLeft != null && stepRight != null) {
            return '-';
        }
        throw new RuntimeException("No valid start replacement");
    }

    private static Step stepLeft(String[] grid, int x, int y) {
        if (x == 0) {
            return null;
        }
        Direction nextDirection = Direction.LEFT;
        if (grid[y + nextDirection.dy].charAt(x + nextDirection.dx) == 'S') {
            return new Step(x + nextDirection.dx, y + nextDirection.dy, null);
        }
        if (grid[y + nextDirection.dy].charAt(x + nextDirection.dx) == 'F') {
            return new Step(x + nextDirection.dx, y + nextDirection.dy, Direction.BOTTOM);
        } else if (grid[y + nextDirection.dy].charAt(x + nextDirection.dx) == '-') {
            return new Step(x + nextDirection.dx, y + nextDirection.dy, Direction.LEFT);
        } else if (grid[y + nextDirection.dy].charAt(x + nextDirection.dx) == 'L') {
            return new Step(x + nextDirection.dx, y + nextDirection.dy, Direction.TOP);
        }
        return null;
    }

    private static Step stepBottom(String[] grid, int x, int y) {
        if (y == grid.length - 1) {
            return null;
        }
        Direction nextDirection = Direction.BOTTOM;
        if (grid[y + nextDirection.dy].charAt(x + nextDirection.dx) == 'S') {
            return new Step(x + nextDirection.dx, y + nextDirection.dy, null);
        }
        if (grid[y + nextDirection.dy].charAt(x + nextDirection.dx) == 'L') {
            return new Step(x + nextDirection.dx, y + nextDirection.dy, Direction.RIGHT);
        } else if (grid[y + nextDirection.dy].charAt(x + nextDirection.dx) == '|') {
            return new Step(x + nextDirection.dx, y + nextDirection.dy, Direction.BOTTOM);
        } else if (grid[y + nextDirection.dy].charAt(x + nextDirection.dx) == 'J') {
            return new Step(x + nextDirection.dx, y + nextDirection.dy, Direction.LEFT);
        }
        return null;
    }

    private static Step stepRight(String[] grid, int x, int y) {
        if (x == grid[y].length() - 1) {
            return null;
        }
        Direction nextDirection = Direction.RIGHT;
        if (grid[y + nextDirection.dy].charAt(x + nextDirection.dx) == 'S') {
            return new Step(x + nextDirection.dx, y + nextDirection.dy, null);
        }
        if (grid[y + nextDirection.dy].charAt(x + nextDirection.dx) == '7') {
            return new Step(x + nextDirection.dx, y + nextDirection.dy, Direction.BOTTOM);
        } else if (grid[y + nextDirection.dy].charAt(x + nextDirection.dx) == '-') {
            return new Step(x + nextDirection.dx, y + nextDirection.dy, Direction.RIGHT);
        } else if (grid[y + nextDirection.dy].charAt(x + nextDirection.dx) == 'J') {
            return new Step(x + nextDirection.dx, y + nextDirection.dy, Direction.TOP);
        }
        return null;
    }

    private static Step stepTop(String[] grid, int x, int y) {
        if (y == 0) {
            return null;
        }
        var nextDirection = Direction.TOP;
        if (grid[y + nextDirection.dy].charAt(x + nextDirection.dx) == 'S') {
            return new Step(x + nextDirection.dx, y + nextDirection.dy, null);
        }
        if (grid[y + nextDirection.dy].charAt(x + nextDirection.dx) == '7') {
            return new Step(x + nextDirection.dx, y + nextDirection.dy, Direction.LEFT);
        } else if (grid[y + nextDirection.dy].charAt(x + nextDirection.dx) == '|') {
            return new Step(x + nextDirection.dx, y + nextDirection.dy, Direction.TOP);
        } else if (grid[y + nextDirection.dy].charAt(x + nextDirection.dx) == 'F') {
            return new Step(x + nextDirection.dx, y + nextDirection.dy, Direction.RIGHT);
        }
        return null;
    }

    enum Direction {
        TOP(0, -1), RIGHT(1, 0), BOTTOM(0, 1), LEFT(-1, 0);

        private final int dx;
        private final int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
    }

    record Step(int x, int y, Direction nextDirection) {
    }

    record Coordinate(int x, int y) {
    }
}
