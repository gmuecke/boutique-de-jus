package workshop

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import workshop.pages._

/**
  *
  */
package object scenarios {

  object chains {

    def register = (username: String, password: String) => exec(RegisterPage).exec(SubmitRegistration(username, password))

    def login = (username: String, password: String) => exec(LoginPage).exec(SubmitLogin(username, password))

    def logout = Logout

    def welcome = exec(WelcomePage)

    def anonymousBrowsing = group("Anonymous Browsing") {
        exec(JuicesPage)
        .exec(BooksPage)
        .exec(AccessoiresPage)
        .exec(CoursesPage)
    }

    def orderProduct(productId: String, qty: Int = 1, returnPage: ShopPages.Page = ShopPages.Juices) =
      group("Order") {
        exec(AddToCart(productId, qty, returnPage)).exec(returnPage.chains)
      }

    def checkout(products: String*) = group("Checkout"){
      exec(ShowCart(products: _*)).exec(ShowOrderSummary(products: _*)).exec(SubmitOrder)
    }

    def shopProducts(username: String, password: String, products: String*) = group("Shopping") {
      exec(chains.login(username, password))
        .exec(
          feed(products.map(i => Map("productId" -> s"$i")).toArray)
            .exec(chains.orderProduct("${productId}")))
        .exec(chains.checkout(products: _*))
        .exec(chains.logout)
    }
  }

  val mostBrowseSomeShop8020 = scenario("Most Browser, Some Shop 80/20")
    .exec(chains.welcome)
    .randomSwitch(
      0.80 -> chains.anonymousBrowsing,
      0.20 -> chains.shopProducts("test","test", "1", "4", "16", "16")
    )

  val onlyBrowsing = scenario("Only Browser")
    .exec(chains.welcome)
    .exec(chains.anonymousBrowsing)

  val onlyShopping = scenario("Only Shopping")
    .exec(chains.welcome)
    .exec(chains.shopProducts("test","test", "1", "4", "16", "6"))


}
