package oyun.memo

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

import com.google.common.cache._

object Builder {

  private implicit def durationToMillis(d: Duration): Long =  d.toMillis

  def expiry[K, V](ttl:  Duration): Cache[K, V] =
    cacheBuilder[K, V](ttl).build[K, V]


  private def cacheBuilder[K, V](ttl: Duration): CacheBuilder[K, V] =
    CacheBuilder.newBuilder()
      .expireAfterWrite(ttl, TimeUnit.MILLISECONDS)
      .asInstanceOf[CacheBuilder[K, V]]

}
