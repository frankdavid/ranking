package hu.frankdavid.ranking

import hu.frankdavid.ranking.GameHistory._
import hu.frankdavid.ranking.strategy.util.ResultGraph

class GameHistory private(val matchupGamesMap: Map[MatchUp, List[Game]], val playerGamesMap: Map[Player, List[Game]],
                          val allGames: List[Game] = List(), private var _resultGraph: ResultGraph)
  extends Traversable[Game] {

  def this() = this(Map(), Map(), _resultGraph = ResultGraph.empty)

  def resultGraph = {
    if (_resultGraph == null) {
      _resultGraph = ResultGraph(this)
    }
    _resultGraph
  }

  def +(game: Game): GameHistory = {
    val updated = updatedMap((matchupGamesMap, playerGamesMap), game)
    new GameHistory(updated._1, updated._2, game :: allGames, _resultGraph = _resultGraph + game)
  }

  def ++(games: List[Game]): GameHistory = {
    val updated = games.foldLeft((matchupGamesMap, playerGamesMap))((map, game) => updatedMap(map, game))
    new GameHistory(updated._1, updated._2, games ::: allGames, _resultGraph = _resultGraph ++ games)
  }

  def apply(player: Player): Seq[Game] = playerGamesMap.getOrElse(player, List())

  def apply(players: MatchUp): Seq[Game] = matchupGamesMap.getOrElse(players, List())

  def maybeLatestGame(matchup: MatchUp): Option[Game] = matchupGamesMap.get(matchup).flatMap(_.headOption)

  def hasResult(players: MatchUp): Boolean = matchupGamesMap.contains(players)

  def foreach[U](f: (Game) => U) = allGames foreach f
}

object GameHistory {
  def apply(games: List[Game]) = {
    val (matchup, player) = games.foldLeft((Map[MatchUp, List[Game]](), Map[Player, List[Game]]())) { (map, game) =>
        updatedMap(map, game)
    }
    new GameHistory(matchup, player, games, null)
  }
  private def updatedMap(map: (Map[MatchUp, List[Game]], Map[Player, List[Game]]), game: Game):
  (Map[MatchUp, List[Game]], Map[Player, List[Game]]) = {
    val (matchupMap, playerMap) = map
    (
    matchupMap.updated(game.matchup, game :: matchupMap.getOrElse(game.matchup, List())),
    playerMap.updated(game.matchup.player1, game :: playerMap.getOrElse(game.matchup.player1, List()))
             .updated(game.matchup.player2, game :: playerMap.getOrElse(game.matchup.player2, List()))
      )
  }
}
