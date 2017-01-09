package models.api

import play.api.libs.json.{JsArray, Json, Writes}

/**
  * Created by Mahbub on 1/10/2017.
  */
case class Result[T](isSuccess: Boolean, response: Option[T], exception: Throwable, elapse: Long)

case class AirportResult(code: String, country: String, count: Int)

case class Report(topAirports: Seq[AirportResult], bottomAirports: Seq[AirportResult])

trait ResultFormatter {
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
      "isoCountry" -> r.code,
      "country" -> r.country,
      "count" -> r.count
    )
  }

  implicit val reportWrites = new Writes[Report] {
    def writes(r: Report) = Json.obj(
      "topAirports" -> JsArray(r.topAirports.map(Json.toJson(_))),
      "bottomAirports" -> JsArray(r.bottomAirports.map(Json.toJson(_)))
    )
  }
}
