package oyun.common

trait Cli {
  def process: PartialFunction[List[String], Fu[String]]
}
