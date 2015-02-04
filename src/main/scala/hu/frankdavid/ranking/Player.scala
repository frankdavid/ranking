package hu.frankdavid.ranking

import org.apache.commons.math3.distribution.{RealDistribution, UniformRealDistribution}

case class Player(name: String, skill: RealDistribution = new UniformRealDistribution(0, 0.1)) extends Ordered[Player] {
  def compare(that: Player): Int = name.compareTo(that.name)

  def -(other: Player): MatchUp = MatchUp(this, other)

  def randomPerformance = skill.sample()

  override def toString = s"Player($name)"
}
