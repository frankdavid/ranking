package hu.frankdavid.ranking.workbench

abstract class TestResultLike {
  val numberOfGames: Number
  val maxNumberOfGamesPerPlayer: Number
  val resultDistance: java.lang.Double
  val numberOfRounds: Number
  def placeGuessedCorrectly(place: Int): Int
  def followPlace(place: Int): Map[Int, Int]
}
