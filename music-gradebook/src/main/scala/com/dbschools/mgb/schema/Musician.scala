package com.dbschools.mgb.schema

import net.liftweb.record.{MetaRecord, Record}
import net.liftweb.squerylrecord.KeyedRecord
import org.squeryl.annotations._
import net.liftweb.record.field.{StringField, IntField}

case class Musician private() extends Record[Musician] with KeyedRecord[Int]{
  override def meta = Musician

  @Column("musician_id")
  override val idField = new IntField(this) {
    override def shouldDisplay_? = false
  }

  def musician_id = idField

  @Column("student_id")
  val student_id = new IntField(this) {
    override def displayName = "Student ID"
  }

  @Column("first_name")
  val first_name = new StringField(this, "") {
    override def displayName = "First Name"
    override def validations = valMinLen(1, "May not be blank") _ :: super.validations
  }

  @Column("last_name")
  val last_name = new StringField(this, "") {
    override def displayName = "Last Name"
    override def validations = valMinLen(1, "May not be blank") _ :: super.validations
  }

  @Column("graduation_year")
  val graduation_year = new IntField(this) {
    override def displayName = "Graduation Year"
  }

  val sex = new StringField(this, "") {
    override def displayName = "Sex"
  }

  def name = last_name.is + ", " + first_name.is
}

object Musician extends Musician with MetaRecord[Musician] {
  override def fieldOrder = List(student_id, first_name, last_name, graduation_year, sex)
}
