package com.dbschools.mgb.schema

import org.squeryl.annotations.Column

case class Tempo(
  @Column("tempo_id")       id:           Int,
                            tempo:        Int,
  @Column("piece_id")       pieceId:      Int,
  @Column("instrument_id")  instrumentId: Option[Int]
)
