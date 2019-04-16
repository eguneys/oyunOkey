package controllers

import play.api.http._
import play.api.mvc.Codec

trait ResponseWriter {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit def wInt(implicit codec: Codec): Writeable[Int] =
    Writeable[Int]((i: Int) => codec encode i.toString)
  implicit def cToInt: ContentTypeOf[Int] =
    ContentTypeOf[Int](Some(ContentTypes.TEXT))

  implicit def wOptionString(): Writeable[Option[String]] = ???
  implicit def cToOptionString: ContentTypeOf[Option[String]] =
    ContentTypeOf[Option[String]](Some(ContentTypes.TEXT))
}
