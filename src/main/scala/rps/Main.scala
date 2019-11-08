package rps

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import wiro.Config
import wiro.server.akkaHttp._
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.buildo.enumero.circe._
import io.circe.generic.auto._
import wiro.server.akkaHttp.FailSupport._
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes, ContentType, HttpEntity}

object Main extends App with RouterDerivationModule {
  implicit val system = ActorSystem("rps")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  implicit def throwableResponse: ToHttpResponse[Throwable] = null
  val router = deriveRouter[GameApi](new GameApiImpl)

  val rpcServer = new HttpRPCServer(
    config = Config("localhost", 8080),
    routers = List(router)
  )
}
