package services

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import models.api.Runway
import play.api.db.DBApi
import utils.CSVConverter

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Mahbub on 1/8/2017.
  */

class RunwayService @Inject() (dBApi: DBApi) extends DBService[Runway](dbApi = dBApi) {

  def insert(runway: Runway) = {
    db.withConnection { implicit connection =>
      SQL(
        """
          insert into runway values (
            {id}, {airRef}, {airIdent}, {length}, {width}, {surface}, {lighted}, {closed}, {leIdent}, {leLat}, {leLong}, {leElevation}, {leHeading}, {leDisp}, {heIdent}, {heLat}, {heLong}, {heElevation}, {heHeading}, {heDisp}
          )
        """
      ).on(
        'id -> runway.id,
        'airRef -> runway.airportRef,
        'airIdent -> runway.airportIdent,
        'length -> runway.lengthFt,
        'width -> runway.widthFt,
        'surface -> runway.surface,
        'lighted -> runway.lighted,
        'closed -> runway.closed,
        'leIdent -> runway.leIdent,
        'leLat -> runway.leLatitudeDeg,
        'leLong -> runway.leLongitudeDeg,
        'leElevation -> runway.leElevationFt,
        'leHeading -> runway.leHeadingDegT,
        'leDisp -> runway.leDisplacedThresholdFt,
        'heIdent -> runway.heIdent,
        'heLat -> runway.heLatitudeDeg,
        'heLong -> runway.heLongitudeDeg,
        'heElevation -> runway.heElevationFt,
        'heHeading -> runway.heHeadingDegT,
        'heDisp -> runway.heDisplacedThresholdFt
      ).executeUpdate()
    }
  }

  val simple = {
    get[String]("runway.surface")
  }

  def runwaysByCountry(country: String) = {
    db.withConnection { implicit connection =>
      val runways = SQL(
        s"""
           |select distinct surface from runway
           |inner join airport on runway.airportRef = airport.id
           |inner join country on airport.isoCountry = country.code
           |where country.code like {country} or country.name like {country}
         """.stripMargin
      ).on(
        'country -> country
      ).as(simple *)

      runways
    }
  }

//  override protected val fsPath: String = "C:\\Users\\Mahbub\\Documents\\csv\\runways.csv"
  override protected val fsPath: String = "/Users/malam/dev/Learn/Scala/random-repo/src/test/resources/runways.csv"

  def task(list: List[String]) = Future {
    list.map(line => {
      val input = line.split(",").reduce((r, s) => {
        CSVConverter.countOccurrence(r) match {
          case c: Int if c % 2 == 1 => r ++ "~" ++ s
          case _ => r ++ "," ++ s
        }
      })
      val res = CSVConverter[Runway].from(input)
      res match {
        case Success(s) => insert(s)
        case Failure(e) =>
          println(s"Exception : $e, line : $line")
      }
    })
  }
}
