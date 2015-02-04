package hu.frankdavid.ranking.strategy

import hu.frankdavid.ranking._

import scala.collection.mutable

case class TraditionalSwissStrategy(maxRounds: (TournamentContext => Int) = SwissStrategy.DefaultNumberOfRounds)
  extends SwissStrategy("Simple Swiss Strategy", maxRounds) {

  protected def playerScores(implicit context: TournamentContext): collection.Map[Player, Double] = {
    val scores = new mutable.HashMap[Player, Double].withDefaultValue(0)
    for (game <- context.gameHistory; winner <- game.winner) {
      scores(winner) = scores(winner) + 1
    }
    for (game <- context.gameHistory; if game.isDraw) {
      scores(game.matchup.player1) = scores(game.matchup.player1) + 0.5
      scores(game.matchup.player2) = scores(game.matchup.player2) + 0.5
    }
    scores
  }
}
