package workshop

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class RegisterUserSimulation extends Simulation {

  val httpProtocol = http
    .baseURL("http://localhost:8080")
    .inferHtmlResources()
    .acceptHeader("image/webp,image/*,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate, sdch, br")
    .acceptLanguageHeader("en-US,en;q=0.8,de-DE;q=0.6,de;q=0.4")
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36")

  val headers_0 = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
    "Upgrade-Insecure-Requests" -> "1")

  val headers_2 = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
    "Accept-Encoding" -> "gzip, deflate, br",
    "Origin" -> "http://localhost:8080",
    "Upgrade-Insecure-Requests" -> "1")


  val scn = scenario("Register User Simulation")
    .exec(http("Register Form")
      .get("/register_input.action")
      .headers(headers_0))
    .pause(20)
    .exec(http("Register Form Submit")
      .post("/register_execute.action")
      .headers(headers_2)
      .formParam("customer.lastname", "A")
      .formParam("customer.firstname", "B")
      .formParam("customer.email", "a@b.ch")
      .formParam("customer.street", "abc")
      .formParam("customer.city", "abc")
      .formParam("customer.zip", "123")
      .formParam("customer.country", "CH")
      .formParam("customer.username", "t234")
      .formParam("customer.password", "t234"))

  setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}
