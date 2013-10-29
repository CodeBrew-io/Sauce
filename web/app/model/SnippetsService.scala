package model

import scalastic.elasticsearch.Indexer
import org.elasticsearch.index.query._, FilterBuilders._, QueryBuilders._

import play.api.libs.json._

import play.api.Play
import play.api.Play.current

object SnippetsService {

  case class Snippet(title: String, description: String, codeOrigin: String, codeParsed: String, tags: String, scalaVar: String, user: String)
  {
    def toJson() : JsValue = {
      Json.toJson(
        Map(
          "snippet" -> toElasticSearchJson()
        )
      )
    }

    def toElasticSearchJson(): JsObject = {
      Json.obj (
        "title" -> title,
        "description" -> description,
        "code.origin" -> codeOrigin,
        "code.parsed" -> codeParsed,
        "tags" -> tags,
        "scalaVer" -> scalaVar,
        "user.field" -> user,
        "user.raw" -> user
      )
    }
  }




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
                         |       "title" : {"type" : "string", "analyzer" : "snowball"},
                         |       "description" : {"type" : "string", "analyzer" : "snowball"},
                         |       "code" : {
                         |           "type" : "multi_field",
                         |           "fields" : {
                         |              "origin" : {"type" : "string", "index" : "analyzed"},
                         |              "parsed" : {"type" : "string", "index" : "analyzed"}
                         |           }
                         |        },
                         |       "tags" : {"type" : "string", "analyzer" : "keyword"},
                         |       "scalaVer": {"type" : "string"},
                         |       "user ": {
                         |           "type" : "multi_field",
                         |           "fields" : {
                         |              "field" : {"type" : "string", "index" : "analyzed"},
                         |              "raw" : {"type" : "string", "index" : "not_analyzed"}
                         |        }
                         |      }
                         |    }
                         |  }
                         |}""".stripMargin


  if (!indexer.exists(indexName).isExists())
  {
    indexer.createIndex(indexName, settings = Map("number_of_shards" -> "1"))
    indexer.waitTillActive()
  }


  def addSnippet(snippet: Snippet): String = {

    // elastic search format
    val jsonSnippet = Json.obj (
      "title" -> snippet.title,
      "description" -> snippet.description,
      "code.origin" -> snippet.codeOrigin,
      "code.parsed" -> snippet.codeParsed,
      "tags" -> snippet.tags,
      "scalaVer" -> snippet.scalaVar,
      "user.field" -> snippet.user,
      "user.raw" -> snippet.user
    )

    indexer.putMapping(indexName, indexType, snippetMapping)
    indexer.index(indexName, indexType, null, Json.stringify(jsonSnippet))

    Json.stringify(jsonSnippet)
  }


    def querySnippets(pQuery: QueryBuilder): Array[Snippet] = {

      indexer.putMapping(indexName, indexType, snippetMapping)

      val responses = indexer.search( indices = List(indexName),
        query = pQuery,
        fields = Seq("title", "description", "code.origin", "code.parsed", "tags", "scalaVer", "user.field")
        )

      responses.getHits().hits().map(
        x => Snippet(
          x.field("title").getValue(),
          x.field("description").getValue(),
          x.field("code.origin").getValue(),
          x.field("code.parsed").getValue(),
          x.field("tags").getValue(),
          x.field("scalaVer").getValue(),
          x.field("user.field").getValue()
        )
      )
    }

    def search(q: Option[String], u: Option[String]): Array[Snippet] = {

      //If pTerm == None, then we don't search for a particular term, instead we return everything (matchAll)
      val codeTermQuery = q.map (
        q => multiMatchQuery( q, "code.parsed","title", "description", "tags")
      ).getOrElse(
        matchAllQuery()
      )

      //If pUser !== None, we return every snippets, no matter the user, else we filter for this exact user only
      val codeTermQueryWithUserFilter = u.map (
        u => filteredQuery(codeTermQuery, termFilter("user.raw", u))
       ).getOrElse(
        filteredQuery(codeTermQuery, null)
      )

      querySnippets(codeTermQueryWithUserFilter)
    }


}