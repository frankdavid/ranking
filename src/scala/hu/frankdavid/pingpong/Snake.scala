package hu.frankdavid.pingpong

import scala.annotation.tailrec

object Snake {
  def main(args: Array[String]) {
    drawPath(Point(2, 2), Point(2, 5), Dir.None, List()).reverse.map(println)
  }

  @tailrec
  def drawPath(from: Point, to: Point, lastDir: Dir.Dir, acc: List[Point]): List[Point] = {
    if (from != to) {
      val dir = lastDir match {
        case Dir.Vertical => Dir.Horizontal
        case Dir.Horizontal => Dir.Vertical
        case _ => if (from.x != to.x) Dir.Horizontal else Dir.Vertical
      }
      if (dir == Dir.Horizontal) {
        val next = if (to.x > from.x) Point(from.x + 1, from.y) else Point(from.x - 1, from.y)
        drawPath(next, to, Dir.Horizontal, from :: acc)
      } else {
        val next = if (to.y > from.y) Point(from.x, from.y + 1) else Point(from.x, from.y - 1)
        drawPath(next, to, Dir.Vertical, from :: acc)
      }
    } else {
      from :: acc
    }
  }

  def printAt(char: Char, point: Point): Unit = {
    print("%c[%d;%df".format(0x1B, point.y, point.x))
    print(char)
  }

}

case class Point(x: Int, y: Int)

object Dir extends Enumeration {
  type Dir = Value
  val Vertical, Horizontal, None = Value
}


object Test extends App {
  //  def readDigit(int: Int, position: Int): Int = {
  //
  //  }
  //
  //  def writeDigit(int: Int, position: Int, digit: Int): Int = {
  //
  //  }
  //
  //  def swapDigits(int: Int, p1: Int, p2: Int): Int = {
  //    val d1 = readDigit(int, p1)
  //    val d2 = readDigit(int, p2)
  //    writeDigit(writeDigit(int, p2, d1), p1, d2)
  //  }
  //  def reverseEven(int: Int): Int = {
  //    def reverseEven(int: Int, fromPos: Int) = {
  //
  //    }
  //    reverseEven(int, 0)
  //  }

  println(osszekevert(12, 2))

  def osszekevert(int: Int, radix: Int): Int = {
    val s = BigInt(int).toString(radix)
    val reversed = s.zipWithIndex.map {
      case (c, pos) if pos % 2 == 1 =>
        if (s.length % 2 == 0) s.charAt(s.length - pos)
        else s.charAt(s.length - pos - 1)
      case (c, pos) => c
    }.mkString
    BigInt(reversed, radix).toInt
  }
}