package hu.frankdavid.pingpong

case class TournamentContext(players: Set[Player],
                             topN: Int,
                             results: Map[Match, Int] = Map()) {
  def hasResult(m: Match): Boolean =
    results.contains(m) || results.contains(m.swap)

  def resultOption(m: Match): Option[Int] =
    results.get(m).orElse(results.get(m.swap).map(-_))

  def withMatchResult(m: Match, result: Int): TournamentContext = {
    copy(results = results.updated(m, result))
  }
}