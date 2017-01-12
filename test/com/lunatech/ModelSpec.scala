package com.lunatech

import controllers.HomeController
import models.api.ResultFormatter
import org.scalatest.{Failed, Succeeded}
import org.scalatest.concurrent.AsyncAssertions.Waiter
import play.api.cache.CacheApi
import play.api.i18n.MessagesApi

import scala.concurrent.Future

/**
  * Created by malam on 1/10/17.
  */
class ModelSpec extends InitDBBeforeAndAfterAll with ResultFormatter {

  val w = new Waiter

  def testQuery(country: String) = {
    val airport = airportService.findByCountry("Nauru")
    w { airport.size must equal(1) }
    w {
      airport.map( r => r._3 match {
        case Some(country) => country.name match {
          case s if country.name equals(s) => Succeeded
          case s if country.name contains(s) => Succeeded
          case _ => Failed
        }
        case None => Failed
      })
    }
  }

  "Query model" should {
    "retrieve by country" in {
      val keyword = "Nauru"
      testQuery(keyword)
    }

    "retrieve by fuzzy country" in {
      val keyword = "Nau"
      testQuery(s"%$keyword%")
    }
  }

  "Report model" should {
    "retrieve countries with top 10 airports" in {
      val topAirports = airportService.fetchNAirports(order = "desc")
      val bottomAirports = airportService.fetchNAirports()
      w { topAirports.items.size mustBe 10 }
      w { bottomAirports.items.size mustBe 10 }
      w {
        val mergedList = topAirports.items zip bottomAirports.items
        mergedList.foldLeft[Boolean](true)((a, b) => a && (b._1.count >= b._2.count)) mustBe true
      }
    }
  }

  "Controller" should {
    "should be valid" in {
      import play.api.mvc._
      import play.api.test._
      import play.api.test.Helpers._

      val keyword = "Nauru"
      val cacheApi: CacheApi = app.injector.instanceOf(classOf[CacheApi])
      val messageApi: MessagesApi = app.injector.instanceOf(classOf[MessagesApi])
      val controller = new HomeController(countryService, airportService, runwayService, cacheApi, messageApi)
      val result: Future[Result] = controller.query(keyword).apply(FakeRequest().withHeaders(play.api.http.HeaderNames.ACCEPT -> "application/json"))
      status(result) mustEqual OK
      val json = contentAsJson(result)
      (json \\ "isSuccess").map(r => r.toString() mustBe true.toString)
      (json \\ "response").map(r => (r \\ "queryString").map(s => s.toString().removeQuotes mustBe keyword))
    }
  }

}
