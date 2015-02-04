package hu.frankdavid.ranking.workbench

class AverageTestResult(testResults: Seq[SingleTestResult]) extends TestResultLike {
  private val sizeAsDouble = testResults.length.toDouble
  val strategy = testResults.head.strategy
  lazy val numberOfGames: java.lang.Double = testResults.map(_.numberOfGames.toInt).sum / sizeAsDouble
  lazy val maxNumberOfGamesPerPlayer: java.lang.Double = testResults.map(_.maxNumberOfGamesPerPlayer.toInt).sum / sizeAsDouble
  lazy val resultDistance: java.lang.Double = testResults.map(_.resultDistance.toDouble).sum / sizeAsDouble
  lazy val numberOfRounds: java.lang.Double = testResults.map(_.numberOfRounds.toInt).sum / sizeAsDouble
  def placeGuessedCorrectly(place: Int) = testResults.count(_.isPlaceGuessedCorrectly(place))

  def followPlace(place: Int): Map[Int, Int] = testResults.map { singleResult =>
    val player = singleResult.expectedResult(place)
    singleResult.result.indexOf(player)
  }.groupBy(identity).mapValues(_.size).withDefaultValue(0)
}
