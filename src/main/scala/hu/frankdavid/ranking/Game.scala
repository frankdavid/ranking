package hu.frankdavid.ranking

case class Game(matchup: MatchUp, score1: Double, score2: Double) extends Ordering[Player] {
  def compare(x: Player, y: Player): Int = {
    if (matchup.player1 == x && matchup.player2 == y) {
      score1.compareTo(score2)
    } else if (matchup.player2 == x && matchup.player1 == y) {
      score2.compareTo(score1)
    } else {
      0
    }
  }

  def winner: Option[Player] = {
    if (score1 > score2) Some(matchup.player1)
    else if (score1 < score2) Some(matchup.player2)
    else None
  }

  def loser: Option[Player] = {
    if (score1 > score2) Some(matchup.player2)
    else if (score1 < score2) Some(matchup.player1)
    else None
  }

  def winnerLoser: Option[(Player, Player)] = {
    for (winner <- winner; loser <- loser) yield (winner, loser)
  }

  def result: Int = compare(matchup.player1, matchup.player2)

  def first: Player = matchup.player1

  def second: Player = matchup.player2

  def firstWins: Boolean = result == 1

  def secondWins: Boolean = result == -1

  def isDraw = result == 0

  def playersDescendingInOrder = if (secondWins) Seq(second, first) else Seq(first, second)
}


object Game {
  def apply(player1: (Player, Int), player2: (Player, Int)): Game = apply(player1, player2, 0)

  def apply(player1: (Player, Int), player2: (Player, Int), priority: Int): Game = {
    apply(MatchUp(player1._1, player2._1, priority), player1._2, player2._2)
  }
}
