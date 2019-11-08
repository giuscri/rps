package rps

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes, ContentType, HttpEntity}
import wiro.Config
import wiro.server.akkaHttp._
import wiro.server.akkaHttp.FailSupport._
import wiro.annotation._

import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.circe.generic.auto._
import io.buildo.enumero.circe._

import rps.model._
import Move._

import scala.concurrent.{ExecutionContext, Future}

object GameController extends RouterDerivationModule {
    def router(implicit ec: ExecutionContext): Router = {
        implicit def throwableResponse: ToHttpResponse[Throwable] = null
        deriveRouter[GameApi](new GameApiImpl)
    }

    @path("rps")
    trait GameApi {
        @command
        def play(userMove: Move): Future[Either[Throwable, Unit]]

        @query
        def result: Future[Either[Throwable, ResultResponse]]
    }

    class GameApiImpl(implicit ec: ExecutionContext) extends GameApi {
        override def play(userMove: Move): Future[Either[Throwable, Unit]] = Future {
            val (_, computerMove, result) = GameService.play(userMove)
            GameRepository.put((userMove, computerMove, result))
            Right(Unit)
        }

        override def result: Future[Either[Throwable, ResultResponse]] = Future {
            GameRepository.get match {
                case Some(t) => Right(ResultResponse.tupled(t))
                case _ => Left(throw new Exception)
            }
        }
    }

}
