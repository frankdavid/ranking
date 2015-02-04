package hu.frankdavid.ranking.gui.util

import javafx.scene.control.ListCell


class ConverterListCell[T](converter: T => String) extends ListCell[T] {
  override def updateItem(item: T, empty: Boolean) = {
    super.updateItem(item, empty)
    setText(if (empty) "" else converter(item))
  }
}
