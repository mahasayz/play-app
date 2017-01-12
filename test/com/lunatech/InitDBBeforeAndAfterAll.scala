package com.lunatech

import org.scalatest.{BeforeAndAfterAll, Suite}
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.db.DBApi
import play.api.db.evolutions.Evolutions
import services.{AirportService, CountryService, RunwayService}

/**
  * Created by malam on 1/12/17.
  */
trait InitDBBeforeAndAfterAll extends PlaySpec with BeforeAndAfterAll with OneAppPerSuite {

  var countryService: CountryService = app.injector.instanceOf(classOf[CountryService])
  var airportService: AirportService = app.injector.instanceOf(classOf[AirportService])
  var runwayService: RunwayService = app.injector.instanceOf(classOf[RunwayService])
  var dbapi: DBApi = app.injector.instanceOf(classOf[DBApi])

  override protected def beforeAll() = {
    Evolutions.applyEvolutions(dbapi.database("default"))
  }

  override protected def afterAll() = {
    Evolutions.cleanupEvolutions(dbapi.database("default"))
  }

}
