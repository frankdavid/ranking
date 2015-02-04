package hu.frankdavid.ranking.strategy

import hu.frankdavid.ranking._

import scala.collection.mutable.ListBuffer

abstract class SwissStrategy(name: String, val maxRoundsFn: (TournamentContext => Int))
  extends TournamentStrategy(name + " (" + maxRoundsFn + ")") {


  def matchesOrResult(implicit context: TournamentContext): MatchesOrResult = {
    val players = context.players
    val scores = playerScores
    val sortedPlayers = players.sortBy(-scores(_))
    if (context.round < maxRounds) {
      val games = calculateMatches(sortedPlayers, scores)
      if (games.isEmpty) {
        Result(sortedPlayers.take(context.numAwardedPlayers))
      } else {
        Matches(games)
      }
    } else {
      Result(sortedPlayers.take(context.numAwardedPlayers))
    }
  }

  private def calculateMatches(sortedPlayers: Seq[Player], scores: collection.Map[Player, Double])
                              (implicit context: TournamentContext): Set[MatchUp] = {
    val matched = new collection.mutable.HashSet[Player]
    val games = new ListBuffer[MatchUp]
    def pairs = sortedPlayers.filterNot(matched.contains).sliding(2)
                .map { pair => MatchUp(pair(0), pair(1), 0, enableDraw = true)}
                .filterNot(context.gameHistory.hasResult)
    while (true) {
      if (pairs.isEmpty)
        return games.toSet
      else {
        val pair = pairs.minBy(pair => scores(pair.player1) - scores(pair.player2))
        matched += pair.player1
        matched += pair.player2
        games += pair
      }
    }
    games.toSet
  }

  // less accurate but faster solution
  private def calculateMatches(sortedPlayers: Seq[Player])(implicit context: TournamentContext): Set[MatchUp] = {
    val games = new ListBuffer[MatchUp]
    val matched = new collection.mutable.HashSet[Player]
    sortedPlayers.foreach { player =>
      if (!(matched contains player)) {
        def hasResult(partner: Player) = context.gameHistory.hasResult(MatchUp(player, partner)) ||
          (matched contains partner)
        val maybePartner = sortedPlayers.dropWhile(_ != player).drop(1).dropWhile(hasResult).headOption
        maybePartner foreach { partner =>
          games += MatchUp(player, partner, 0, enableDraw = true)
          matched += player
          matched += partner
        }
      }
    }
    games.toSet
  }

  protected def playerScores(implicit context: TournamentContext): collection.Map[Player, Double]

  protected def maxRounds(implicit context: TournamentContext): Int = {
    maxRoundsFn(context)
  }
}

object SwissStrategy {
  def DefaultNumberOfRounds(context: TournamentContext) =
    math.ceil(math.log(context.players.size) / math.log(2)).toInt + 2
}
