package my.simplestore

import com.typesafe.config.ConfigFactory

object FromConfig {
  lazy val settings = ConfigFactory.load("application")
}

trait FromConfig {
  lazy val settings = FromConfig.settings.getConfig({
    val c = this.getClass.getName
    if ((c takeRight 1) == "$") c dropRight 1 else c
  })
}


