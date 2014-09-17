package hu.frankdavid.pingpong.strategy

import hu.frankdavid.pingpong.{Match, Player, TournamentContext}

abstract class MatchOrganizerStrategy(val name: String) {
  def matchesOrResult(implicit context: TournamentContext): Either[Set[Match], Seq[Player]]

  override def toString: String = name
}
