package sample

import com.typesafe.config.ConfigFactory

/**
  * @author Anton Gnutov
  */
trait AppConfig {
  val config = ConfigFactory.load()

  val httpHost = config.getString("sample.host")
  val httpPort = config.getInt("sample.port")
  val originUrl = config.getString("sample.origin")
}
