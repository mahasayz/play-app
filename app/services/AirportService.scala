package services

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import models.api.Airport
import play.api.db.DBApi
import utils.CSVConverter

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Mahbub on 1/8/2017.
  */

case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long)

class AirportService @Inject() (dBApi: DBApi) extends DBService[Airport](dbApi = dBApi) {

  def insert(airport: Airport) = {
    db.withConnection { implicit connection =>
      SQL(
        """
          insert into airport values (
            {id}, {ident}, {type}, {name}, {lat}, {long}, {elevation}, {continent}, {country}, {region}, {municipality}, {scheduledService}, {gps}, {iata}, {local}, {homeLink}, {wiki}, {keyword}
          )
        """
      ).on(
        'id -> airport.id,
        'ident -> airport.ident,
        'type -> airport.`type`,
        'name -> airport.name,
        'lat -> airport.latitudeDeg,
        'long -> airport.longitudeDeg,
        'elevation -> airport.elevationFt,
        'continent -> airport.continent,
        'country -> airport.isoCountry,
        'region -> airport.isoRegion,
        'municipality -> airport.municipality,
        'scheduledService -> airport.scheduledService,
        'gps -> airport.gpsCode,
        'iata -> airport.iataCode,
        'local -> airport.localCode,
        'homeLink -> airport.homeLink,
        'wiki -> airport.wikipediaLink,
        'keyword -> airport.keywords
      ).executeUpdate()
    }
  }

  /*def insert[T <: Runway](runway: T) = {
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
  }*/

  val simpleCount = {
    get[String]("airport.isoCountry") ~
    get[String]("country.name") ~
    get[Int]("airportCount") map {
      case code ~ name ~ count => (code, name, count)
    }
  }

  def fetchNAirports(offset: Int = 0, limit: Int = 10, orderBy: Int = 1, order: String = "asc", filter: String = "%"): Page[(String, String, Int)] = {

    db.withConnection { implicit connection =>

      val airports = SQL(
        s"""
          select airport.isoCountry, country.name, count(*) as airportCount from airport
          left join country on airport.isoCountry = country.code
          where airport.name like {filter}
          group by airport.isoCountry
          order by {orderBy} $order
          limit {limit} offset {offset}
        """
      ).on(
        'limit -> limit,
        'offset -> offset,
        'filter -> filter,
        'orderBy -> orderBy
      ).as(simpleCount *)

      val totalRows = SQL(
        """
          select count(*) from airport
          left join country on airport.isoCountry = country.code
          where airport.name like {filter}
        """
      ).on(
        'filter -> filter
      ).as(scalar[Long].single)

      Page(airports, 0, offset, totalRows)

    }

  }

  override protected val fsPath: String = "C:\\Users\\Mahbub\\Documents\\csv\\airports.csv"

  def task(list: List[String]) = Future {
    list.map(line => {
      val input = line.split(",").reduce((r, s) => {
        CSVConverter.countOccurrence(r) match {
          case c: Int if c % 2 == 1 => r ++ "~" ++ s
          case _ => r ++ "," ++ s
        }
      })
      val res = CSVConverter[Airport].from(input)
      res match {
        case Success(s) => insert(s)
        case Failure(e) =>
          println(s"Exception : $e, line : $line")
      }
    })
  }
}
