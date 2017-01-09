package services

import akka.actor.Actor

/**
  * Created by Mahbub on 1/10/2017.
  */
class CsvToDBUPdater (countryService: CountryService, airportService: AirportService) extends Actor {

  def receive: Receive = {
    case Run => {
      countryService.csvToDB
      airportService.csvToDB
    }
  }

}

case object Run
