package test

import org.specs2._

import play.api._
import play.api.libs.json._

import model.Api

class HelloWorldSpec extends Specification { def is = s2"""

    Api scpecification

    The Api json reader
    	must read insight              $insight
    	must read autocompletion       $autocomplete
    	must read all in order: 
            insight                    $allInsight
            autocomplete               $allAutocomplete
            detect invalid             $allInvalid

    The Api json writer
        must write the insight result       $insightResult
        must write the error result         $errorResult
        must write the completion result    $completionResult
"""

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

    import _root_.io.codebrew.api._
    import eval._

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

    def allInsight = {
    	Api.all(insightJson, (code, cid) => true, (code, pos, cid) => false, () => false ) ==== true
    }

    def allAutocomplete = {
    	Api.all(autocompleteJson, (code, cid) => false, (code, pos, cid) => true, () => false ) ==== true
    }

    def allInvalid = {
    	Api.all(Json.obj("a" -> "1"), (code, cid) => false, (code, pos, cid) => false, () => true ) ==== true
    }

    def insightResult = {
        Api.insightResult(Result(
            Some(InsightResult("a = 1", "1")),
            Nil
        ), 1) ==== Json.obj(
            "insight" -> JsString("a = 1"),
            "output" -> JsString("1"),
            "errors" -> JsArray(),
            "warnings" -> JsArray(),
            "infos" -> JsArray(),
            "callback_id" -> 1
        )
    }

    def errorResult = {
        Api.insightResult(Result(
            None,
            List(
                CompilationInfo("e1", 1, Severity.Error),
                CompilationInfo("w1", 1, Severity.Warning),
                CompilationInfo("i1", 1, Severity.Info)
            )
        ), 1) ==== Json.obj(
            "insight" -> JsString(""),
            "output" -> JsString(""),
            "errors" -> JsArray(Seq( Json.obj("message"-> "e1", "position" -> 1))),
            "warnings" -> JsArray(Seq( Json.obj("message"-> "w1", "position" -> 1))),
            "infos" -> JsArray(Seq( Json.obj("message"-> "i1", "position" -> 1))),
            "callback_id" -> 1
        )
    }

    def completionResult = {
        Api.autocompleteResult(List(
            Completion("a", "a"),
            Completion("b", "b")
        ), 1) ==== Json.obj(
            "completions" -> JsArray(Seq(
                Json.obj("name" -> "a", "signature" -> "a"),
                Json.obj("name" -> "b", "signature" -> "b")
            )),
            "callback_id" -> 1
        )
    }
}