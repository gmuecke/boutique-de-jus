package workshop

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class OrderSimulation extends Simulation {

  val httpProtocol = http
    .baseURL("http://localhost:8080")
    .inferHtmlResources() //remove this line to no load images or scripts
    .acceptHeader("image/webp,image/*,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate, sdch, br")
    .acceptLanguageHeader("en-US,en;q=0.8,de-DE;q=0.6,de;q=0.4")
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36")

  val headers_0 = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
    "Upgrade-Insecure-Requests" -> "1")

  val headers_1 = Map(
    "Accept" -> "*/*",
    "Accept-Encoding" -> "gzip, deflate, br",
    "Content-Type" -> "application/json",
    "Origin" -> "chrome-extension://ompiailgknfdndiefoaoiligalphfdae")

  val headers_2 = Map(
    "Accept" -> "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
    "Accept-Encoding" -> "gzip, deflate, br",
    "Origin" -> "http://localhost:8080",
    "Upgrade-Insecure-Requests" -> "1")


  val scn = scenario("Order Process Simulation")
    .exec(http("Login Page")
      .get("/Login.action")
      .headers(headers_0))
    .pause(6)
    .exec(http("Login Action")
      .post("/Login.action")
      .headers(headers_2)
      .formParam("username", "t234")
      .formParam("password", "t234"))
    .pause(3)
    .exec(http("Juices Page")
      .get("/products_juices.action")
      .headers(headers_0))
    .pause(2)
    .exec(http("Add to Cart")
      .post("/cart_add.action")
      .headers(headers_2)
      .formParam("url", "http://localhost:8080/products_juices.action")
      .formParam("quantity", "12")
      .formParam("id", "1"))
    .exec(http("Accessoires Page")
      .get("/products_accessoires.action")
      .headers(headers_0)
      .resources(
        http("image 4")
          .get("/productImage.action?id=4"),
        http("image 5")
          .get("/productImage.action?id=5"),
        http("image 7")
          .get("/productImage.action?id=7"),
        http("image 6")
          .get("/productImage.action?id=6"),
        http("image 8")
          .get("/productImage.action?id=8")))
    .pause(3)
    .exec(http("Add to Cart")
      .post("/cart_add.action")
      .headers(headers_2)
      .formParam("url", "http://localhost:8080/products_accessoires.action")
      .formParam("quantity", "2")
      .formParam("id", "4"))
    exec(http("Books Page")
        .get("/products_books.action")
        .headers(headers_0)
      .resources(
        http("image 17")
          .get("/productImage.action?id=17"),
        http("image 18")
          .get("/productImage.action?id=18"),
        http("image 23")
          .get("/productImage.action?id=23"),
        http("image 22")
          .get("/productImage.action?id=22"),
        http("image 21")
          .get("/productImage.action?id=21"),
        http("image 19")
          .get("/productImage.action?id=19"),
        http("image 20")
          .get("/productImage.action?id=20"),
        http("image 24")
          .get("/productImage.action?id=24"),
        http("image 25")
          .get("/productImage.action?id=25"),
        http("image 26")
          .get("/productImage.action?id=26")))
    .pause(2)
    .exec(http("Add to Cart")
      .post("/cart_add.action")
      .headers(headers_2)
      .formParam("url", "http://localhost:8080/products_books.action")
      .formParam("quantity", "44")
      .formParam("id", "18"))
    .pause(1)
    .exec(http("Courses Page")
      .get("/products_courses.action")
      .headers(headers_0))
    .pause(3)
    .exec(http("Add to Cart")
      .post("/cart_add.action")
      .headers(headers_2)
      .formParam("url", "http://localhost:8080/products_courses.action")
      .formParam("quantity", "1")
      .formParam("id", "16"))
    .pause(1)
    .exec(http("Show Cart")
      .get("/cart_.action")
      .headers(headers_0)
      .resources(http("image 4")
        .get("/productImage.action?id=4"),
        http("image 18")
          .get("/productImage.action?id=18"),
        http("image 1")
          .get("/productImage.action?id=1"),
        http("image 16")
          .get("/productImage.action?id=16")))
    .pause(3)
    .exec(http("Order Summary")
      .get("/secure/order_input.action")
      .headers(headers_0)
      .resources(http("image 4")
        .get("/productImage.action?id=4"),
        http("image 18")
          .get("/productImage.action?id=18"),
        http("image 1")
          .get("/productImage.action?id=1"),
        http("image 16")
          .get("/productImage.action?id=16")))
    .pause(3)
    .exec(http("Submit Order")
      .post("/secure/order_execute.action")
      .headers(headers_2)
      .formParam("order", "Submit Order"))
    .pause(2)
    .exec(http("Logout")
      .get("/Logout.action")
      .headers(headers_0))

  setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}
