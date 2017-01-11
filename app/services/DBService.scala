package services

import anorm._
import models.api.DBModel
import play.api.Logger
import play.api.db.DBApi

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Mahbub on 1/8/2017.
  */
@javax.inject.Singleton
abstract class DBService [T <: DBModel] (dbApi: DBApi) {
  protected val db = dbApi.database("default")
  protected val fsPath: String

  /**
    * Construct the Map[String,String] needed to fill a select options set.
    */
  implicit class StringUtils(s: String) {
    def removeQuotes = s.replaceAll("\"", "")
  }

  def selectAll[A](parser: RowParser[A], tableName: String, orderBy: String): List[A] = db.withConnection { implicit connection =>
    SQL(s"select * from $tableName order by $orderBy").as(parser *)
  }

  protected def task(list: List[String]): Future[List[AnyVal]]

  def loop(list: List[String]): Future[Boolean] = {
    list.isEmpty match {
      case true => Future(true)
      case false =>
        task(list.take(100))
        loop(list.drop(100))
    }
  }

  def insert(model: T): Int

  def csvToDB = {
    Logger.info(fsPath)
    val lines = scala.io.Source.fromFile(fsPath, "UTF-8").getLines().drop(1).toList
    Logger.info(s"lines = ${lines.size}")
    loop(lines)
  }

}
