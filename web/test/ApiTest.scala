package test

import org.specs2._

import play.api._
import play.api.libs.json._

import model.Api

class HelloWorldSpec extends Specification { def is = s2"""

    Api scpecification

    The Api json reader
    	must parse ping											$ping
    	must parse insight										$insight
    	must parse autocompletion								$autocomplete
    	must parse all in order: ping, insight, autocomplete	$all
"""

	val pingJson = Json.obj(
		"ping" -> Json.obj(
			"callback_id" -> 1
		)
	)

	val insightJson = Json.obj(
		"insight" -> Json.obj(
			"code" -> "1+1",
			"callback_id" -> 1
		)
	)

	val autocompleteJson = Json.obj(
		"autocomplete" -> Json.obj(
			"code" -> "1+1",
			"position" -> 12,
			"callback_id" -> 1
		)
	)

    def ping = {
    	Api.ping.reads(pingJson) must beLike { 
    		case JsSuccess(1, _) => ok
    		case JsError(_) => ko
    	}
    }

    def insight = {
    	Api.insight.reads(insightJson) must beLike { 
    		case JsSuccess(("1+1",1), _) => ok
    		case JsError(_) => ko
    	}
    }

    def autocomplete = {
    	Api.autocomplete.reads(autocompleteJson) must beLike { 
    		case JsSuccess(("1+1",12,1), _) => ok
    		case JsError(_) => ko
    	}
    }

    def all = {
    	Api.all(pingJson, (cid) => true, (code, cid) => false, (code, pos, cid) => false, () => false ) ==== true
    	Api.all(insightJson, (cid) => false, (code, cid) => true, (code, pos, cid) => false, () => false ) ==== true
    	Api.all(autocompleteJson, (cid) => false, (code, cid) => false, (code, pos, cid) => true, () => false ) ==== true
    	Api.all(Json.obj("a" -> "1"), (cid) => false, (code, cid) => false, (code, pos, cid) => false, () => true ) ==== true
    }
}