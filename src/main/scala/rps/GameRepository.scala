package rps

import scala.collection.concurrent.TrieMap
import rps.model._
import Move._
import Result._

object GameRepository {
    private val trieMap = TrieMap[Int, (Move, Move, Result)]()

    def get: Option[(Move, Move, Result)] =
        trieMap.get(0)

    def put(value: (Move, Move, Result)): Unit =
        trieMap.put(0, value)
}
