package controllers

import javax.inject.Inject

import models.api.{AirportResult, Report, ResultFormatter}
import play.api.i18n._
import play.api.libs.json.Json
import play.api.mvc._
import services.AirportService
import play.api.cache._
import scala.concurrent.duration._

/**
  * Created by Mahbub on 1/8/2017.
  */
class HomeController @Inject() (airportService: AirportService,
                                cache: CacheApi,
                                val messagesApi: MessagesApi) extends Controller with I18nSupport with ResultFormatter {

  case class Response(isSuccess: Boolean, response: String)



  def index = Action {
    Ok("Howdy! This server is running.")
  }

  def reports = Action {

    val start = System.currentTimeMillis()
    val top = cache.getOrElse[Seq[AirportResult]]("top"){
      val res = airportService.fetchNAirports(orderBy = 3, order = "desc").items.map(r => AirportResult.tupled(r))
      cache.set("top", res, 5 minutes)
      res
    }
    val bottom = cache.getOrElse[Seq[AirportResult]]("bottom"){
      val res = airportService.fetchNAirports(orderBy = 3).items.map(r => AirportResult.tupled(r))
      cache.set("bottom", res, 5 minutes)
      res
    }

    val res = models.api.Result[Report](
      true,
      Some(Report(top, bottom)),
      null,
      System.currentTimeMillis() - start
    )

//    val res = airportService.fetchNAirports(orderBy = 3, order = order)
    Ok(Json.stringify(Json.toJson(res))).withHeaders(CONTENT_TYPE -> "appliction/json")
  }

}
