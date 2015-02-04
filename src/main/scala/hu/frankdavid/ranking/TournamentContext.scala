package hu.frankdavid.ranking

import hu.frankdavid.ranking.strategy.util.ResultGraph

import scala.util.Random


case class TournamentContext(players: Seq[Player],
                             numAwardedPlayers: Int,
                             randomSeed: Long,
                             maxParallelism: Int,
                             round: Int,
                             gameHistory: GameHistory = new GameHistory()
                              ) {

  def resultGraph: ResultGraph = gameHistory.resultGraph

  def withGame(game: Game): TournamentContext = {
    copy(gameHistory = gameHistory + game)
  }

  def withGames(games: List[Game]) = {
    copy(gameHistory = gameHistory ++ games)
  }

  def createRandom(): Random = new Random(randomSeed)
}
