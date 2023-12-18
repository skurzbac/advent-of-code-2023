import java.nio.file.Path
import java.util.*
import kotlin.io.path.readLines
import kotlin.math.abs

fun main() {
    Day_18.main()
}

typealias ColorCode = String

object Day_18 {
    val secondStar = true

    val moves =
        Path.of("/Users/Stefan.Kurzbach/private-repos/advent-of-code-2023/src/main/java/Day_18.txt").readLines()
            .map { parseLine(it) };

    private fun parseLine(line: String): Move {
        //R 6 (#70c710)
        val parts = line.split(" ")
        val dir = Dir.valueOf(parts[0])
        val steps = parts[1].toInt()
        val colorCode = parts[2].substring(1, 8)
        if (secondStar) {
            return parseColorCode(colorCode)
        }
        return Move(dir, steps, colorCode)
    }

    private fun parseColorCode(colorCode: String): Move {
        val steps = colorCode.substring(1, 6).toInt(16)
        val dir = Dir.entries[colorCode.substring(6, 7).toInt(16)]
        return Move(dir, steps, "")
    }

    fun main() {
        var start = Coord(0, 0)
        val visited = mutableListOf<Coord>()
        var current = start
        visited.add(current)
        for (move in moves) {
            current = current.move(move)
            visited.add(current)
        }

        if (secondStar) {
            val area = trapezoidArea(visited)
            val circumferenceArea = moves.map { it.steps }.sum() / 2
            println("area + circumference area: ${area + circumferenceArea + 1}")
        }
        if (!secondStar) {
            val min = Coord(visited.map { it.x }.min(), visited.map { it.y }.min())
            val max = Coord(visited.map { it.x }.max(), visited.map { it.y }.max())
            val size = Coord(max.x - min.x + 1, max.y - min.y + 1)

            val grid = Array(size.y) { Array(size.x) { '.' } }

            start = Coord(current.x - min.x, current.y - min.y)
            current = start.copy()
            for (move in moves) {
                val dir = move.dir
                val steps = move.steps
                for (i in 0..steps) {
                    grid[current.y + i * dir.dy][current.x + i * dir.dx] = '#'
                }
                current = current.move(move)
            }

            floodFill(grid, Coord(start.x + 1, start.y + 1))

            var inside = 0
            for (y in 0..<size.y) {
                for (x in 0..<size.x) {
                    print(grid[y][x])
                    if (grid[y][x] == '#') {
                        inside++
                    }
                }
                println()
            }
            println("Inside: $inside")
        }
    }

    private fun trapezoidArea(visited: List<Coord>): Long {
        var result = 0L
        for (i in 0..<visited.size - 1) {
            val from = visited[i]
            val to = visited[i + 1]
            val area = (from.x.toLong() - to.x.toLong()) * (from.y.toLong() + to.y.toLong())
            result += area
        }
        return abs(result) / 2
    }

    private fun floodFill(grid: Array<Array<Char>>, seed: Coord) {
        val queue = ArrayDeque<Coord>()
        queue.add(seed)
        while (queue.isNotEmpty()) {
            val current = queue.poll()
            Dir.entries.forEach { dir ->
                val next = Coord(current.x + dir.dx, current.y + dir.dy)
                if (next.inBounds(grid) && grid[next.y][next.x] == '.') {
                    grid[next.y][next.x] = '#'
                    queue.add(next)
                }
            }
        }
    }

    data class Coord(val x: Int, val y: Int) {
        fun move(move: Move): Coord {
            return Coord(x + move.dir.dx * move.steps, y + move.dir.dy * move.steps)
        }

        fun inBounds(grid: Array<Array<Char>>): Boolean {
            return x >= 0 && x < grid[0].size && y >= 0 && y < grid.size
        }
    }

    enum class Dir(val dx: Int, val dy: Int) {
        R(1, 0), D(0, 1), L(-1, 0), U(0, -1)
    }


    data class Move(val dir: Dir, val steps: Int, val colorCode: ColorCode) {

    }
}