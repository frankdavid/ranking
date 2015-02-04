package hu.frankdavid.ranking.util

/**
 * source: http://stackoverflow.com/a/8878078/1107109
 */
case class RingBuffer[A](data: IndexedSeq[A], index: Int = 0) extends IndexedSeq[A] {
  def shiftLeft = new RingBuffer(data, (index + 1) % data.size)

  def shiftRight = new RingBuffer(data, (index + data.size - 1) % data.size)

  def length = data.length

  def apply(i: Int) = data((index + i) % data.size)
}
