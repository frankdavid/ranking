package hu.frankdavid.ranking.gui

import javafx.event.EventHandler
import javafx.scene.control.ButtonBar.ButtonData
import javafx.scene.control.{ButtonType, Dialog, DialogEvent}

import hu.frankdavid.ranking.TournamentStrategy
import hu.frankdavid.ranking.gui.configs._

import scalafx.Includes._
import scalafx.application.Platform
import scalafx.geometry.Insets
import scalafx.scene.control.ComboBox
import scalafx.scene.layout.{GridPane, Pane, Priority}
import scalafx.util.StringConverter

class StrategyDialog extends Dialog[TournamentStrategy] {

  def this(strategy: TournamentStrategy) = {
    this()
    load(strategy)
  }

  val Width = 400
  val Height = 300

  val ConfigurationEditors = Seq(
    new RoundRobinConfigurationEditor, new OneByOneConfigurationEditor, new TimSortConfigurationEditor,
    new HeapSortConfigurationEditor,  new SwissConfigurationEditor, new EmptyConfigurationEditor
  )

  private val content = new GridPane {
    padding = Insets(10)
    vgap = 10
    hgap = 10
    hgrow = Priority.Always
    maxWidth = Width
    prefWidth = Width
  }
  getDialogPane.setContent(content)

  onShownProperty()() = new EventHandler[DialogEvent] {
    def handle(event: DialogEvent) = Platform.runLater {
      getDialogPane.getScene.getWindow.sizeToScene()
    }
  }


  private val configPane = new Pane() {hgrow = Priority.Always}

  private var configEditor: StrategyConfigurationEditor = new EmptyConfigurationEditor

  def strategyConfigPanel(strategy: TournamentStrategy): StrategyConfigurationEditor = {
    ConfigurationEditors.find(_.load(strategy)).getOrElse(new EmptyConfigurationEditor)
  }

  def load(strategy: TournamentStrategy) = {
    if (strategy != null) {
      strategySelectorBox.value() = strategy
    }
  }

  content.add(configPane, 0, 1)

  val distinctTypeOfStrategies = DefaultStrategies.groupBy(_.getClass).values.map(_.head).toSeq.sortBy(_.name)
  private val strategySelectorBox = new ComboBox[TournamentStrategy](distinctTypeOfStrategies) {
    prefWidth = Width
    converter = new StringConverter[TournamentStrategy] {
      def fromString(string: String) = ???
      def toString(t: TournamentStrategy) = t.typeName
    }
    value.onChange { (_, _, value) =>
      configEditor = strategyConfigPanel(value)
      configPane.content = configEditor
      Platform.runLater {
        getDialogPane.getScene.getWindow.sizeToScene()
      }
    }
    selectionModel().selectFirst()
    hgrow = Priority.Always
  }
  content.add(strategySelectorBox, 0, 0)

  private val okButton = new ButtonType("OK", ButtonData.OK_DONE)

  private val cancelButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE)
  getDialogPane.getButtonTypes += okButton
  getDialogPane.getButtonTypes += cancelButton

  setResultConverter { x: ButtonType =>
    x match {
      case `okButton` => configEditor.strategy
      case _ => null
    }
  }


}
