/**
  *
  */
package object workshop {

  object Headers {
    val defaultHeader = Map(
      "Accept" -> "*/*",
      "Accept-Encoding" -> "gzip, deflate",
      "Accept-Language" -> "en-US,de;q=0.7,en;q=0.3",
      "Connection" -> "keep-alive",
      "Pragma" -> "no-cache",
      "User-Agent" -> "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0")

    val acceptCss = Map("Accept" -> "text/css,*/*;q=0.1")

    val acceptImage = Map("Accept" -> "image/webp,image/*,*/*;q=0.8")

    val formHeader = Map(
      "Accept-Encoding" -> "gzip, deflate, br")
  }
}
