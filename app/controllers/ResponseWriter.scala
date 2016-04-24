package controllers

import play.api.http._

trait ResponseWriter {

  implicit def wOptionString(): Writeable[Option[String]] = ???
  implicit def cToOptionString: ContentTypeOf[Option[String]] =
    ContentTypeOf[Option[String]](Some(ContentTypes.TEXT))
}
