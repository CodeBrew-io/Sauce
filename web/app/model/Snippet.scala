package model

import scalastic.elasticsearch.Indexer
import org.elasticsearch.index.query._, FilterBuilders._, QueryBuilders._

import play.api.libs.json._

import play.api.Play
import play.api.Play.current

case class Snippet( 
  title: String, description: String, codeOrigin: String, codeParsed: String,
  tags: String, scalaVersion: String, user: String){
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
      "scalaVersion" -> scalaVersion,
      "user.field" -> user,
      "user.raw" -> user
    )
  }
}

object Snippets {

  val size = 10

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
   |       "scalaVersion": {"type" : "string"},
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


  if (!indexer.exists(indexName).isExists()) {
    indexer.createIndex(indexName, settings = Map("number_of_shards" -> "1"))
    indexer.waitTillActive()
  }

  def add(snippet: Snippet): Unit = {
    val jsonSnippet = Json.obj (
      "title" -> snippet.title,
      "description" -> snippet.description,
      "code.origin" -> snippet.codeOrigin,
      "code.parsed" -> snippet.codeParsed,
      "tags" -> snippet.tags,
      "scalaVersion" -> snippet.scalaVersion,
      "user.field" -> snippet.user,
      "user.raw" -> snippet.user
    )

    indexer.putMapping(indexName, indexType, snippetMapping)
    indexer.index(indexName, indexType, null, Json.stringify(jsonSnippet))
    println(Json.stringify(jsonSnippet))
  }

  def querySnippets(pQuery: QueryBuilder, offset: Option[Int]): Array[Snippet] = {

    indexer.putMapping(indexName, indexType, snippetMapping)

    val responses = indexer.search( indices = List(indexName),
      query = pQuery,
      fields = Seq("title", "description", "code.origin", "code.parsed", "tags", "scalaVersion", "user.field"),
      from = offset,
      size = Some(size)
    )

    responses.getHits().hits().map( x => {
      Snippet(
        x.field("title").getValue(),
        x.field("description").getValue(),
        x.field("code.origin").getValue(),
        x.field("code.parsed").getValue(),
        x.field("tags").getValue(),
        x.field("scalaVersion").getValue(),
        x.field("user.field").getValue()
      )
    })
  }

  def query(terms: Option[String] = None, userName: Option[String] = None, offset: Option[Int] = None): Array[Snippet] = {

    //If pTerm == None, then we don't search for a particular term, instead we return everything (matchAll)
    val codeTermQuery = terms.map (
      q => multiMatchQuery( q, "code.origin")
    ).getOrElse(
      matchAllQuery()
    )

    //If pUser !== None, we return every snippets, no matter the user, else we filter for this exact user only
    val codeTermQueryWithUserFilter = userName.map (
      u => filteredQuery(codeTermQuery, termFilter("user.raw", u))
     ).getOrElse(
      filteredQuery(codeTermQuery, null)
    )

    println(codeTermQueryWithUserFilter)

    querySnippets(codeTermQueryWithUserFilter, offset)
  }
}