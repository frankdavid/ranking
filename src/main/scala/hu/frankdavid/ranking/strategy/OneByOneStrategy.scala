package hu.frankdavid.ranking.strategy

import hu.frankdavid.ranking._

/**
 * This strategy first finds the top rank player, then the second rank and so on
 */
case class OneByOneStrategy(delegate: TournamentStrategy) extends
TournamentStrategy(s"One by one Strategy ($delegate)") {

  def matchesOrResult(implicit context: TournamentContext): MatchesOrResult = {
    step()
  }

  private def step(ordered: List[Player] = Nil)(implicit context: TournamentContext): MatchesOrResult = {
    if (ordered.size >= context.numAwardedPlayers) {
      Result(ordered.reverse)
    } else {
      val losers =
        if (ordered.isEmpty) {
          context.players
        } else {
          context.gameHistory.collect {
            case Game(MatchUp(a, b, _, _), _, _) if ordered.contains(a) && !ordered.contains(b) => b
            case Game(MatchUp(b, a, _, _), _, _) if ordered.contains(a) && !ordered.contains(b) => b
          }.toSeq.distinct
        }
      val filteredGameHistory = GameHistory(context.gameHistory.filter {
        case Game(MatchUp(a, b, _, _), _, _) => losers.contains(a) && losers.contains(b)
        case _ => false
      }.toList)
      val subContext = context.copy(players = losers, numAwardedPlayers = 1, gameHistory = filteredGameHistory)
      val matchesOrResult = delegate.matchesOrResult(subContext)
      matchesOrResult match {
        case Result(iterable) =>
          step(iterable.head :: ordered)
        case other => other
      }
    }
  }

}
