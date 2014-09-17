package hu.frankdavid.pingpong

import hu.frankdavid.pingpong.strategy.{MatchOrganizerStrategy, MergeMatchOrganizerStrategy}

import scala.annotation.tailrec

object Workbench {

  case class MatchOrganizationResult(result: Seq[Player], numberOfRounds: Int, games: Map[Player, Int])

  def createPlayer(num: Int): Player = Player(num + "")

  @tailrec
  def organize(strategy: MatchOrganizerStrategy, round: Int = 0, playerGames: Map[Player, Int] = Map())
              (implicit context: TournamentContext, debug: Boolean): MatchOrganizationResult = {
    val matchesOrResult = strategy.matchesOrResult
    matchesOrResult match {
      case Left(matches) =>
        if (debug) {
          matches.foreach(println)
          println("-----------------------------")
        }
        val updatedPlayedGames = matches.foldLeft(playerGames) { (acc, m) =>
          acc.updated(m.player1, acc.getOrElse(m.player1, 0) + 1).updated(m.player2, acc.getOrElse(m.player2, 0) + 1)
        }
        val updatedContext = matches.foldLeft(context) { (acc, m) =>
          acc.withMatchResult(m, m.player2.name.toInt - m.player1.name.toInt)
        }
        organize(strategy, round + 1, updatedPlayedGames)(updatedContext, debug)
      case Right(results) =>
        MatchOrganizationResult(results, round, playerGames)
    }
  }

  def organizeAndTest(strategy: MatchOrganizerStrategy)(implicit context: TournamentContext, debug: Boolean): Unit = {
    val result = organize(strategy)
    if (debug) {
      result.result.zipWithIndex.foreach { case (player, index) => println(s"$index. $player")}
    }
    println(strategy.name)
    val matches = result.games.map(_._2).sum / 2
    val max = result.games.map(_._2).max
    println(s"${result.numberOfRounds} rounds, $matches matches, $max max matches")
    for ((p, n) <- result.result.zipWithIndex) {
      assert(p == createPlayer(n + 1), strategy + " failed")
    }

  }

  def main(args: Array[String]): Unit = {
    val players = (1 to 100000).map(n => createPlayer(n)).toSet
    implicit val tournament = TournamentContext(players, 5)
    implicit val debug = false
    val strategies = List(new MergeMatchOrganizerStrategy)
    //    val strategies = List(new GraphMatchOrganizerStrategy)
    strategies foreach organizeAndTest
  }
}
