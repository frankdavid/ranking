package hu.frankdavid.pingpong.strategy

import hu.frankdavid.pingpong.{Match, Player, TournamentContext}

import scala.collection.mutable

class GraphMatchOrganizerStrategy extends MatchOrganizerStrategy("Graph Strategy") {

  def matchesOrResult(implicit context: TournamentContext): Either[Set[Match], Seq[Player]] = {
    val betterThan = new mutable.HashMap[Player, mutable.Set[Player]] with mutable.MultiMap[Player, Player]
    val worseThan = new mutable.HashMap[Player, mutable.Set[Player]] with mutable.MultiMap[Player, Player]
    context.results.foreach {
      case (m, result) =>
        if (result > 0) {
          betterThan.addBinding(m.player1, m.player2)
          worseThan.addBinding(m.player2, m.player1)
        }
        if (result < 0) {
          betterThan.addBinding(m.player2, m.player1)
          worseThan.addBinding(m.player1, m.player2)
        }
    }
    val bestPlayers = context.players.toSeq.sortBy(-betterThan.getOrElse(_, Set()).size)
    bestPlayers.grouped(2).flatMap { players =>
      if (players.size < 2)
        None
      else {
        val m = Match(players(0), players(1))
        if (context.hasResult(m))
          None
        else
          Some(m)
      }
    }.toSet
    null
  }

}
