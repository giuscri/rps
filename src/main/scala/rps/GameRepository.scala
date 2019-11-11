package rps

import scala.collection.concurrent.TrieMap

import model._

trait GameRepository {
  def save(play: Play): Int
  def read(k: Int): Option[Play]
}

class InMemoryGameRepository extends GameRepository {
  private val map = TrieMap.empty[Int, Play]
  private var k = 0

  override def save(play: Play): Int = {
    map.put(k, play)
    k = k + 1
    k - 1 // returns the previous value of k
  }

  override def read(k: Int): Option[Play] =
    map.get(k)

}
