package hu.frankdavid.ranking.strategy

import hu.frankdavid.ranking._

case object GreedyGraphStrategy extends TournamentStrategy("Greedy Graph Strategy") {

  def matchesOrResult(implicit context: TournamentContext): MatchesOrResult = {
    val betterPlayers = context.resultGraph.betterPlayers
    val worsePlayers = context.resultGraph.worsePlayers

    val betterPlayersCount = betterPlayers.map(p => (p._1, p._2.count(context.players.contains))).withDefaultValue(0)

    val eliminated = (for ((worse, numberOfPlayers) <- betterPlayersCount
                           if numberOfPlayers >= context.numAwardedPlayers) yield worse).toSet

    val worsePlayersCount = worsePlayers.map(p => (p._1, p._2.count(!eliminated.contains(_)))).withDefaultValue(0)

    val remaining = context.players.diff(eliminated.toSeq)
    val games = remaining.combinations(2)
                .map(MatchUp(_))
                .filter (pair => !worsePlayers.containsEntry(pair.player1, pair.player2) &&
                         !worsePlayers.containsEntry(pair.player2, pair.player1))
                .toSeq
                .sortBy{pair => -(worsePlayersCount(pair.player1) max worsePlayersCount(pair.player2))}

    if (games.nonEmpty) {
      Matches(disjointGames(games).take(context.maxParallelism))
    } else {
      Result(remaining.sortBy(-worsePlayersCount(_)))
    }
  }

}
