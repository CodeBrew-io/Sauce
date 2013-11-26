package model

import scalastic.elasticsearch.Indexer
import org.elasticsearch.index.query._, FilterBuilders._, QueryBuilders._
import org.elasticsearch.search.facet._, terms._,FacetBuilders._

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

  def toElasticSearchJson(): JsObject = {
    Json.obj (
      "title" -> title,
      "description" -> description,
      "code.origin" -> codeOrigin,
      "code.raw" -> codeRaw,
      "tags" -> tags,
      "scalaVersion" -> scalaVersion,
      "user.origin" -> user,
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

    indexer.putMapping(indexName, indexType, snippetMapping)

    val responses = indexer.search( indices = List(indexName),
      query = pQuery,
      fields = Seq("title", "description", "code.origin", "code.raw", "tags", "scalaVersion", "user.origin"),
      from = offset,
      size = Some(size)
    )

    responses.getHits().hits().map( x => {
      Snippet(
        x.getId,
        x.field("title").getValue(),
        x.field("description").getValue(),
        x.field("code.origin").getValue(),
        x.field("code.raw").getValue(),
        x.field("tags").getValue(),
        x.field("scalaVersion").getValue(),
        x.field("user.origin").getValue()
      )
    }).to[List]
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

  def queryDistinctSnippets(pQuery: QueryBuilder, offset: Option[Int]): List[Snippet] = {
    import scala.collection.JavaConversions._

    indexer.putMapping(indexName, indexType, snippetMapping)

    val responses = indexer.search( indices = List(indexName),
      query = pQuery,
      fields = Seq("title", "description", "code.origin", "code.raw", "tags", "scalaVersion", "user.origin"),
      from = offset,
      facets=Seq(termsFacet("facetDistinctCode").field("code.raw").size(size)),
      size = Some(size)
    )
    val facet: TermsFacet = responses.getFacets.facet("facetDistinctCode")
    facet.getEntries().map( _.getTerm.toString).to[List].map { x => 
      Snippet("", "", "", x, x, "", "", "")
    }
  }

  def queryDistinct(terms: Option[String] = None, userName: Option[String] = None, offset: Option[Int] = None): List[Snippet] = {

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

    queryDistinctSnippets(codeTermQueryWithUserFilter, offset)
  }

  def delete(id:String, username:String): Boolean = {
    val query = boolQuery.
      must(termQuery("user.raw", username)).
      must(idsQuery().ids(id))

    indexer.deleteByQuery( 
      indices = List(indexName),
      query = query
    ).getIndices().size == 1
  }
}