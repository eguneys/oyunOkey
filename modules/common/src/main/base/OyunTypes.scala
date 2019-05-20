package oyun.base

import ornicar.scalalib.{ Zero, ValidTypes }
import play.api.libs.json.{ JsObject, JsError }

trait OyunTypes extends ValidTypes {
  trait IntValue extends Any {
    def value: Int
    override def toString = value.toString
  }

  implicit val jsObjectZero = Zero.instance(JsObject(Seq.empty))
  implicit val jsResultZero = Zero.instance(JsError(Seq.empty))

}

object OyunTypes extends OyunTypes {

  trait StringValue extends Any {
    def value: String
    override def toString = value
  }
}
