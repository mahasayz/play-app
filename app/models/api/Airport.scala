package models.api

import anorm.SqlParser._
import anorm._

/**
  * Created by Mahbub on 1/8/2017.
  */
case class Airport(id: Option[Long],
                   ident: String,
                   `type`: String,
                   name: String,
                   latitudeDeg: Option[Double],
                   longitudeDeg: Option[Double],
                   elevationFt: Option[Int],
                   continent: Option[String],
                   isoCountry: String,
                   isoRegion: Option[String],
                   municipality: Option[String],
                   scheduledService: Option[String],
                   gpsCode: Option[String],
                   iataCode: Option[String],
                   localCode: Option[String],
                   homeLink: Option[String],
                   wikipediaLink: Option[String],
                   keywords: Option[String]) extends DBModel

object Airport {
  val airportParser = {
    get[Option[Long]]("airport.id") ~
      get[String]("airport.ident") ~
      get[String]("airport.type") ~
      get[String]("airport.name") ~
      get[Option[Double]]("airport.latitudeDeg") ~
      get[Option[Double]]("airport.longitudeDeg") ~
      get[Option[Int]]("airport.elevationFt") ~
      get[Option[String]]("airport.continent") ~
      get[String]("airport.isoCountry") ~
      get[Option[String]]("airport.isoRegion") ~
      get[Option[String]]("airport.municipality") ~
      get[Option[String]]("airport.scheduledService") ~
      get[Option[String]]("airport.gpsCode") ~
      get[Option[String]]("airport.iataCode") ~
      get[Option[String]]("airport.localCode") ~
      get[Option[String]]("airport.homeLink") ~
      get[Option[String]]("airport.wikipediaLink") ~
      get[Option[String]]("airport.keywords") map {
      case id ~ ident ~ airType ~ name ~ lat ~ long ~ elevation ~ continent ~ country ~ region ~ municipality ~ scheduledService ~ gps ~ iata ~ local ~ homeLink ~ wiki ~ keywords =>
        Airport(id, ident, airType, name, lat, long, elevation, continent, country, region, municipality, scheduledService, gps, iata, local, homeLink, wiki, keywords)
    }
  }
}