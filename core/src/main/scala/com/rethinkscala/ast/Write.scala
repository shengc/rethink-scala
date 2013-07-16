package com.rethinkscala.ast

import ql2.Term.TermType
import com.rethinkscala.reflect.Reflector
import com.rethinkscala.net.{ChangeResult, Document, InsertResult}

case class Insert[T <: Document](table: Table[T], records: Either[Seq[Map[String, Any]], Seq[T]],
                                 upsert: Option[Boolean] = None, durability: Option[Durability.Kind] = None, returnValues: Option[Boolean] = None)
  extends ProduceDocument[InsertResult] {

  override lazy val args = buildArgs(table, records match {
    case Left(x: Seq[Map[String, Any]]) => x
    case Right(x: Seq[T]) => {
      val r = x.map(Reflector.toMap(_))
      //
      if (x.size > 1) r else r(0)
    }
  })
  override lazy val optargs = buildOptArgs(Map("upsert" -> upsert, "durability" -> durability, "return_vals" -> returnValues))

  def withResults = Insert[T](table, records, upsert, durability, Some(true))

  def termType = TermType.INSERT

}

case class Update(target: Selection, data: Either[Map[String, Any], Predicate],
                  durability: Option[Durability.Kind] = None, nonAtomic: Option[Boolean] = None)
  extends ProduceDocument[ChangeResult] {

  override lazy val args = buildArgs(target, data match {
    case Left(x: Map[String, Any]) => x
    case Right(x: Predicate) => x()
  })
  override lazy val optargs = buildOptArgs(Map("non_atomic" -> nonAtomic, "durability" -> durability))

  def termType = TermType.UPDATE
}

case class Replace(target: Selection, data: Either[Map[String, Any], Predicate1],
                   durability: Option[Durability.Kind] = None, nonAtomic: Option[Boolean] = None)
  extends ProduceDocument[ChangeResult] {

  override lazy val args = buildArgs(target, data match {
    case Left(x: Map[String, Any]) => x
    case Right(x: Predicate1) => x()
  })
  override lazy val optargs = buildOptArgs(Map("non_atomic" -> nonAtomic, "durability" -> durability))

  def termType = TermType.REPLACE

}

case class Delete(target: Selection, durability: Option[Durability.Kind] = None) extends ProduceDocument[ChangeResult] {


  override lazy val args = buildArgs(target)
  override lazy val optargs = buildOptArgs(Map("durability" -> durability))

  def termType = TermType.DELETE
}