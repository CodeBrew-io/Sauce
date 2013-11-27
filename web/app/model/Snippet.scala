package model

import scalastic.elasticsearch.Indexer

import org.elasticsearch._
import search.SearchHit
import index.query._
import FilterBuilders._
import QueryBuilders._

import play.api.libs.json._

import play.api.Play
import play.api.Play.current

case class Snippet( 
  id: String,
  title: String, description: String, codeOrigin: String, codeRaw: String,
  tags: String, scalaVersion: String, user: String){
  def toJson() : JsValue = {
    Json.obj("id" -> id, "code" -> codeOrigin)
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
/*  val clusterName = "snippets_service_cluster"
  val host = "localhost"
  val port = 9300

  val indexer = Indexer.transport(settings = Map("cluster.name" -> clusterName), host = host, ports=Seq(port))

  val indexName = "snippets_service_index"
  val indexType = "user_snippets"*/

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
   |              "raw" : {"type" : "string", "index" : "not_analyzed"}
   |           }
   |        },
   |       "tags" : {"type" : "string", "analyzer" : "keyword"},
   |       "scalaVersion": {"type" : "string"},
   |       "user ": {
   |           "type" : "multi_field",
   |           "fields" : {
   |              "origin" : {"type" : "string", "index" : "analyzed"},
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

  def add(snippet: Snippet): String = {
    val jsonSnippet = Json.obj (
      "title" -> snippet.title,
      "description" -> snippet.description,
      "code.origin" -> snippet.codeOrigin,
      "code.raw" -> snippet.codeOrigin,
      "tags" -> snippet.tags,
      "scalaVersion" -> snippet.scalaVersion,
      "user.origin" -> snippet.user,
      "user.raw" -> snippet.user
    )

    indexer.putMapping(indexName, indexType, snippetMapping)
    indexer.index(indexName, indexType, null, Json.stringify(jsonSnippet)).getId
  }

  def querySnippets(pQuery: QueryBuilder, offset: Option[Int]): List[Snippet] = {
    val responses = indexer.search(
      indices = List(indexName),
      query = pQuery,
      fields = Seq("title", "description", "code.origin", "code.raw", "tags", "scalaVersion", "user.origin"),
      from = offset,
      size = Some(size)
    )
    responses.getHits().hits().map(fromHit).to[List]
  }

  private def fromHit(hit: SearchHit): Snippet = {
    Snippet(
      hit.getId,
      hit.field("title").getValue(),
      hit.field("description").getValue(),
      hit.field("code.origin").getValue(),
      hit.field("code.raw").getValue(),
      hit.field("tags").getValue(),
      hit.field("scalaVersion").getValue(),
      hit.field("user.origin").getValue()
    )
  }

  def query(terms: Option[String] = None, userName: Option[String] = None, offset: Option[Int] = None): List[Snippet] = {

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

    querySnippets(codeTermQueryWithUserFilter, offset)
  }

  def queryDistinct(terms: Option[String] = None, userName: Option[String] = None, offset: Option[Int] = None): List[Snippet] = {
    query(terms, userName, offset).groupBy(_.codeOrigin).values.flatMap(_.headOption).to[List]
  }

  private def byId(id: String, username: String) = {
    boolQuery.
      must(termQuery("user.raw", username)).
      must(idsQuery().ids(id))
  }

  def find(id: String, username: String) = {
    val responses = indexer.search(
      indices = List(indexName),
      query = byId(id, username),
      size = Some(1)
    )

    responses.getHits().hits().map(fromHit).headOption
  }

  def delete(id:String, username:String): Boolean = {
    indexer.deleteByQuery( 
      indices = List(indexName),
      query = byId(id, username)
    ).getIndices().size == 1
  }
}