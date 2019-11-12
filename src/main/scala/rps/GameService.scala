package rps

import rps.model._
import Move._
import Result._
import scala.util.Random

object GameService {
    def play(userMove: Move): (Move, Move, Result) = {
        val computerMove = generateComputerMove()
        val result = (userMove, computerMove) match {
          case (Rock, Scissors) | (Paper, Rock) | (Scissors, Paper) => Win
          case (x, y) if x == y => Draw
          case _ => Lose
        }
        (userMove, computerMove, result)
      }

    private def generateComputerMove(): Move =
        Random.shuffle(List(Rock, Paper, Scissors)).head
}
