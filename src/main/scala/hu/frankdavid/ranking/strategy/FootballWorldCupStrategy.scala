package hu.frankdavid.ranking.strategy

import hu.frankdavid.ranking._

case object FootballWorldCupStrategy extends TournamentStrategy("Football World Cup Strategy") {

  val GroupStageMatch = 1

  val KnockoutStageMatch = 2

  private val GroupSize: Int = 4

  private val RoundRobinStrategy = new RoundRobinStrategy(1, 3, 1, 0)

  def matchesOrResult(implicit context: TournamentContext): MatchesOrResult = {
    if (context.players.size % 4 != 0) {
      throw new StrategyException("Football Strategy requires the number of players to be devisible by 4.")
    }
    val numberOfGroups = context.players.size / GroupSize
    val pots = context.players.grouped(numberOfGroups)
    val random = context.createRandom()
    val groups = pots.map(random.shuffle(_)).toSeq.transpose.map(new Group(_))

    MatchesOrResult.chain(groups.map(_.matchesOrResult)) { (results) =>
      // winner teams are matched to the loser teams in reverse order
      val knockoutInit = (results.map(_.head) zip results.map(_.last).reverse).map(p => Seq(p._1, p._2)).flatten
      new KnockoutTreeNode(knockoutInit).matchesOrResult(3)
    }.withMaxParallelism(context.maxParallelism)
  }

  private class KnockoutTreeNode(players: Seq[Player])
                                (implicit context: TournamentContext) {

    def matchesOrResult(topN: Int): MatchesOrResult = {
      val halves = players.splitAt(players.size / 2)
      if (topN == 3) {
        val half1 = new KnockoutTreeNode(halves._1).matchesOrResult(2)
        val half2 = new KnockoutTreeNode(halves._2).matchesOrResult(2)
        half1.chain(half2) { (h1, h2) =>
          val bronze = new KnockoutTreeNode(Seq(h1(1), h2(1))).matchesOrResult(1)
          val goldSilver = new KnockoutTreeNode(Seq(h1(0), h2(0))).matchesOrResult(2)
          goldSilver.chain(bronze) { (gs, b) =>
            Result(gs ++ b)
          }
        }
      }
      else if (players.size > 1) {
        val half1 = new KnockoutTreeNode(halves._1).matchesOrResult(1)
        val half2 = new KnockoutTreeNode(halves._2).matchesOrResult(1)
        half1.chain(half2) { (h1, h2) =>
          val matchup = MatchUp(h1(0), h2(0), KnockoutStageMatch)
          context.gameHistory.maybeLatestGame(matchup) map
            (game => Result(game.playersDescendingInOrder.take(topN))) getOrElse Matches(matchup)
        }
      } else {
        // only one player
        Result(players)
      }
    }
  }

  private class Group(val players: Seq[Player])(implicit context: TournamentContext) {
    val matchesOrResult: MatchesOrResult = {
      val roundRobinContext = context.copy(players = players, numAwardedPlayers = 2)
      RoundRobinStrategy.matchesOrResult(roundRobinContext) match {
        case Matches(matches) => Matches(matches.map(_.copy(matchType = GroupStageMatch, enableDraw = true)))
        case x => x
      }
    }
  }

}
