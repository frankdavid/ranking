package hu.frankdavid.ranking.workbench

import hu.frankdavid.ranking._
import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.random.{RandomGenerator, Well19937c}
import org.apache.commons.math3.util.Precision

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Random

case class TestRunner(numberOfPlayers: Int,
                      awardedPlayers: Int,
                      maxParallelism: Int,
                      playerPerformanceDeviation: Double,
                      resultPredictionDeviation: Double,
                      private val randomGenerator: RandomGenerator = new Well19937c()) {

  private val playerSkillDistribution = new NormalDistribution(randomGenerator, 0, 1)
  private val predictionDistribution = new NormalDistribution(randomGenerator, 0, resultPredictionDeviation max
    Precision.EPSILON)

  private lazy val players: (Seq[Player], Seq[Player]) = {
    val strengths = (0 until numberOfPlayers).map(_ => playerSkillDistribution.sample()).sorted.reverse
    val players = strengths.zipWithIndex.map {
      case (strength, i) =>
        val skill = new NormalDistribution(randomGenerator, strength, playerPerformanceDeviation max Precision.EPSILON)
        (Player(i.toString, skill), strength + predictionDistribution.sample())
    }
    (players.map(_._1), players.sortBy(_._2).map(_._1).reverse)
  }

  def run(strategy: TournamentStrategy): SingleTestResult = {
    val initialContext = TournamentContext(players._2, awardedPlayers, Random.nextLong(), maxParallelism, 0)
    try {
      runIteratively(strategy, initialContext, Map(), players._1)
    } catch {
      case e: Throwable => e.printStackTrace(); copy().run(strategy)
    }
  }

  def runMany(strategy: TournamentStrategy, times: Int): TestResultLike = {
    if (times == 1) {
      run(strategy)
    } else {
      val results = (0 until times).map(_ => copy().run(strategy))
      new AverageTestResult(results)
    }
  }

  @tailrec
  private def runIteratively(strategy: TournamentStrategy, context: TournamentContext,
                             playerGames: scala.collection.Map[Player, Int] = Map(),
                             expectedResults: Seq[Player]): SingleTestResult = {
    val matchesOrResult = strategy.matchesOrResult(context)
    matchesOrResult match {
      case Matches(matches) =>
        val updatedPlayerGames = new mutable.HashMap[Player, Int]().withDefaultValue(0) ++ playerGames
        matches.foreach { m =>
          updatedPlayerGames.update(m.player1, updatedPlayerGames(m.player1) + 1)
          updatedPlayerGames.update(m.player2, updatedPlayerGames(m.player2) + 1)
        }
        val games = matches.toList.map(organizeGame)
        val updatedContext = context.copy(round = context.round + 1).withGames(games)
        runIteratively(strategy, updatedContext, updatedPlayerGames, expectedResults)
      case Result(results) =>
        SingleTestResult(strategy, results, expectedResults, context.round, playerGames)
    }
  }

  private def organizeGame(matchup: MatchUp): Game = {
    val perf1 = matchup.player1.randomPerformance
    val perf2 = matchup.player2.randomPerformance
    val epsilon = 0.1
    if (matchup.enableDraw && math.abs(perf1 - perf2) < epsilon) {
      val avg = (perf1 + perf2) / 2
      Game(matchup, avg, avg)
    } else {
      Game(matchup, perf1, perf2)
    }
  }
}
