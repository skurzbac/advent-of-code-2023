import java.nio.file.Path
import java.util.*
import kotlin.io.path.readLines

val secondStar = true
val heatLossMap =
    Path.of("/Users/Stefan.Kurzbach/private-repos/advent-of-code-2023/src/main/java/Day_17.txt").readLines()
        .map { it.map { it.digitToInt() } };
val visited = mutableSetOf<VisitedCrucible>()
val current = PriorityQueue(compareBy<Crucible> { it.totalHeatLoss })

fun main() {
    current.add(Crucible(0, 0, Dir.RIGHT, 1, 0))
    visited.add(VisitedCrucible(0, 0, Dir.RIGHT, 1))
    //sanityCheck()
    search()
}

fun search() {
    var steps = 0
    while (current.isNotEmpty() && step()) {
        if (++steps % 10000 == 0) {
            println("Steps: $steps, Size: ${current.size}")
        }
    }
}

private fun step(): Boolean {
    val crucible = current.poll()
    if (crucible.x == heatLossMap[0].size - 1 && crucible.y == heatLossMap.size - 1
        && (!secondStar || crucible.straightSteps >= 4)
    ) {
        println("Found path")
        crucible.printPath()
        return false
    }
    Move.entries.forEach { move ->
        if (crucible.canMove(move)) {
            val next = crucible.move(move)
            val visitedCrucible = VisitedCrucible(next.x, next.y, next.dir, next.straightSteps)
            if (!visited.contains(visitedCrucible)) {
                current.add(next)
                visited.add(visitedCrucible)
            }
        }
    }
    return true
}

data class VisitedCrucible(
    val x: Int,
    val y: Int,
    val dir: Dir,
    val straightSteps: Int
)

data class Crucible(
    val x: Int,
    val y: Int,
    val dir: Dir,
    val straightSteps: Int,
    val totalHeatLoss: Int,
    val previous: Crucible? = null
) {

    fun canMove(move: Move): Boolean {
        if (secondStar) {
            when {
                move != Move.STRAIGHT && straightSteps < 4 -> return false
                move == Move.STRAIGHT && straightSteps == 10 -> return false
            }
        } else {
            if (move == Move.STRAIGHT && straightSteps == 3) {
                return false
            }
        }
        return move.nextDir(dir).let { nextDir ->
            val nextX = x + nextDir.dx
            val nextY = y + nextDir.dy
            nextX >= 0 && nextX < heatLossMap[0].size && nextY >= 0 && nextY < heatLossMap.size
        }
    }

    fun move(move: Move): Crucible {
        val nextDir = move.nextDir(dir)
        return when (move) {
            Move.STRAIGHT -> copy(
                x = x + nextDir.dx,
                y = y + nextDir.dy,
                dir = nextDir,
                straightSteps = straightSteps + 1,
                totalHeatLoss = totalHeatLoss + heatLossMap[y + nextDir.dy][x + nextDir.dx],
                previous = this
            )

            Move.LEFT, Move.RIGHT -> copy(
                x = x + nextDir.dx,
                y = y + nextDir.dy,
                dir = nextDir,
                straightSteps = 1,
                totalHeatLoss = totalHeatLoss + heatLossMap[y + nextDir.dy][x + nextDir.dx],
                previous = this
            )
        }
    }

    fun printPath() {
        generateSequence(this) { it.previous }.forEach { println("x: ${it.x}, y: ${it.y}, dir: ${it.dir}, straightSteps: ${it.straightSteps}, totalHeatLoss: ${it.totalHeatLoss}") }
    }
}

fun sanityCheck() {
    var crucible = Crucible(0, 0, Dir.RIGHT, 1, heatLossMap[0][0])

    assert(crucible.canMove(Move.STRAIGHT))
    assert(crucible.canMove(Move.RIGHT))
    assert(!crucible.canMove(Move.LEFT))

    crucible = crucible.move(Move.STRAIGHT)

    assert(crucible.canMove(Move.STRAIGHT))
    assert(crucible.canMove(Move.RIGHT))
    assert(!crucible.canMove(Move.LEFT))

    crucible = crucible.move(Move.STRAIGHT)

    assert(!crucible.canMove(Move.STRAIGHT))
    assert(crucible.canMove(Move.RIGHT))
    assert(!crucible.canMove(Move.LEFT))

    crucible = crucible.move(Move.RIGHT)

    assert(crucible.canMove(Move.STRAIGHT))
    assert(crucible.canMove(Move.RIGHT))
    assert(crucible.canMove(Move.LEFT))

    crucible = crucible.move(Move.LEFT)

    assert(crucible.canMove(Move.STRAIGHT))
    assert(crucible.canMove(Move.RIGHT))
    assert(crucible.canMove(Move.LEFT))

    crucible = crucible.move(Move.LEFT)

    assert(!crucible.canMove(Move.STRAIGHT))
    assert(crucible.canMove(Move.RIGHT))
    assert(crucible.canMove(Move.LEFT))
}

enum class Dir(val dx: Int, val dy: Int) {
    UP(0, -1), DOWN(0, 1), LEFT(-1, 0), RIGHT(1, 0)
}

enum class Move(val nextDir: (Dir) -> Dir) {
    STRAIGHT({ it }), LEFT({ dir ->
        when (dir) {
            Dir.UP -> Dir.LEFT
            Dir.LEFT -> Dir.DOWN
            Dir.DOWN -> Dir.RIGHT
            Dir.RIGHT -> Dir.UP
        }
    }),
    RIGHT({ dir ->
        when (dir) {
            Dir.UP -> Dir.RIGHT
            Dir.RIGHT -> Dir.DOWN
            Dir.DOWN -> Dir.LEFT
            Dir.LEFT -> Dir.UP
        }
    })
}