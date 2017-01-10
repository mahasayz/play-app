import akka.actor.{ActorSystem, Props}
import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import services._
import scala.concurrent.duration._

/**
  * Created by malam on 9/6/16.
  */
@Singleton
class Init @Inject() (appLifecycle: ApplicationLifecycle, config: Configuration,
                      countryService: CountryService,
                      airportService: AirportService,
                      runwayService: RunwayService) {

  val system = ActorSystem("reporter")

  implicit val executionContext = system.dispatchers.lookup("job-dispatcher")

  val csvToDBUpdater = system.actorOf(Props(new CsvToDBUPdater(countryService, airportService, runwayService)))

  system.scheduler.scheduleOnce(0 second, csvToDBUpdater, Run)

}
