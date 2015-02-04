package hu.frankdavid.ranking.strategy.util

object RichDouble {
  case class Precision(p: Double)

  implicit class DoubleWithAlmostEquals(val d: Double) extends AnyVal {
    def ~=(d2: Double)(implicit p: Precision) = (d - d2).abs < p.p
  }
}
