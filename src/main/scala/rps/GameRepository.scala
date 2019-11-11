package rps

import scala.collection.concurrent.TrieMap

import model._

trait GameRepository {
  def save(play: Play): Int
  def read(userId: Int): Option[Play]
}

class InMemoryGameRepository extends GameRepository {
  private val map = TrieMap.empty[Int, Play]
  private var nextUserId = 0

  override def save(play: Play): Int = {
    map.put(nextUserId, play)
    nextUserId = nextUserId + 1
    nextUserId - 1 // returns the key for the just stored value
  }

  override def read(userId: Int): Option[Play] =
    map.get(userId)

}
