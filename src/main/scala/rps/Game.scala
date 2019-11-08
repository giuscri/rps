package rps

import scala.util.Random
import model.{Move, Result}
import Move._
import Result._

import io.buildo.enumero.{CaseEnumIndex, CaseEnumSerialization}

object Game {
  def play(userMove: Move): (Move, Result) = {
    val computerMove = generateComputerMove()
    (userMove, computerMove) match {
      case (Rock, Scissors) | (Paper, Rock) | (Scissors, Paper) => (computerMove, Win)
      case (x, y) if x == y => (computerMove, Draw)
      case _ => (computerMove, Lose)
    }
  }

  private def generateComputerMove(): Move =
    Random.shuffle(List(Rock, Paper, Scissors)).head
}
