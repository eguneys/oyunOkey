package oyun.memo

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

import com.google.common.cache._

object Builder {

  private implicit def durationToMillis(d: Duration): Long =  d.toMillis

  /**
    * A caching wrapper for a function (K => V),
    * backed by a Cache from Google Collections.
    */
  def cache[K, V](ttl: Duration, f: K => V): LoadingCache[K, V] =
    cacheBuilder[K, V](ttl).build[K, V](f)

  def expiry[K, V](ttl:  Duration): Cache[K, V] =
    cacheBuilder[K, V](ttl).build[K, V]


  private def cacheBuilder[K, V](ttl: Duration): CacheBuilder[K, V] =
    CacheBuilder.newBuilder()
      .expireAfterWrite(ttl, TimeUnit.MILLISECONDS)
      .asInstanceOf[CacheBuilder[K, V]]

  implicit def functionToGoogleCacheLoader[T, R](f: T => R): CacheLoader[T, R] =
    new CacheLoader[T, R] {
      def load(p1: T) = f(p1)
    }

}
