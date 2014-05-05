package model

import com.sksamuel.elastic4s.ElasticClient

import play.api._
import libs.json._
import play.api.Play.current

import scala.concurrent._

case class Snippet(id: Snippets.Id, code: String, user: String, scalaVersion: String) {
	def toJson() : JsValue = {
		Json.obj("id" -> id, "code" -> code, "user" -> user)
	}
	override def equals(other: Any) = {
		other match {
			case Snippet(_, code_, user_, scalaVersion_) => 
				code_ == code &&
				user_ == user &&
				scalaVersion_ == scalaVersion
			case _ => false
		}
	}
}
object Snippet {
	def apply(code: String, user: String, scalaVersion: String): Snippet =
		Snippet("", code, user, scalaVersion)
}

trait Setup {
	implicit val context: ExecutionContext
	val client: ElasticClient
}

trait PlaySetup extends Setup {
	implicit lazy val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
	val client = 
		if(Play.isDev(Play.current)){
			ElasticClient.local
		} else {
			ElasticClient.remote("localhost", 9300)
		}
}

trait TestSetup extends Setup {
	implicit lazy val context = ExecutionContext.Implicits.global
	val client = ElasticClient.local
}

object Snippets extends PlaySetup with SimpleSnippets
object TestSnippets extends TestSetup with SimpleSnippets

trait SimpleSnippets { self: Setup =>
	import org.elasticsearch.action.get.GetResponse
	import org.elasticsearch.action.delete.DeleteResponse
	import org.elasticsearch.action.update.UpdateResponse

	import com.sksamuel.elastic4s.ElasticDsl._
	import com.sksamuel.elastic4s.mapping.FieldType._

	import scala.collection.JavaConversions._

	type Id = String
	type Resource = (Id, String)

	val indexName = "katas"
	val indexType = "snippets"
	val fullindex = s"$indexName/$indexType"

	import scala.concurrent.duration._
	val timeout = 10.minutes

	// create index unless present
	val indexExists = Await.result(client.exists(indexName).map(_.isExists), timeout)
	if(!indexExists) {
		Await.ready(client execute {
			create.index(indexName).mappings(
				indexType as (
					id typed StringType,
					"code" typed StringType,
					"scalaVersion" typed StringType,
					"user" multi(
						"user" typed StringType index "analyzed",
						"user_raw" typed StringType index "not_analyzed"
					)
				)
			)
		}, timeout)
	}
	import scala.collection.JavaConversions._

	private def from(id: Id, m: Map[String, Any]): Snippet = {
		val ms = m.mapValues(_.asInstanceOf[String])
		Snippet(
			id,
			ms("code"),
			ms("user"),
			ms("scalaVersion")
		)
	}

	def query(	terms: Option[String] = None, 
				userName: Option[String] = None, 
				offset: Option[Int] = None) : Future[List[Snippet]] = {

		val termQuery = terms.map(t => term("code",t)).getOrElse(matchall)
		val userQuery = userName.map(u => term("user_raw", u)).getOrElse(matchall)

		client execute {
			search in fullindex query {
				bool { must(termQuery, userQuery) }
			}
		} map { response =>
			response.getHits().to[List].map(hit => 
				from(hit.getId, hit.getSource.toMap)
			)
		}
	}

	def find(resource: Resource): Future[Option[Snippet]] = {
		val (snippetId, userName) = resource
		client execute {
		  get id snippetId from fullindex
		} map { s =>
			// println(s.getId)
			// if(s.getSourceAsMap == null) println("null pointer")
			
			val fieldmap = s.getSourceAsMap.toMap
			fieldmap.get("user").flatMap{ user =>
				if(user == userName) Some(from(s.getId, fieldmap))
				else None
			}
		}
	}

	def add(snippet: Snippet): Future[Snippet] = {
		client execute {
			index into fullindex fields (
				"code" -> snippet.code,
				"scalaVersion" -> snippet.scalaVersion,
				"user" -> snippet.user
			)
		} map (s => snippet.copy(id = s.getId))
	}

	def size: Future[Long] = {
		client execute {
			count from fullindex
		} map(_.getCount)
	}

	// def modify(resource: Resource, code: String): Future[Option[UpdateResponse]] = {
	// 	// if we find the resource update it
	// 	val (i, _) = resource
	// 	lazy val updateQuery = client execute { update id i in(fullindex) doc ("code" -> code) }
	// 	for {
	// 		response <- find(resource)
	// 		_ <- response
	// 		updateResponse <- updateQuery
	// 	} yield updateResponse
	// }

	// isFound
	def remove(resource: Resource): Future[DeleteResponse] = {
		val (i, userName) = resource
		client execute { delete id i from fullindex }
	}
}