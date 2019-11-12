package rps

import scala.collection.concurrent.TrieMap

import model._
import java.util.concurrent.atomic.AtomicInteger
import slick.driver.H2Driver.api._
import slick.lifted.ProvenShape
import scala.concurrent.duration._
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
  Await.result(setupFuture, Duration(500, MILLISECONDS))

  override def save(play: Play): UserId = {
    val userMove = play.userMove
    val computerMove = play.computerMove
    val result = play.result
    val plays = TableQuery[Plays]
    val action = (plays returning plays.map(_.id)) +=
      Play(userMove, computerMove, result, None) // https://stackoverflow.com/a/55269918/2219670
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
      case play :: _ => Some(play) // assume only 1 row is returned
    }
  }

}

class Plays(tag: Tag) extends Table[Play](tag, "PLAYS") {
  def userMove = column[String]("PLA_USERMOVE")
  def computerMove = column[String]("PLA_COMPUTERMOVE")
  def result = column[String]("PLA_RESULT")
  def id = column[Int]("PLA_ID", O.PrimaryKey, O.AutoInc)

  def moveFromString(m: String) = implicitly[CaseEnumSerialization[Move]].caseFromString(m).get
  def resultFromString(r: String) = implicitly[CaseEnumSerialization[Result]].caseFromString(r).get

  def * = (userMove, computerMove, result, id.?).shaped <> (
    {
      case (userMove, computerMove, result, id) => Play(
        moveFromString(userMove),
        moveFromString(computerMove),
        resultFromString(result),
        id,
      )
    },
    {
      play: Play =>
        Some((play.userMove.toString, play.computerMove.toString, play.result.toString, None))
    }
  )
}
