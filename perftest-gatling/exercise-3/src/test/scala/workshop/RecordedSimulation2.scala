package workshop

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class RecordedSimulation2 extends Simulation {

	val httpProtocol = http
		.baseURL("http://localhost:8080")
		.inferHtmlResources()
		.acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
		.acceptEncodingHeader("gzip, deflate, sdch, br")
		.acceptLanguageHeader("en-US,en;q=0.8,de-DE;q=0.6,de;q=0.4")
		.userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36")

	val headers_0 = Map("Upgrade-Insecure-Requests" -> "1")

	val headers_3 = Map(
		"Accept" -> "*/*",
		"Accept-Encoding" -> "gzip, deflate, br",
		"Content-Type" -> "application/json",
		"Origin" -> "chrome-extension://ompiailgknfdndiefoaoiligalphfdae")

	val headers_4 = Map(
		"Accept-Encoding" -> "gzip, deflate, br",
		"Origin" -> "http://localhost:8080",
		"Upgrade-Insecure-Requests" -> "1")

	val headers_12 = Map("Accept" -> "image/webp,image/*,*/*;q=0.8")

	val headers_22 = Map(
		"Accept" -> "*/*",
		"Accept-Encoding" -> "gzip, deflate",
		"Accept-Language" -> "en-US,de;q=0.7,en;q=0.3",
		"Connection" -> "keep-alive",
		"Pragma" -> "no-cache",
		"User-Agent" -> "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0")

    val uri1 = "localhost"

	val scn = scenario("RecordedSimulation2")
		.exec(http("request_0")
			.get("/")
			.headers(headers_0))
		.pause(4)
		.exec(http("request_1")
			.get("/")
			.headers(headers_0))
		.pause(3)
		.exec(http("request_2")
			.get("/Login.action")
			.headers(headers_0))
		.pause(4)
		.exec(http("request_4")
			.post("/Login.action")
			.headers(headers_4)
			.formParam("username", "abctest")
			.formParam("password", "abctest"))
		.pause(1)
		.exec(http("request_5")
			.get("/products_juices.action")
			.headers(headers_0))
		.pause(2)
		.exec(http("request_6")
			.post("/cart_add.action")
			.headers(headers_4)
			.formParam("url", "http://localhost:8080/products_juices.action")
			.formParam("quantity", "2134")
			.formParam("id", "1"))
		.pause(3)
		.exec(http("request_7")
			.post("/cart_add.action")
			.headers(headers_4)
			.formParam("url", "http://localhost:8080/products_juices.action")
			.formParam("quantity", "123")
			.formParam("id", "2"))
		.pause(2)
		.exec(http("request_8")
			.post("/cart_add.action")
			.headers(headers_4)
			.formParam("url", "http://localhost:8080/products_juices.action")
			.formParam("quantity", "123")
			.formParam("id", "3"))
		.pause(2)
		.exec(http("request_9")
			.get("/products_courses.action")
			.headers(headers_0))
		.pause(3)
		.exec(http("request_10")
			.post("/cart_add.action")
			.headers(headers_4)
			.formParam("url", "http://localhost:8080/products_courses.action")
			.formParam("quantity", "1")
			.formParam("id", "16"))
		.pause(1)
		.exec(http("request_11")
			.get("/cart_.action")
			.headers(headers_0)
			.resources(http("request_12")
			.get("/productImage.action?id=3")
			.headers(headers_12),
            http("request_13")
			.get("/productImage.action?id=1")
			.headers(headers_12),
            http("request_14")
			.get("/productImage.action?id=2")
			.headers(headers_12),
            http("request_15")
			.get("/productImage.action?id=16")
			.headers(headers_12)))
		.pause(2)
		.exec(http("request_16")
			.get("/secure/order_input.action")
			.headers(headers_0)
			.resources(http("request_17")
			.get("/productImage.action?id=1")
			.headers(headers_12),
            http("request_18")
			.get("/productImage.action?id=3")
			.headers(headers_12),
            http("request_19")
			.get("/productImage.action?id=2")
			.headers(headers_12),
            http("request_20")
			.get("/productImage.action?id=16")
			.headers(headers_12)))
		.pause(3)
		.exec(http("request_21")
			.post("/secure/order_execute.action")
			.headers(headers_4)
			.formParam("order", "Submit Order"))

	setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}
