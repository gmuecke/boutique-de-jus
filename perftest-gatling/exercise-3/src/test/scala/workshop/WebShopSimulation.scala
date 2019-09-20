package workshop

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.language.postfixOps


/**
  *
  */
class WebShopSimulation extends Simulation{

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .inferHtmlResources()
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate, sdch, br")
    .acceptLanguageHeader("en-US,en;q=0.8,de-DE;q=0.6,de;q=0.4")
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36")


  setUp(
    scenarios.mostBrowseSomeShop8020.inject(
      rampUsersPerSec(1) to 40 during (4 minutes),
      constantUsersPerSec(40) during (20 minutes)
      //EXERCISE: try out different parameters and load profiles
    )
  ).protocols(httpProtocol)

}
