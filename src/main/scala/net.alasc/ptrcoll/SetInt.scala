package net.alasc.ptrcoll

/** Stupid implementation of a set of integers, delegating everything to `Set[Int]`
  * in the most inefficient way possible.
  * 
  * Will be reimplemented as a fast bitset.
  */
case class SetInt(set: Set[Int]) extends PointableAtImpl[Int] {
  def pointer: Ptr = if (set.isEmpty) Ptr(-1L) else Ptr(set.min.toLong)

  def next(ptr: Long) = {
    val rest = set.filter(_ > ptr.toInt)
    if (rest.isEmpty) -1L else rest.min.toLong
  }
  def hasAt(ptr: Long) = ptr != -1
  def at(ptr: Long) = if (ptr == -1) Iterator.empty.next else ptr.toInt
}