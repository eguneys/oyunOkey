package oyun.common

import scala.concurrent.duration._
import java.util.concurrent.TimeUnit

import com.typesafe.config.Config

object PimpedConfig {

  implicit final class OyunPimpedConfig(config: Config) {
    def millis(name: String): Int = config.getDuration(name, TimeUnit.MILLISECONDS).toInt
    def duration(name: String): FiniteDuration = millis(name).millis
  }
}
