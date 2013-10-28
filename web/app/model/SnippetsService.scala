package model

import scalastic.elasticsearch.Indexer

import org.elasticsearch.index.query.QueryBuilders._
import org.elasticsearch.index.query._

import play.api.libs.json.{ Json, JsObject }
import play.api.Play
import play.api.Play.current

object SnippetsService {

  val clusterName = Play.application.configuration.getString("elasticsearch.cluster").getOrElse("")
  val host = Play.application.configuration.getString("elasticsearch.host").getOrElse("")
  val port = Play.application.configuration.getString("elasticsearch.port").getOrElse("").toInt

  val indexer = Indexer.transport(settings = Map("cluster.name" -> clusterName), host = host, ports=Seq(port))

  val indexName = Play.application.configuration.getString("elasticsearch.index").getOrElse("")
  val indexType = Play.application.configuration.getString("elasticsearch.userSnippetsType").getOrElse("")

  val snippetMapping = s"""
                         |{
                         |  "$indexType":{
                         |    "properties" : {
                         |       "title" : {"type" : "string"},
                         |       "code" : {"type" : "string"},
                         |       "user ": {
                         |           "type" : "multi_field",
                         |           "fields" : {
                         |              "field" : {"type" : "string", "index" : "analyzed"},
                         |              "raw" : {"type" : "string", "index" : "not_analyzed"}
                         |           }
                         |       }
                         |    }
                         |  }
                         |}""".stripMargin

  indexer.createIndex(indexName, settings = Map("number_of_shards" -> "1"))
 // indexer.waitTillActive()

  //indexer.putMapping(indexName, indexType, mapping)

  def addSnippet(snippet: Snippet): String = {

    val jsonSnippet = Json.obj (
      "title" -> snippet.title,
      "code" -> snippet.code,
      "user" -> snippet.user
    )
    indexer.putMapping(indexName, indexType, snippetMapping)
    indexer.index(indexName, indexType, null, Json.stringify(jsonSnippet))
    //indexer.refresh()

    Json.stringify(jsonSnippet)
  }

  def querySnippets(pQuery: QueryBuilder): Array[Snippet] = {

    val responses = indexer.search( indices = List(indexName), query = pQuery)

    responses.getHits().hits().map(
      x => Snippet(
        x.field("title").getValue(),
        x.field("code").getValue(),
        x.field("user").getValue()
      )
    )
  }

  /*  def searchSnippetsByCode(term: String): Array[Snippet] = {

  }

  def searchSnippetsUser(user: String): Array[Snippet] = {

    querySnippets(matchQuery("user.raw", user))

  }
  */

  def search(term: Option[String], user: Option[String]): Array[Snippet] = {

    val userMatchQuery = matchQuery("user.raw", user)
    val codeTermQuery = matchQuery("code", term)

    querySnippets(codeTermQuery)

  }



  case class Snippet(title: String, code: String, user: String)

  // implicit val Snippet = Json.writes[Snippet]

}