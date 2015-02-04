package hu.frankdavid.ranking.strategy

import hu.frankdavid.ranking._

import scala.collection.mutable

case class MarkovChainStrategy(maxRounds: (TournamentContext => Int) = SwissStrategy.DefaultNumberOfRounds)
  extends SwissStrategy("Markov Chain Strategy", maxRounds) {
  private val SimulationRounds = 30

  private val DampingFactor = 0.9

  protected def playerScores(implicit context: TournamentContext): collection.Map[Player, Double] = {
    val players = context.players

    val loserWinners = new mutable.HashMap[Player, mutable.Set[(Player, Double)]]() with mutable.MultiMap[Player, (Player, Double)]
    for (game <- context.gameHistory.allGames; loser <- game.loser; winner <- game.winner) {
      loserWinners.addBinding(loser, (winner, math.abs(game.score2 - game.score1)))
    }

    val initialScores = new mutable.HashMap[Player, Double] ++ players.map(_ -> 1d / players.size)

    val finalScores = (1 to SimulationRounds).foldLeft(initialScores)((scoreMap, _) => {
      val newScoreMap = new mutable.HashMap[Player, Double].withDefaultValue((1 - DampingFactor) / players.size)
      for ((loser, winners) <- loserWinners) {
        val scoreDiffSum = winners.map(_._2).sum
        for ((winner, scoreDiff) <- winners) {
          newScoreMap(winner) = newScoreMap(winner) + (scoreMap(loser) * scoreDiff / scoreDiffSum) * DampingFactor
        }
      }
      newScoreMap
    })

    finalScores
  }
}
