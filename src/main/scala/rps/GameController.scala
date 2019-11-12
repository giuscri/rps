package rps

import model._
import wiro.annotation._
import scala.concurrent.{ExecutionContext, Future}

import io.buildo.enumero.{CaseEnumIndex, CaseEnumSerialization}

@path("rps")
trait GameController {
  @command
  def play(userMove: Move): Future[Either[Throwable, UserId]]

  @query
  def result(id: Int): Future[Either[Throwable, PlayResponse]]
}

class GameControllerImpl(gameService: GameService)(implicit ec: ExecutionContext)
    extends GameController {
  override def play(userMove: Move): Future[Either[Throwable, UserId]] =
    gameService.playMove(userMove).map(userId => Right(userId))

  override def result(id: Int): Future[Either[Throwable, PlayResponse]] =
    gameService.getResult(UserId(id)).map(optionPlay => optionPlay match {
      case None => Left(new IllegalStateException)
      case Some(play) => Right(PlayResponse(play.userMove, play.computerMove, play.result))
    })

}
