package edu.arizona.sista.qa.ranking.cache

import java.io.{IOException, File}
import edu.arizona.sista.utils.JSONSerialization
import edu.arizona.sista.struct.Counter

/**
 * Save the features generated by models for QA pairs to a file
 * User: dfried
 * Date: 7/31/14
 */
class JSONFeatureCache(val filename: String) extends FeatureCache {
  val cache = JSONFeatureCache.readCache(filename)

  def writeCache: Unit = {
    JSONFeatureCache.writeCache(filename, cache)
  }
}

object JSONFeatureCache {
  import FeatureCache._
  def readCache(filename: String): Cache = {
    try {
      val qaPairs = JSONSerialization.deserialize[List[QAPair]](new File(filename))
      val cache = mkCache
      for (QAPair(question, answer, features, kernel) <- qaPairs) {
        val key = (question, answer)
        cache(key) = (featuresToCounter(features), kernel)
      }
      cache
    } catch {
      case e:IOException => mkCache
    }
  }

  def writeCache(filename: String, cache: Cache): Unit = this.synchronized {
    val qaPairs = for {
      ((question, answer), (features, kernel)) <- cache.toSeq
    } yield QAPair(question, answer, counterToFeatures(features), kernel)
    JSONSerialization.serialize(new File(filename), qaPairs)
  }
}