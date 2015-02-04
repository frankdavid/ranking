package hu.frankdavid.ranking.strategy

import hu.frankdavid.ranking._
import hu.frankdavid.ranking.strategy.util.ContextOrdering

case class TimSortStrategy(guessAhead: (TournamentContext => Int) = { _ => 0})
  extends TournamentStrategy(s"TimSort Strategy $guessAhead") {
  def matchesOrResult(implicit context: TournamentContext): MatchesOrResult = {
    try {
      val ordering = new ContextOrdering(guessAhead(context))
      val sorted = context.players.sorted(ordering).reverse
      if (ordering.requiresGames) {
        Matches(disjointGames(ordering.requiredGames).take(context.maxParallelism))
      } else {
        Result(sorted.take(context.numAwardedPlayers))
      }
    } catch {
      case ResultRequiredException(requiredGames) => {
        Matches(disjointGames(requiredGames).take(context.maxParallelism))
      }
    }
  }
}
