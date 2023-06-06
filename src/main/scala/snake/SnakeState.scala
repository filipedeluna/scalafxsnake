package snake

import scalafx.scene.shape.Rectangle
import snake.SnakeState.Direction.*
import snake.SnakeState.{Direction, computeMovement}

import scala.math.{pow, sqrt}

case class SnakeState(private val path: List[Direction], private val squares: List[(Int, Int)]) {
  def move(direction: Direction): SnakeState =
    val newPath = direction :: path.take(squares.length)
    val newSquares = squares.zip(newPath).map((c, d) => computeMovement(c._1, c._2, d))

    copy(path = newPath, squares = newSquares)

  def eat(pos: (Int, Int)): SnakeState = copy(squares = squares :+ pos)

  def getSquares: Seq[Rectangle] = squares.map((x, y) => Rectangle(x + 2, y + 2, 16, 16))
  def getHead: (Int, Int) = squares.head
  def getTail: (Int, Int) = squares.last

  def coordsOverlap(coords: (Int, Int)): Boolean = squares.contains(coords)
  def isNearSnake(coords: (Int, Int)): Boolean = squares.exists(s => getDistance(coords, s) < 2)
  def touchedItself: Boolean = squares.distinct.length != squares.length
}

object SnakeState extends SnakeState(path = List(Direction.Left), squares = List((380, 380))) {
  enum Direction:
    case Right, Left, Down, Up

  private def computeMovement(x: Int, y: Int, direction: Direction): (Int, Int) = direction match {
    case Direction.Right => (x + 20, y)
    case Direction.Left  => (x - 20, y)
    case Direction.Down  => (x, y + 20)
    case Direction.Up    => (x, y - 20)
  }
}

private def getDistance(a: (Int, Int), b: (Int, Int)): Double =
  val xMin = a._1 min b._1
  val xMax = a._1 max b._1
  val yMin = a._2 min b._2
  val yMax = a._2 max b._2

  sqrt(pow(xMax - xMin, 3) + pow(yMax - yMin, 3))
