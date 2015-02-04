package hu.frankdavid.ranking.strategy

import breeze.linalg.{DenseMatrix, DenseVector}
import hu.frankdavid.ranking._

case class ColleyStrategy(maxRounds: (TournamentContext => Int) = SwissStrategy.DefaultNumberOfRounds)
  extends SwissStrategy("Colley Strategy", maxRounds) {

  protected def playerScores(implicit context: TournamentContext): collection.Map[Player, Double] = {
    val players = context.players.toIndexedSeq
    val numPlayers = players.size
    val colleyMatrix = DenseMatrix.tabulate[Double](numPlayers, numPlayers) {
      case (i, j) => if (i != j) {
        -context.gameHistory.apply(MatchUp(players(i), players(j))).size
      } else {
        2 + context.gameHistory(players(i)).size
      }
    }
    val bVector = DenseVector.tabulate(numPlayers) { i =>
      val games = context.gameHistory(players(i))
      val losses = games.count(game => game.loser.contains(players(i)))
      val wins = games.count(game => game.winner.contains(players(i)))
      1 + (wins - losses) / 2.0
    }

    val x = colleyMatrix \ bVector
    players.zip(x.data).toMap
  }
}
