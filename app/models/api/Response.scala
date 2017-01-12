package models.api

import play.api.libs.json.{JsArray, Json, Writes}

/**
  * Created by Mahbub on 1/10/2017.
  */
case class Result[T](isSuccess: Boolean, response: Option[T], exception: Throwable, elapse: Long)

case class AirportResult(code: String, country: String, count: Int, runways: List[String] = List.empty)

case class Report(topAirports: Seq[AirportResult], bottomAirports: Seq[AirportResult])

case class QueryAirport(country: String, airport: Airport,
                        runways: List[Runway] = List.empty)

case class Query(country: String, airports: List[QueryAirport])

trait ResultFormatter {

  implicit class StringUtils(s: String) {
    def removeQuotes = s.replaceAll("\"", "")
  }

  implicit def resultWrites[T](implicit fmt: Writes[T]) = new Writes[Result[T]] {
    def writes(r: Result[T]) = Json.obj(
      "isSuccess" -> r.isSuccess,
      "response" -> r.response.map(Json.toJson(_)),
      "exception" -> Option(r.exception).map(_.getMessage),
      "elapse" -> r.elapse
    )
  }

  implicit val airportWrites = new Writes[AirportResult] {
    def writes(r: AirportResult) = Json.obj(
      "isoCountry" -> r.code.removeQuotes,
      "country" -> r.country.removeQuotes,
      "count" -> r.count,
      "runways" -> JsArray(r.runways.map(r => Json.toJson(r.removeQuotes)))
    )
  }

  implicit val reportWrites = new Writes[Report] {
    def writes(r: Report) = Json.obj(
      "topAirports" -> JsArray(r.topAirports.map(Json.toJson(_))),
      "bottomAirports" -> JsArray(r.bottomAirports.map(Json.toJson(_)))
    )
  }

  implicit val runwayWrites = new Writes[Runway] {
    def writes(r: Runway) = Json.obj(
      "airportIdent" -> r.airportIdent.removeQuotes,
      "lengthFt" -> r.lengthFt,
      "widthFt" -> r.widthFt,
      "surface" -> r.surface.removeQuotes,
      "lighted" -> r.lighted,
      "closed" -> r.closed,
      "leIdent" -> r.leIdent.getOrElse("").removeQuotes,
      "leLatitudeDeg" -> r.leLatitudeDeg,
      "leLongitudeDeg" -> r.leLongitudeDeg,
      "leElevationFt" -> r.leElevationFt,
      "leHeadingDegT" -> r.leHeadingDegT,
      "leDisplacedThresholdFt" -> r.leDisplacedThresholdFt,
      "heIdent" -> r.heIdent.getOrElse("").removeQuotes,
      "heLatitudeDeg" -> r.heLatitudeDeg,
      "heLongitudeDeg" -> r.heLongitudeDeg,
      "heElevationFt" -> r.heElevationFt,
      "heHeadingDegT" -> r.heHeadingDegT,
      "heDisplacedThresholdFt" -> r.heDisplacedThresholdFt
    )
  }

  implicit val airportQWrites = new Writes[QueryAirport] {
    def writes(r: QueryAirport) = Json.obj(
      "country" -> r.country,
      "ident" -> r.airport.ident.removeQuotes,
      "type" -> r.airport.`type`.removeQuotes,
      "name" -> r.airport.name.removeQuotes,
      "latitudeDeg" -> r.airport.latitudeDeg,
      "longitudeDeg" -> r.airport.longitudeDeg,
      "elevationFt" -> r.airport.elevationFt,
      "continent" -> r.airport.continent.getOrElse("").removeQuotes,
      "isoCountry" -> r.airport.isoCountry.removeQuotes,
      "isoRegion" -> r.airport.isoRegion.getOrElse("").removeQuotes,
      "municipality" -> r.airport.municipality.getOrElse("").removeQuotes,
      "scheduledService" -> r.airport.scheduledService.getOrElse("").removeQuotes,
      "gpsCode" -> r.airport.gpsCode.getOrElse("").removeQuotes,
      "iataCode" -> r.airport.iataCode.getOrElse("").removeQuotes,
      "localCode" -> r.airport.localCode.getOrElse("").removeQuotes,
      "homeLink" -> r.airport.homeLink.getOrElse("").removeQuotes,
      "wikipediaLink" -> r.airport.wikipediaLink.getOrElse("").removeQuotes,
      "keywords" -> r.airport.keywords.getOrElse("").removeQuotes,
      "runways" -> JsArray(r.runways.map(Json.toJson(_)))
    )
  }

  implicit val queryQrites = new Writes[Query] {
    def writes(r: Query) = Json.obj(
      "queryString" -> r.country,
      "airports" -> JsArray(r.airports.map(Json.toJson(_)))
    )
  }
}