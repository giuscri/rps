package rps

import scala.concurrent.Future
import wiro.annotation._
import rps.model._

@path("rps")
trait GameApi {
    @command
    def play(userMove: Move): Future[Either[Throwable, PlayResponse]]
}
