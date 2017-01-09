package models.api

import anorm.SqlParser._
import anorm._

/**
  * Created by Mahbub on 1/8/2017.
  */
case class Country(id: Option[Long],
                   code: String,
                   name: String,
                   continent: Option[String],
                   wikipediaLink: Option[String],
                   keywords: Option[String]) extends DBModel

object Country {
  val countryParser = {
    get[Option[Long]]("country.id") ~
      get[String]("country.code") ~
      get[String]("country.name") ~
      get[Option[String]]("country.continent") ~
      get[Option[String]]("country.wikipediaLink") ~
      get[Option[String]]("country.keywords") map {
      case id ~ code ~ name ~ continent ~ wiki ~ keywords => Country(id, code, name, continent, wiki, keywords)
    }
  }
}
