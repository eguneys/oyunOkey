[warn] /home/eguneys/scala/oyun/modules/db/src/main/BSON.scala:31: method _1 in class BSONElement is deprecated: Use [[name]]
[warn]           b += (tuple._1 -> vr.read(tuple._2.asInstanceOf[BSONDocument]))
[warn]                       ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/BSON.scala:31: method _2 in class BSONElement is deprecated: Use [[value]]
[warn]           b += (tuple._1 -> vr.read(tuple._2.asInstanceOf[BSONDocument]))
[warn]                                           ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/BSON.scala:58: method _1 in class BSONElement is deprecated: Use [[name]]
[warn]         for (tuple <- bson.elements) b += (tuple._1 -> valueReader.read(tuple._2))
[warn]                                                  ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/BSON.scala:58: method _2 in class BSONElement is deprecated: Use [[value]]
[warn]         for (tuple <- bson.elements) b += (tuple._1 -> valueReader.read(tuple._2))
[warn]                                                                               ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/BSON.scala:87: method _1 in class BSONElement is deprecated: Use [[name]]
[warn]       for (tuple <- doc.stream if tuple.isSuccess) b += (tuple.get._1 -> tuple.get._2)
[warn]                                                                    ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/BSON.scala:87: method _2 in class BSONElement is deprecated: Use [[value]]
[warn]       for (tuple <- doc.stream if tuple.isSuccess) b += (tuple.get._1 -> tuple.get._2)
[warn]                                                                                    ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/CollExt.scala:49: method uncheckedUpdate in trait GenericCollection is deprecated: Use [[update]]
[warn]       coll.uncheckedUpdate(selector, $set(field -> value))
[warn]            ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/CursorExt.scala:12: method collect in trait Cursor is deprecated: Use `collect` with an [[Cursor.ErrorHandler]].
[warn]       c.collect[M](upTo, stopOnError = false)
[warn]         ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/Env.scala:22: constructor MongoDriver in class MongoDriver is deprecated: Use the constructor with the classloader
[warn]     val driver = new MongoDriver(Some(config))
[warn]                  ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/Env.scala:26: method apply in object DB is deprecated: Use [[MongoConnection.database]]
[warn]       val db = DB(dbUri, connection)
[warn]                ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/QueryBuiderExt.scala:16: method collect in trait Cursor is deprecated: Use `collect` with an [[Cursor.ErrorHandler]].
[warn]       b.cursor[A]().collect[M](upTo, stopOnError = false)
[warn]                     ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/QueryBuiderExt.scala:30: method collect in trait Cursor is deprecated: Use `collect` with an [[Cursor.ErrorHandler]].
[warn]         .collect[Iterable](1, stopOnError = false)
[warn]          ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/dsl.scala:115: method nameValue2Producer in object Producer is deprecated: Replaced by [[element2Producer]] + [[BSONElement.converted]]
[warn]     $doc("$rename" -> $doc((Seq(item) ++ items).map(Producer.nameValue2Producer[String]): _*))
[warn]                                                              ^
[warn] 13 warnings found




[warn] /home/eguneys/scala/oyun/modules/db/src/main/BSON.scala:31: method _1 in class BSONElement is deprecated: Use [[name]]
[warn]           b += (tuple._1 -> vr.read(tuple._2.asInstanceOf[BSONDocument]))
[warn]                       ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/BSON.scala:31: method _2 in class BSONElement is deprecated: Use [[value]]
[warn]           b += (tuple._1 -> vr.read(tuple._2.asInstanceOf[BSONDocument]))
[warn]                                           ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/BSON.scala:58: method _1 in class BSONElement is deprecated: Use [[name]]
[warn]         for (tuple <- bson.elements) b += (tuple._1 -> valueReader.read(tuple._2))
[warn]                                                  ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/BSON.scala:58: method _2 in class BSONElement is deprecated: Use [[value]]
[warn]         for (tuple <- bson.elements) b += (tuple._1 -> valueReader.read(tuple._2))
[warn]                                                                               ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/BSON.scala:87: method _1 in class BSONElement is deprecated: Use [[name]]
[warn]       for (tuple <- doc.stream if tuple.isSuccess) b += (tuple.get._1 -> tuple.get._2)
[warn]                                                                    ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/BSON.scala:87: method _2 in class BSONElement is deprecated: Use [[value]]
[warn]       for (tuple <- doc.stream if tuple.isSuccess) b += (tuple.get._1 -> tuple.get._2)
[warn]                                                                                    ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/CollExt.scala:49: method uncheckedUpdate in trait GenericCollection is deprecated: Use [[update]]
[warn]       coll.uncheckedUpdate(selector, $set(field -> value))
[warn]            ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/CursorExt.scala:12: method collect in trait Cursor is deprecated: Use `collect` with an [[Cursor.ErrorHandler]].
[warn]       c.collect[M](upTo, stopOnError = false)
[warn]         ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/Env.scala:22: constructor MongoDriver in class MongoDriver is deprecated: Use the constructor with the classloader
[warn]     val driver = new MongoDriver(Some(config))
[warn]                  ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/Env.scala:26: method apply in object DB is deprecated: Use [[MongoConnection.database]]
[warn]       val db = DB(dbUri, connection)
[warn]                ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/QueryBuiderExt.scala:16: method collect in trait Cursor is deprecated: Use `collect` with an [[Cursor.ErrorHandler]].
[warn]       b.cursor[A]().collect[M](upTo, stopOnError = false)
[warn]                     ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/QueryBuiderExt.scala:30: method collect in trait Cursor is deprecated: Use `collect` with an [[Cursor.ErrorHandler]].
[warn]         .collect[Iterable](1, stopOnError = false)
[warn]          ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/dsl.scala:115: method nameValue2Producer in object Producer is deprecated: Replaced by [[element2Producer]] + [[BSONElement.converted]]
[warn]     $doc("$rename" -> $doc((Seq(item) ++ items).map(Producer.nameValue2Producer[String]): _*))
[warn]                                                              ^
[warn] 13 warnings found[warn] /home/eguneys/scala/oyun/modules/db/src/main/BSON.scala:31: method _1 in class BSONElement is deprecated: Use [[name]]
[warn]           b += (tuple._1 -> vr.read(tuple._2.asInstanceOf[BSONDocument]))
[warn]                       ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/BSON.scala:31: method _2 in class BSONElement is deprecated: Use [[value]]
[warn]           b += (tuple._1 -> vr.read(tuple._2.asInstanceOf[BSONDocument]))
[warn]                                           ^
[warn] /home/eguneys/scala/oyun/modules/db/src/main/BSON.scala:58: method _1 in class BSONElem[info] C


[warn] /home/eguneys/scala/oyun/modules/round/src/main/Titivate.scala:37: method enumerate in trait Cursor is deprecated: Use `.enumerator` from Play Iteratees module
[warn]         .enumerate(5000, stopOnError = false)
[warn]          ^
[warn] one warning found


[warn] Found version conflict(s) in library dependencies; some are suspected to be binary incompatible:
[warn]
[warn]  * io.netty:netty:3.10.6.Final is selected over {3.10.4.Final, 3.10.1.Final}
[warn]      +- oyun:oyun_2.11:0.1-SNAPSHOT                        (depends on 3.10.6.Final)
[warn]      +- com.typesafe.netty:netty-http-pipelining:1.1.4     (depends on 3.10.4.Final)
[warn]      +- com.typesafe.play:play-netty-server_2.11:2.4.6     (depends on 3.10.4.Final)
[warn]
[warn]  * com.typesafe.play:play-iteratees_2.11:2.6.1 is selected over 2.4.6
[warn]      +- org.reactivemongo:reactivemongo_2.11:0.12.4 ()     (depends on 2.6.1)
[warn]      +- com.typesafe.play:play-server_2.11:2.4.6           (depends on 2.4.6)
[warn]      +- com.typesafe.play:play-json_2.11:2.4.6             (depends on 2.4.6)
[warn]      +- com.typesafe.play:play_2.11:2.4.6                  (depends on 2.4.6)
[warn]
[warn]  * org.scala-stm:scala-stm_2.11:0.8 is selected over 0.7
[warn]      +- com.typesafe.play:play-iteratees_2.11:2.6.1        (depends on 0.8)
[warn]      +- com.typesafe.play:play-iteratees_2.11:2.4.6        (depends on 0.7)
[warn]      +- com.typesafe.play:play_2.11:2.4.6                  (depends on 0.7)
[warn]
[warn] Run 'evicted' to see detailed eviction warnings


