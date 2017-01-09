package services

import javax.inject.Inject

import anorm._
import models.api.Country
import play.api.db.DBApi
import utils.CSVConverter

import scala.concurrent.Future
import scala.util.{Failure, Success}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Mahbub on 1/8/2017.
  */
@javax.inject.Singleton
class CountryService @Inject() (dBApi: DBApi) extends DBService[Country](dbApi = dBApi) {

  def insert(input: Country) = {
    db.withConnection { implicit connection =>
      SQL(
        """
          insert into country values (
            {id}, {code}, {name}, {continent}, {wiki}, {keywords}
          )
        """
      ).on(
        'id -> input.id,
        'code -> input.code,
        'name -> input.name,
        'continent -> input.continent,
        'wiki -> input.wikipediaLink,
        'keywords -> input.keywords.map(_.replaceAll("~", ","))
      ).executeUpdate()
    }
  }

  override protected val fsPath: String = "C:\\Users\\Mahbub\\Documents\\csv\\countries.csv"

  def task(list: List[String]) = Future {
    list.map(line => {
      val input = line.split(",").reduce((r, s) => {
        CSVConverter.countOccurrence(r) match {
          case c: Int if c % 2 == 1 => r ++ "~" ++ s
          case _ => r ++ "," ++ s
        }
      })
      val res = CSVConverter[Country].from(input)
      res match {
        case Success(s) => insert(s)
        case Failure(e) =>
          println(s"Exception : $e, line : $line")
      }
    })
  }

}
