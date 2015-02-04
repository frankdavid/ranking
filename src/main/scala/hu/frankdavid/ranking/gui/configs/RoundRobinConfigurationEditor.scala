package hu.frankdavid.ranking.gui.configs

import hu.frankdavid.ranking.TournamentStrategy
import hu.frankdavid.ranking.gui.StrategyConfigurationEditor
import hu.frankdavid.ranking.strategy.RoundRobinStrategy
import myjavafx.spinner.NumberSpinner

import scalafx.Includes._
import scalafx.scene.control.Label

class RoundRobinConfigurationEditor extends StrategyConfigurationEditor {
  add(new Label("Rounds:"), 0, 0)
  val roundsSpinner = new NumberSpinner()
  add(roundsSpinner, 1, 0)
  add(new Label("Points for win:"), 0, 1)
  val winSpinner = new NumberSpinner()
  add(winSpinner, 1, 1)
  add(new Label("Points for draw:"), 0, 2)
  val drawSpinner = new NumberSpinner()
  add(drawSpinner, 1, 2)
  add(new Label("Points for lose:"), 0, 3)
  val loseSpinner = new NumberSpinner()
  add(loseSpinner, 1, 3)

  List(roundsSpinner, winSpinner, drawSpinner, loseSpinner).foreach(_.maxWidth_=(100))

  def load(strategy: TournamentStrategy) = {
    strategy match {
      case RoundRobinStrategy(rounds, win, draw, lose) =>
        roundsSpinner.setValue(rounds)
        winSpinner.setValue(win)
        drawSpinner.setValue(draw)
        loseSpinner.setValue(lose)
        true
      case _ => false
    }
  }

  def strategy = RoundRobinStrategy(
    roundsSpinner.getValue.intValue(),
    winSpinner.getValue.intValue(),
    drawSpinner.getValue.intValue(),
    loseSpinner.getValue.intValue()
  )
}
