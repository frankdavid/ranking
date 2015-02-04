package hu.frankdavid.ranking.strategy

import hu.frankdavid.ranking._
import hu.frankdavid.ranking.strategy.util.ContextOrdering

import scala.collection.mutable

case class HeapStrategy(guessAhead: (TournamentContext => Int) = { _ => 0})
  extends TournamentStrategy("Heap Strategy " + guessAhead) {

  def matchesOrResult(implicit context: TournamentContext): MatchesOrResult = {
    val ordering = new ContextOrdering(guessAhead(context))
    val heap = new mutable.PriorityQueue[Player]()(ordering)

    try {
      heap ++= context.players
      val result = (0 until context.numAwardedPlayers) map { _ => heap.dequeue()}
      if (ordering.requiresGames) {
        Matches(disjointGames(ordering.requiredGames).take(context.maxParallelism))
      } else {
        Result(result)
      }
    } catch {
      case ResultRequiredException(games) => Matches(disjointGames(games).take(context.maxParallelism))
    }
  }

}
