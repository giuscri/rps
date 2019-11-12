package rps

import scala.util.Random
import model._
import Move._
import Result._
import scala.concurrent.Future

trait GameService {
  def playMove(userMove: Move): Future[UserId]
  def getResult(userId: UserId): Future[Option[Play]]
}

class GameServiceImpl(repository: GameRepository) extends GameService {
  override def playMove(userMove: Move): Future[UserId] = {
    val computerMove = generateComputerMove()
    val result = (userMove, computerMove) match {
      case (Rock, Scissors) | (Paper, Rock) | (Scissors, Paper) => Win
      case (x, y) if x == y                                     => Draw
      case _                                                    => Lose
    }
    val play = Play(userMove, computerMove, result)
    repository.save(play)
  }

  private def generateComputerMove(): Move =
    Random.shuffle(List(Rock, Paper, Scissors)).head

  override def getResult(userId: UserId): Future[Option[Play]] =
    repository.read(userId)
}
