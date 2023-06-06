package snake

import javafx.util.Duration
import scalafx.Includes.*
import scalafx.animation.AnimationTimer
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.beans.property.*
import scalafx.scene.Scene
import scalafx.scene.input.{KeyCode, KeyEvent}
import scalafx.scene.media.{AudioClip, Media, MediaPlayer}
import scalafx.scene.paint.Color.*
import scalafx.scene.shape.Rectangle
import snake.SnakeState.Direction.*
import snake.SnakeState.Direction

import scala.io.Source
import scala.util.Random

object Snake extends JFXApp3 {
  override def start(): Unit =
    stage = new JFXApp3.PrimaryStage { title = "Snake" }
    stage.setScene(gameScene)

  private def gameScene: Scene =
    new Scene(width = 800, height = 800) {
      fill = White
      content = drawGrid

      val deathSoundPlayer: AudioClip = AudioClip(getClass.getResource("/oof.mp3").toURI.toString)
      val eatSoundPlayer: AudioClip = AudioClip(getClass.getResource("/eat.mp3").toURI.toString)
      val songPlayer: AudioClip = AudioClip(getClass.getResource("/song.mp3").toURI.toString)
      songPlayer.setCycleCount(999)
      songPlayer.volume = 0.7
      songPlayer.play()
      val direction: ObjectProperty[Direction] = ObjectProperty(Left)
      val frameDelay: LongProperty = LongProperty(100) // Frame Delay in ms
      val lastFrameTime: LongProperty = LongProperty(0)
      val food: ObjectProperty[Option[(Int, Int)]] = ObjectProperty(None)
      val obstacles: ObjectProperty[List[(Int, Int)]] = ObjectProperty(List.empty)
      val snake: ObjectProperty[SnakeState] = ObjectProperty(SnakeState)

      // Listener for changing direction
      onKeyPressed = (e: KeyEvent) =>
        direction.value = e.code match {
          case KeyCode.Up if direction.value != Down    => Up
          case KeyCode.Down if direction.value != Up    => Down
          case KeyCode.Left if direction.value != Right => Left
          case KeyCode.Right if direction.value != Left => Right
          case _                                        => direction.value
        }

      def deathRoutine(): Unit =
        snake.setValue(SnakeState)
        deathSoundPlayer.play()
        obstacles.value = List.empty
        food.value = None

      AnimationTimer(time => {
        if (time - lastFrameTime.value >= frameDelay.value * 1e6)
          val maybeFoodRectangle: Option[Rectangle] =
            food.value.map((x, y) => {
              val foodRect = Rectangle(x + 2, y + 2, 16, 16)
              foodRect.fill = Red
              foodRect
            })

          val obstacleRectangles: List[Rectangle] =
            obstacles.value.map((x, y) => {
              val foodRect = Rectangle(x + 2, y + 2, 16, 16)
              foodRect.fill = Blue
              foodRect
            })

          val oldTail = snake.value.getTail
          snake.value = snake.value.move(direction.value)

          if (snake.value.touchedItself)
            println("Game over, you ate yourself")
            deathRoutine()

          if (isOffBounds(snake.value.getHead))
            println("Game over, you are off bounds")
            deathRoutine()

          if (obstacles.value.exists(snake.value.coordsOverlap))
            println("Game over, you hit an obstacle")
            deathRoutine()

          if (food.value.contains(snake.value.getHead))
            snake.value = snake.value.eat(oldTail)
            food.value = None
            eatSoundPlayer.play()

          content =
            drawGrid ++ snake.value.getSquares ++ maybeFoodRectangle.toList ++ obstacleRectangles

          while (food.value.isEmpty)
            val candidate: (Int, Int) = (Random.nextInt(40) * 20, Random.nextInt(20) * 20)
            if (!snake.value.isNearSnake(candidate) && !obstacles.value.contains(candidate))
              food.value = Some(candidate)

          if (Random.nextInt(100) >= 94)
            val obstaclesSize = obstacles.value.size
            while (obstaclesSize == obstacles.value.size)
              val candidate: (Int, Int) = (Random.nextInt(40) * 20, Random.nextInt(20) * 20)
              if (!snake.value.isNearSnake(candidate) && !food.value.contains(candidate))
                obstacles.value = obstacles.value :+ candidate

          lastFrameTime.value = time
      }).start()
    }

  private def isOffBounds(coords: (Int, Int)): Boolean =
    coords._1 < 0 || coords._1 > 800 || coords._2 < 0 || coords._2 > 800

  // Draw the static grid
  private def drawGrid: List[Rectangle] =
    (for {
      x <- 0.to(39).map(_ * 20)
      y <- 0.to(39).map(_ * 20)
    } yield {
      val rect = Rectangle(x, y, 20, 20)
      if ((x / 20 + y / 20) % 2 == 0)
        rect.fill = LightGrey
      else
        rect.fill = White
      rect
    }).toList
}