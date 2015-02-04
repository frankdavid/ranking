package hu.frankdavid.ranking.gui.configs

import hu.frankdavid.ranking.TournamentStrategy
import hu.frankdavid.ranking.gui.StrategyConfigurationEditor
import hu.frankdavid.ranking.strategy._

class EmptyConfigurationEditor extends StrategyConfigurationEditor {

  var loaded: TournamentStrategy = _

  def load(strategy: TournamentStrategy) = {
    loaded = strategy
    true
  }

  def strategy = loaded
}
