package hu.frankdavid.ranking.gui

import javafx.collections.ObservableList

import hu.frankdavid.ranking.TournamentStrategy
import hu.frankdavid.ranking.gui.util.ConverterListCell
import hu.frankdavid.ranking.util.Optional2Option._

import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, ListView}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout._


class StrategyPicker extends VBox {

  private val strategyList = new ListView[TournamentStrategy](DefaultStrategies) {
    cellFactory = list => new ConverterListCell[TournamentStrategy]({ x =>
      val index = list.items().indexOf(x) + 1
      s"$index. $x"
    })
  }
  private val addButton = new Button("Add") {onAction = add _}
  private val editButton = new Button("Edit") {onAction = editSelected _}
  private val removeButton = new Button("Remove") {onAction = removeSelected _}
  private val removeAllButton = new Button("Remove All") {onAction = removeAll _}
  content = Seq(
    strategyList,
    new HBox {
      padding = Insets(10)
      spacing = 10
      alignment = Pos.BottomRight
      content = Seq(
        addButton, editButton, removeButton, removeAllButton
      )
    }
  )

  strategyList.onMouseClicked = { (event: MouseEvent) =>
    if (event.clickCount == 2) {
      editSelected()
    }
  }

  def strategies: ObservableList[TournamentStrategy] = {
    strategyList.items()
  }

  val strategyEditor = new StrategyDialog

  def add(): Unit = {
    strategyEditor.showAndWait().foreach { strategy =>
      strategyList.items() += strategy
    }
  }

  def editSelected(): Unit = {
    val selected = strategyList.selectionModel().getSelectedItem
    strategyEditor.load(selected)
    val items = strategyList.items()
    strategyEditor.showAndWait().foreach { strategy =>
      val index = items.indexOf(selected)
      items.remove(index)
      items.add(index, strategy)
      strategyList.selectionModel().select(index)
    }

  }

  def removeSelected(): Unit = {
    val selected = strategyList.selectionModel().getSelectedItem
    strategyList.items().remove(selected)
  }

  def removeAll(): Unit = {
    strategyList.items().clear()
  }

}
