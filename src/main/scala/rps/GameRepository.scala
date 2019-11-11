package rps

import scala.collection.concurrent.TrieMap

import model._
import java.util.concurrent.atomic.AtomicInteger

trait GameRepository {
  def save(play: Play): UserId
  def read(userId: UserId): Option[Play]
}

class InMemoryGameRepository extends GameRepository {
  private val map = TrieMap.empty[UserId, Play]
  private var nextInt = new AtomicInteger

  override def save(play: Play): UserId = {
    val userId = UserId(nextInt.getAndIncrement)
    map.put(userId, play)
    userId
  }

  override def read(userId: UserId): Option[Play] =
    map.get(userId)

}
