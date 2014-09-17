//package hu.frankdavid.pingpong
//
//import java.util.Collections
//
//import scala.collection.{JavaConversions, mutable}
//import scala.collection.mutable.ListBuffer
//import scala.util.Random
//
//object SorterTest extends App {
//  var players = new ListBuffer[Player]()
//  val NumberOfPlayers: Int = 25
//  val Resolution = 5
//
//  for(i <- 0 until NumberOfPlayers by Resolution) {
//    players ++= gen(i, i + Resolution)
//  }
//
//  def gen(from: Int, to: Int): ListBuffer[Player] = {
//    val res = new ListBuffer[Player]
//    for(i <- from until to) {
//      players += Player("Player " + i, i)
//    }
//    Random.shuffle(players)
//  }
//
////  val ordering = new Ordering[Player]() {
////    def compare(x: Player, y: Player) = {
////      numberOfGames.update(x, numberOfGames.getOrElse(x, 0) + 1)
////      numberOfGames.update(y, numberOfGames.getOrElse(y, 0) + 1)
////      x.skill.compareTo(y.skill)
////    }
////  }
//  val ordering = new CountingOrdering()
////  val queue = new mutable.PriorityQueue[Player]()(ordering)
////  queue ++= players
////  queue.dequeue()
////  queue.dequeue()
////  queue.dequeue()
////  queue.dequeue()
////  queue.dequeue()
////  queue.dequeue()
////  queue.dequeue()
//  Collections.sort(new java.util.ArrayList(JavaConversions.seqAsJavaList(players)), ordering)
////  players.remove(players.indexOf(players.max(ordering)))
////  players.remove(players.indexOf(players.max(ordering)))
////  players.remove(players.indexOf(players.max(ordering)))
//
//  val sorted = ordering.numberOfGames.toSeq.sortBy(_._2)
//  for(s <- sorted) {
//    println(s"${s._1}:\t${s._2} games")
//  }
//
//  ordering.matches.foreach(println)
//
//}
//
//class CountingOrdering extends Ordering[Player] {
//  val matches = new mutable.LinkedHashSet[Set[Player]]()
//  val numberOfGames = new mutable.HashMap[Player, Int]()
//
//  def compare(x: Player, y: Player) = {
//    val game = Set(x, y)
//    if(!matches.contains(game)) {
//      numberOfGames.update(x, numberOfGames.getOrElse(x, 0) + 1)
//      numberOfGames.update(y, numberOfGames.getOrElse(y, 0) + 1)
//      matches += game
//    }
//    x.skill.compareTo(y.skill)
//  }
//}
//
//case class Player(name: String, skill: Int) {
//  override def toString = s"$name"
//}