package controllers

import javax.inject.Inject

import models.api._
import play.api.i18n._
import play.api.libs.json.Json
import play.api.mvc._
import services.{AirportService, RunwayService}
import play.api.cache._
import views._

import scala.concurrent.duration._

/**
  * Created by Mahbub on 1/8/2017.
  */
class HomeController @Inject() (airportService: AirportService,
                                runwayService: RunwayService,
                                cache: CacheApi,
                                val messagesApi: MessagesApi) extends Controller with I18nSupport with ResultFormatter {

  def index = Action {
    Ok("Howdy! This server is running.")
  }

  def reports = Action { implicit request =>

    val start = System.currentTimeMillis()
    val top = cache.getOrElse[Seq[AirportResult]]("top"){
      val res = airportService.fetchNAirports(orderBy = 3, order = "desc").items.map(r => r.copy(runways = runwayService.runwaysByCountry(r.country)))
      cache.set("top", res, 5 minutes)
      res
    }
    val bottom = cache.getOrElse[Seq[AirportResult]]("bottom"){
      val res = airportService.fetchNAirports(orderBy = 3).items.map(r => r.copy(runways = runwayService.runwaysByCountry(r.country)))
      cache.set("bottom", res, 5 minutes)
      res
    }

    request.headers.toSimpleMap.get("Accept").fold(false)(h => h.contains(JSON)) match {
      case true =>
        val res = models.api.Result[Report](
          true,
          Some(Report(top, bottom)),
          null,
          System.currentTimeMillis() - start
        )
        Ok(Json.stringify(Json.toJson(res))).withHeaders(CONTENT_TYPE -> "appliction/json")
      case false =>
        Ok(html.report(Report(top, bottom)))
    }

  }

  def query(country: String) = Action { implicit request =>

    val start = System.currentTimeMillis()

    val queryResult = cache.getOrElse[Query](country) {
      val airports = airportService.findByCountry(s"%$country%").groupBy(a => a._1.id.get)
      val airportResult = airports.mapValues(v => {
        val runways = v.map(runways => {
          val runway = runways._2
          runway
        })
        val airport = QueryAirport(v.head._3.get.name, v.head._1)
        airport.copy(runways = runways.flatten.toList)
      }).map(_._2).toList
      val res = Query(country, airportResult)
      cache.set(country, res, 5 minutes)
      res
    }

    request.headers.toSimpleMap.get("Accept").fold(false)(h => h.contains(JSON)) match {
      case true =>
        val res = models.api.Result[Query](
          true,
          Some(queryResult),
          null,
          System.currentTimeMillis() - start
        )
        Ok(Json.stringify(Json.toJson(res))).withHeaders(CONTENT_TYPE -> "appliction/json")
      case false =>
        Ok(html.query(queryResult))
    }

  }

}
