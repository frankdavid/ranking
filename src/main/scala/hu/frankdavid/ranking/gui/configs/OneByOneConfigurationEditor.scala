package hu.frankdavid.ranking.gui.configs

import hu.frankdavid.ranking.TournamentStrategy
import hu.frankdavid.ranking.gui.StrategyConfigurationEditor
import hu.frankdavid.ranking.strategy._

import scalafx.scene.control.{ComboBox, Label}

class OneByOneConfigurationEditor extends StrategyConfigurationEditor {
  val underlyingCombobox = new ComboBox[TournamentStrategy](Seq(GreedyGraphStrategy, MergeStrategy))

  add(new Label("Underlying strategy:"), 0, 0)
  add(underlyingCombobox, 1, 0)

  def load(strategy: TournamentStrategy) = {
    strategy match {
      case OneByOneStrategy(underlying) =>
        underlyingCombobox.selectionModel().select(underlying)
        true
      case _ => false
    }
  }

  def strategy = OneByOneStrategy(underlyingCombobox.value())
}
