package services

import javax.inject.Inject

import anorm.SqlParser._
import anorm._
import models.api.{Airport, AirportResult, Country, Runway}
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
        'ident -> airport.ident.removeQuotes,
        'type -> airport.`type`.removeQuotes,
        'name -> airport.name.removeQuotes,
        'lat -> airport.latitudeDeg,
        'long -> airport.longitudeDeg,
        'elevation -> airport.elevationFt,
        'continent -> airport.continent.getOrElse("").removeQuotes,
        'country -> airport.isoCountry.removeQuotes,
        'region -> airport.isoRegion.getOrElse("").removeQuotes,
        'municipality -> airport.municipality.getOrElse("").removeQuotes,
        'scheduledService -> airport.scheduledService.getOrElse("").removeQuotes,
        'gps -> airport.gpsCode.getOrElse("").removeQuotes,
        'iata -> airport.iataCode.getOrElse("").removeQuotes,
        'local -> airport.localCode.getOrElse("").removeQuotes,
        'homeLink -> airport.homeLink.getOrElse("").removeQuotes,
        'wiki -> airport.wikipediaLink.getOrElse("").removeQuotes,
        'keyword -> airport.keywords.getOrElse("").removeQuotes
      ).executeUpdate()
    }
  }

  val simpleCount = {
    get[String]("airport.isoCountry") ~
    get[String]("country.name") ~
    get[Int]("airportCount") map {
      case code ~ name ~ count => AirportResult(code, name, count)
    }
  }

  def fetchNAirports(offset: Int = 0, limit: Int = 10, orderBy: Int = 1, order: String = "asc", filter: String = "%"): Page[AirportResult] = {

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

//  override protected val fsPath: String = "C:\\Users\\Mahbub\\Documents\\csv\\airports.csv"
  override protected val fsPath: String = "/Users/malam/dev/Learn/Scala/random-repo/src/test/resources/airports.csv"

  val withCountry = Airport.airportParser ~ (Runway.runwayParser ?) ~ (Country.countryParser ?) map {
    case airport~runway~country => (airport, runway, country)
  }

  def findByCountry(country: String): List[(Airport, Option[Runway], Option[Country])] = {
    db.withConnection { implicit connection =>
      val airports = SQL(
        """
          |select * from airport
          |inner join country on airport.isoCountry = country.code
          |left join runway on airport.id = runway.airportRef
          |where country.name like {country}
          |or country.code like {country}
        """.stripMargin
      ).on(
        'country -> country
      ).as(withCountry *)

      airports
    }
  }

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
