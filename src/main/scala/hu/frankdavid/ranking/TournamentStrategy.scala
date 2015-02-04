package hu.frankdavid.ranking

import scala.collection.mutable

abstract class TournamentStrategy(val name: String) {
  def matchesOrResult(implicit context: TournamentContext): MatchesOrResult

  def typeName: String = getClass.getSimpleName.replace("$", "")

  protected def disjointGames(games: Traversable[MatchUp]): Set[MatchUp] = {
    val busy = mutable.Set[Player]()
    games.filter { game =>
      if (busy(game.player1) || busy(game.player2)) {
        false
      } else {
        busy += game.player1
        busy += game.player2
        true
      }
    }.toSet
  }

}
