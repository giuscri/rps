package rps

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn
import scala.util.Random

import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.circe.generic.auto._
import io.buildo.enumero.circe._

import rps.model.Move
import rps.model.Move._
import rps.model.Result._
import rps.model.{Request, Response}

object Main extends App {

  // boilerplate code from https://github.com/akka/akka-http/blob/v10.1.10/docs/src/test/scala/docs/http/scaladsl/HttpServerExampleSpec.scala#L318-L348

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  val route =
    path("rps" / "play") {
      post {
        entity(as[Request]) { request =>
          request match {
            case Request(userMove) =>
              val (computerMove, result) = Game.play(userMove)
              complete(Response(userMove, computerMove, result))
          }
        }
      }
    } ~ options(complete())

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}
