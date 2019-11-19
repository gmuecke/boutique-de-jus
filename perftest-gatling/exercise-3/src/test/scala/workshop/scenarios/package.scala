package workshop

import io.gatling.core.Predef._
import io.gatling.core.structure.{ChainBuilder, ScenarioBuilder}
import workshop.pages._

/**
  *
  */
package object scenarios {

  object chains {

    def register: (String, String) => ChainBuilder = (username: String, password: String) => exec(RegisterPage).exec(SubmitRegistration(username, password))

    def login: (String, String) => ChainBuilder = (username: String, password: String) => exec(LoginPage).exec(SubmitLogin(username, password))

    def logout: ChainBuilder = Logout

    def welcome: ChainBuilder = exec(WelcomePage)

    def anonymousBrowsing: ChainBuilder = group("Anonymous Browsing") {
        exec(JuicesPage)
        .pause(20)
        .exec(BooksPage)
        .pause(20)
        .exec(AccessoiresPage)
        .pause(20)
        .exec(CoursesPage)
        .pause(20)
    }

    def orderProduct(productId: String, qty: Int = 1, returnPage: ShopPages.Page = ShopPages.Juices): ChainBuilder =
      group("Order") {
        exec(AddToCart(productId, qty, returnPage))
          .pause(20)
          .exec(returnPage.chains)
      }

    def checkout(products: String*): ChainBuilder = group("Checkout"){
      exec(ShowCart(products: _*)).exec(ShowOrderSummary(products: _*)).exec(SubmitOrder)
    }

    def shopProducts(username: String, password: String)(products: String*): ChainBuilder = group("Shopping") {
      exec(chains.login(username, password))
        .pause(20)
        .exec(products.map(id => chains.orderProduct(id)).toArray)
        .pause(20)
        .exec(chains.checkout(products: _*))
        .pause(20)
        .exec(chains.logout)
    }
  }

  val mostBrowseSomeShop9010: ScenarioBuilder = scenario("Most Browser, Some Shop 80/20")
    .exec(chains.welcome)
    .randomSwitch(
      90.0 -> chains.anonymousBrowsing,
      10.0 -> chains.shopProducts("test","test")("1", "4", "16", "16")
    )

  val mostBrowseSomeShop8020: ScenarioBuilder = scenario("Most Browser, Some Shop 80/20")
    .exec(chains.welcome)
    .randomSwitch(
      80.0 -> chains.anonymousBrowsing,
      20.0 -> chains.shopProducts("test","test")("1", "4", "16", "16")
    )

  val onlyBrowsing: ScenarioBuilder = scenario("Only Browser")
    .exec(chains.welcome)
    .exec(chains.anonymousBrowsing)

  val onlyShopping: ScenarioBuilder = scenario("Only Shopping")
    .exec(chains.welcome)
    .exec(chains.shopProducts("test","test")("1", "4", "16", "6"))


}
