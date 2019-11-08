package rps

import rps.model._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

class GameApiImpl(implicit ec: ExecutionContext) extends GameApi {
    override def play(userMove: Move): Future[Either[Throwable, PlayResponse]] = Future {
        Right(PlayResponse.tupled(Game.play(userMove)))
    }
}
