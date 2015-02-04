package hu.frankdavid.ranking.gui.configs

import hu.frankdavid.ranking.TournamentStrategy
import hu.frankdavid.ranking.gui.StrategyConfigurationEditor
import hu.frankdavid.ranking.strategy._
import hu.frankdavid.ranking.strategy.util.IntJsFunction

import scalafx.scene.control.{Label, TextField}

class SwissConfigurationEditor extends StrategyConfigurationEditor {
  private val numberOfRoundsScriptField = new TextField

  var loaded: SwissStrategy = _


  add(new Label("Number of rounds:"), 0, 0)
  add(numberOfRoundsScriptField, 1, 0)
  add(new JSEditorLabel, 0, 1, 2, 1)

  def load(strategy: TournamentStrategy) = {
    strategy match {
      case s: SwissStrategy =>
        s.maxRoundsFn match {
          case IntJsFunction(script) => numberOfRoundsScriptField.text = script
          case _ => numberOfRoundsScriptField.text = ""
        }
        loaded = s
        true
      case _ => false
    }
  }

  def strategy = {
    loaded match {
      case _: TraditionalSwissStrategy => TraditionalSwissStrategy(IntJsFunction(numberOfRoundsScriptField.text()))
      case _: MarkovChainStrategy => MarkovChainStrategy(IntJsFunction(numberOfRoundsScriptField.text()))
      case _: ColleyStrategy => ColleyStrategy(IntJsFunction(numberOfRoundsScriptField.text()))
    }
  }
}
