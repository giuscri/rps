package rps

import scala.collection.concurrent.TrieMap

import model._
import java.util.concurrent.atomic.AtomicInteger
import slick.driver.H2Driver.api._
import slick.lifted.ProvenShape
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import io.buildo.enumero.CaseEnumSerialization

trait GameRepository {
  def save(play: Play): UserId
  def read(userId: UserId): Option[Play]
}

class InMemoryGameRepository extends GameRepository {
  private val map = TrieMap.empty[UserId, Play]
  private val db = Database.forConfig("h2mem1") // TODO: we should db.close this!

  val plays = TableQuery[Plays]
  val setupAction = plays.schema.create
  val setupFuture = db.run(setupAction)
  Await.result(setupFuture, Duration.Inf)

  override def save(play: Play): UserId = {
    val userMove = play.userMove
    val computerMove = play.computerMove
    val result = play.result
    val plays = TableQuery[Plays]
    val action = (plays returning plays.map(_.id)) +=
      (0, userMove.toString(), computerMove.toString(), result.toString()) // https://stackoverflow.com/a/55269918/2219670
    val future = db.run(action)
    val id = Await.result(future, Duration.Inf) // assume this always succeeds
    UserId(id)
  }

  override def read(userId: UserId): Option[Play] = {
    val plays = TableQuery[Plays]
    val action = plays.filter(_.id === userId.id).result
    val future = db.run(action)

    val moveFromString = implicitly[CaseEnumSerialization[Move]].caseFromString _
    val resultFromString = implicitly[CaseEnumSerialization[Result]].caseFromString _

    Await.result(future, Duration.Inf).toList match { // why a Vector is returned instead of a Seq?
      case Nil => None
      case (_, userMoveAsString, computerMoveAsString, resultAsString) :: _ => { // assume only 1 row is returned
        (moveFromString(userMoveAsString), moveFromString(computerMoveAsString), resultFromString(resultAsString)) match {
          case (None, _, _) | (_, None, _) | (_, _, None) => None
          case (Some(userMove), Some(computerMove), Some(result)) => Some(Play(userMove, computerMove, result))
        }
      }
    }
  }

}

class Plays(tag: Tag) extends Table[(Int, String, String, String)](tag, "PLAYS") {
  def id: Rep[Int] = column[Int]("PLA_ID", O.PrimaryKey, O.AutoInc)
  def userMove: Rep[String] = column[String]("PLA_USERMOVE")
  def computerMove: Rep[String] = column[String]("PLA_COMPUTERMOVE")
  def result: Rep[String] = column[String]("PLA_RESULT")

  def * : ProvenShape[(Int, String, String, String)] = (id, userMove, computerMove, result)
}
