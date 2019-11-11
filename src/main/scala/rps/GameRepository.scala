package rps

import scala.collection.concurrent.TrieMap

import model._
import java.util.concurrent.atomic.AtomicInteger

trait GameRepository {
  def save(play: Play): Int
  def read(userId: Int): Option[Play]
}

class InMemoryGameRepository extends GameRepository {
  private val map = TrieMap.empty[Int, Play]
  private var nextUserId = new AtomicInteger

  override def save(play: Play): Int = {
    val userId = nextUserId.getAndIncrement
    map.put(userId, play)
    userId
  }

  override def read(userId: Int): Option[Play] =
    map.get(userId)

}
