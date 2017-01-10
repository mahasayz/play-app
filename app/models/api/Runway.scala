package models.api

import anorm.SqlParser._
import anorm._

/**
  * Created by Mahbub on 1/8/2017.
  */
case class Runway(id: Option[Long],
                  airportRef: Long,
                  airportIdent: String,
                  lengthFt: Int,
                  widthFt: Int,
                  surface: String,
                  lighted: Boolean,
                  closed: Boolean,
                  leIdent: Option[String],
                  leLatitudeDeg: Option[Double],
                  leLongitudeDeg: Option[Double],
                  leElevationFt: Option[Int],
                  leHeadingDegT: Option[Double],
                  leDisplacedThresholdFt: Option[Int],
                  heIdent: Option[String],
                  heLatitudeDeg: Option[Double],
                  heLongitudeDeg: Option[Double],
                  heElevationFt: Option[Int],
                  heHeadingDegT: Option[Double],
                  heDisplacedThresholdFt: Option[Int]) extends DBModel

object Runway {
  val runwayParser = {
    get[Option[Long]]("runway.id") ~
      get[Long]("runway.airportRef") ~
      get[String]("runway.airportIdent") ~
      get[Int]("runway.lengthFt") ~
      get[Int]("runway.widthFt") ~
      get[String]("runway.surface") ~
      get[Boolean]("runway.lighted") ~
      get[Boolean]("runway.closed") ~
      get[Option[String]]("runway.leIdent") ~
      get[Option[Double]]("runway.leLatitudeDeg") ~
      get[Option[Double]]("runway.leLongitudeDeg") ~
      get[Option[Int]]("runway.leElevationFt") ~
      get[Option[Double]]("runway.leHeadingDegT") ~
      get[Option[Int]]("runway.leDisplacedThresholdFt") ~
      get[Option[String]]("runway.heIdent") ~
      get[Option[Double]]("runway.heLatitudeDeg") ~
      get[Option[Double]]("runway.heLongitudeDeg") ~
      get[Option[Int]]("runway.heElevationFt") ~
      get[Option[Double]]("runway.heHeadingDegT") ~
      get[Option[Int]]("runway.heDisplacedThresholdFt") map {
      case id ~ airportRef ~ airIdent ~ length ~ width ~ surface ~ lighted ~ closed ~ leIdent ~ leLat ~ leLong ~ leElevation ~ leHeading ~ leDisp ~ heIdent ~ heLat ~ heLong ~ heElevation ~ heHeading ~ heDisp =>
        Runway(id, airportRef, airIdent, length, width, surface, lighted, closed, leIdent, leLat, leLong, leElevation, leHeading, leDisp, heIdent, heLat, heLong, heElevation, heHeading, heDisp)
    }
  }
}