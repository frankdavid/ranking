package hu.frankdavid.ranking.util

class ListMultiMap[K, V] private(val underlying: Map[K, List[V]]) extends Map[K, List[V]] {

  def this() = this(Map().withDefaultValue(List()))

  def &(k: K, v: V) = new ListMultiMap(underlying.updated(k, v :: underlying(k)))

  def get(key: K) = Some(underlying(key))

  def ++(k: K, vs: Iterable[V]) = new ListMultiMap(underlying.updated(k, underlying(k) ++ vs))

  def +[B1 >: List[V]](kv: (K, B1)) = new ListMultiMap(underlying + kv.asInstanceOf[(K, List[V])])

  def iterator = underlying.iterator

  def -(key: K) = new ListMultiMap[K, V](underlying - key)
}

object ListMultiMap {
  def empty[K, V] = new ListMultiMap[K, V](Map().withDefaultValue(Nil))
}


