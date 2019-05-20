package oyun

import play.api.http._
import play.api.mvc.Codec
import scalatags.Text.Frag

package object app extends PackageObject with WithPlay {

  implicit def contentTypeOfFrag(implicit codec: Codec): ContentTypeOf[Frag] =
    ContentTypeOf[Frag](Some(ContentTypes.HTML))

  implicit def writeableOfFrag(implicit codec: Codec): Writeable[Frag] =
    Writeable(frag => codec.encode(frag.render))

}
