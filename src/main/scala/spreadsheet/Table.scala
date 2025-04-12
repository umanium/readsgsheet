package spreadsheet

case class Table(header: Map[String, String], data: Seq[Seq[String]])
