package hu.frankdavid.ranking.gui.configs

import hu.frankdavid.ranking.TournamentStrategy
import hu.frankdavid.ranking.gui.StrategyConfigurationEditor
import hu.frankdavid.ranking.strategy._
import hu.frankdavid.ranking.strategy.util.IntJsFunction

import scalafx.scene.control.{Label, TextField}

class HeapSortConfigurationEditor extends StrategyConfigurationEditor {
  private val guessAheadScriptField = new TextField

  add(new Label("Guess ahead:"), 0, 0)
  add(guessAheadScriptField, 1, 0)
  add(new JSEditorLabel, 0, 1, 2, 1)

  def load(strategy: TournamentStrategy) = {
    strategy match {
      case HeapStrategy(IntJsFunction(guessAheadScript)) =>
        guessAheadScriptField.text = guessAheadScript
        true
      case _ => false
    }
  }

  def strategy = HeapStrategy(IntJsFunction(guessAheadScriptField.text()))
}
