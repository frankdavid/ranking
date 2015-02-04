package hu.frankdavid.ranking.workbench

import hu.frankdavid.ranking.{TournamentStrategy, Player}

case class SingleTestResult(strategy: TournamentStrategy,
                            result: Seq[Player],
                            expectedResult: Seq[Player],
                            numberOfRounds: java.lang.Integer,
                            games: scala.collection.Map[Player, Int]) extends TestResultLike {

  lazy val numberOfGames: java.lang.Integer = games.map(_._2).sum / 2
  lazy val maxNumberOfGamesPerPlayer: java.lang.Integer = games.map(_._2).max
  lazy val isSortedCorrectly = result.zip(expectedResult).forall(s => s._1 == s._2)

  def isPlaceGuessedCorrectly(place: Int) =
    expectedResult.size > place && result.size > place && expectedResult(place) == result(place)

  def placeGuessedCorrectly(place: Int) = if (isPlaceGuessedCorrectly(place)) 1 else 0

  def followPlace(place: Int) = {
    val player = expectedResult(place)
    Map(result.indexOf(player) -> 1)
  }

  lazy val resultDistance: java.lang.Double = {
    val expected = expectedResult.zipWithIndex.toMap
    result.zipWithIndex.map { case (player, i) => math.pow(expected(player) - i, 2)}.sum
  }

}
