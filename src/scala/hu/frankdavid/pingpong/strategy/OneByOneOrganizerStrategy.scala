package hu.frankdavid.pingpong.strategy

import hu.frankdavid.pingpong.{Match, Player, TournamentContext}

class OneByOneOrganizerStrategy extends MatchOrganizerStrategy("One by one Strategy") {

  def matchesOrResult(implicit context: TournamentContext): Either[Set[Match], Seq[Player]] = {
    null
  }

}
