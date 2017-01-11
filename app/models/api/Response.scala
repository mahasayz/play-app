package models.api

import play.api.libs.json.{JsArray, Json, Writes}

/**
  * Created by Mahbub on 1/10/2017.
  */
case class Result[T](isSuccess: Boolean, response: Option[T], exception: Throwable, elapse: Long)

case class AirportResult(code: String, country: String, count: Int, runways: List[String] = List.empty)

case class Report(topAirports: Seq[AirportResult], bottomAirports: Seq[AirportResult])

case class QueryRunway(airportIdent: String,
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
                       heDisplacedThresholdFt: Option[Int])

case class QueryAirport(ident: String,
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
                        keywords: Option[String],
                        runways: List[QueryRunway]) {
  def apply(a: Airport): QueryAirport = new QueryAirport(
    a.ident, a.`type`, a.name, a.latitudeDeg, a.longitudeDeg, a.elevationFt, a.continent, a.isoCountry, a.isoRegion, a.municipality, a.scheduledService, a.gpsCode, a.iataCode, a.localCode, a.homeLink,
    a.wikipediaLink, keywords, List.empty
  )
}

case class Query(country: String, aiports: List[QueryAirport])

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

  implicit val runwayWrites = new Writes[QueryRunway] {
    def writes(r: QueryRunway) = Json.obj(
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
      "ident" -> r.ident.removeQuotes,
      "type" -> r.`type`.removeQuotes,
      "name" -> r.name.removeQuotes,
      "latitudeDeg" -> r.latitudeDeg,
      "longitudeDeg" -> r.longitudeDeg,
      "elevationFt" -> r.elevationFt,
      "continent" -> r.continent.getOrElse("").removeQuotes,
      "isoCountry" -> r.isoCountry.removeQuotes,
      "isoRegion" -> r.isoRegion.getOrElse("").removeQuotes,
      "municipality" -> r.municipality.getOrElse("").removeQuotes,
      "scheduledService" -> r.scheduledService.getOrElse("").removeQuotes,
      "gpsCode" -> r.gpsCode.getOrElse("").removeQuotes,
      "iataCode" -> r.iataCode.getOrElse("").removeQuotes,
      "localCode" -> r.localCode.getOrElse("").removeQuotes,
      "homeLink" -> r.homeLink.getOrElse("").removeQuotes,
      "wikipediaLink" -> r.wikipediaLink.getOrElse("").removeQuotes,
      "keywords" -> r.keywords.getOrElse("").removeQuotes,
      "runways" -> JsArray(r.runways.map(Json.toJson(_)))
    )
  }

  implicit val queryQrites = new Writes[Query] {
    def writes(r: Query) = Json.obj(
      "queryString" -> r.country,
      "airports" -> JsArray(r.aiports.map(Json.toJson(_)))
    )
  }
}

object Response {
  def toQueryRunway(r: Runway): QueryRunway = QueryRunway(
    r.airportIdent,
    r.lengthFt,
    r.widthFt,
    r.surface,
    r.lighted,
    r.closed,
    r.leIdent,
    r.leLatitudeDeg,
    r.leLongitudeDeg,
    r.leElevationFt,
    r.leHeadingDegT,
    r.leDisplacedThresholdFt,
    r.heIdent,
    r.heLatitudeDeg,
    r.heLongitudeDeg,
    r.heElevationFt,
    r.heHeadingDegT,
    r.heDisplacedThresholdFt
  )
  def toQueryAirport(a: Airport): QueryAirport = QueryAirport(
    a.ident, a.`type`, a.name, a.latitudeDeg, a.longitudeDeg, a.elevationFt, a.continent, a.isoCountry, a.isoRegion, a.municipality, a.scheduledService, a.gpsCode, a.iataCode, a.localCode, a.homeLink,
    a.wikipediaLink, a.keywords, List.empty
  )
}