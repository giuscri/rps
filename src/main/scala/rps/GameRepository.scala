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

  val storedPlays = TableQuery[StoredPlays]
  val setupAction = storedPlays.schema.create
  val setupFuture = db.run(setupAction)
  Await.result(setupFuture, Duration.Inf)

  override def save(play: Play): UserId = {
    val userMove = play.userMove
    val computerMove = play.computerMove
    val result = play.result
    val storedPlays = TableQuery[StoredPlays]
    val action = (storedPlays returning storedPlays.map(_.id)) +=
      StoredPlay(Play(userMove, computerMove, result)) // https://stackoverflow.com/a/55269918/2219670
    val future = db.run(action)
    val id = Await.result(future, Duration.Inf) // assume this always succeeds
    UserId(id)
  }

  override def read(userId: UserId): Option[Play] = {
    val storedPlays = TableQuery[StoredPlays]
    val action = storedPlays.filter(_.id === userId.id).result
    val future = db.run(action)

    val moveFromString = implicitly[CaseEnumSerialization[Move]].caseFromString _
    val resultFromString = implicitly[CaseEnumSerialization[Result]].caseFromString _

    Await.result(future, Duration.Inf).toList match { // why a Vector is returned instead of a Seq?
      case Nil => None
      case StoredPlay(play, _) :: _ => Some(play) // assume only 1 row is returned
    }
  }

}

case class StoredPlay(play: Play, id: Option[Int] = None)

class StoredPlays(tag: Tag) extends Table[StoredPlay](tag, "STOREDPLAYS") {
  def userMove = column[String]("STO_USERMOVE")
  def computerMove = column[String]("STO_COMPUTERMOVE")
  def result = column[String]("STO_RESULT")

  def id = column[Int]("STO_ID", O.PrimaryKey, O.AutoInc)

  def sMove(m: String) = implicitly[CaseEnumSerialization[Move]].caseFromString(m).get
  def sResult(r: String) = implicitly[CaseEnumSerialization[Result]].caseFromString(r).get

  def * = (id.?, (userMove, computerMove, result)).shaped <> (
    {
      case (id, (um, cm, r)) => {
        StoredPlay(Play.tupled.apply(sMove(um), sMove(cm), sResult(r)), id)
      }
    },
    {
      storedPlay: StoredPlay => {
        Some((None, (storedPlay.play.userMove.toString, storedPlay.play.computerMove.toString, storedPlay.play.result.toString)))
      }
    }
  )
}
