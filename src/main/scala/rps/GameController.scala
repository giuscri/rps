package rps

import model._
import wiro.annotation._
import scala.concurrent.{ExecutionContext, Future}

import io.buildo.enumero.{CaseEnumIndex, CaseEnumSerialization}

@path("rps")
trait GameController {
  @command
  def play(userMove: Move): Future[Either[Throwable, Int]]

  @query
  def result(k: Int): Future[Either[Throwable, PlayResponse]]
}

class GameControllerImpl(gameService: GameService)(implicit ec: ExecutionContext)
    extends GameController {
  override def play(userMove: Move): Future[Either[Throwable, Int]] =
    Future {
      Right(gameService.playMove(userMove))
    }

  override def result(k: Int): Future[Either[Throwable, PlayResponse]] =
    Future {
      gameService.getResult(k).map { play =>
        PlayResponse(play.userMove, play.computerMove, play.result)
      }.toRight(new IllegalStateException)
    }

}
