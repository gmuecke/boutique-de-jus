package workshop

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._


/**
  *
  */
class WebShopSimulation extends Simulation{

  val httpProtocol = http
    .baseURL("http://localhost:8080")
    .inferHtmlResources()
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate, sdch, br")
    .acceptLanguageHeader("en-US,en;q=0.8,de-DE;q=0.6,de;q=0.4")
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36")


  setUp(
    scenarios.mostBrowseSomeShop8020.inject(
      rampUsersPerSec(1) to (50) during (5 minutes),
      constantUsersPerSec(50) during (10 minutes)
    )
  ).protocols(httpProtocol)

}
