package oyun.base

final class PimpedString(private val s: String) extends AnyVal {

  def replaceIf(t: Char, r: Char): String =
    if (s.indexOf(t) >= 0) s.replace(t, r) else s

  def replaceIf(t: Char, r: CharSequence): String =
    if (s.indexOf(t) >= 0) s.replace(String.valueOf(t), r) else s
  
}
