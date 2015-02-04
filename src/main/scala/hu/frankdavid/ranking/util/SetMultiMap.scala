package hu.frankdavid.ranking.util

class SetMultiMap[K, V](val underlying: Map[K, Set[V]]) extends Map[K, Set[V]] {

  def containsEntry(key: K, value: V) = underlying.getOrElse(key, Set()).contains(value)

  def this() = this(Map().withDefaultValue(Set()))

  def &(k: K, v: V) = new SetMultiMap(underlying.updated(k, underlying(k) + v))

  def get(key: K) = Some(underlying(key))

  def ++(k: K, vs: Iterable[V]) = new SetMultiMap(underlying.updated(k, underlying(k) ++ vs))

  def +[B1 >: Set[V]](kv: (K, B1)) = underlying + kv

  def iterator = underlying.iterator

  def -(key: K) = new SetMultiMap[K, V](underlying - key)
}

object SetMultiMap {
  def empty[K, V] = new SetMultiMap[K, V](Map().withDefaultValue(Set()))
}
