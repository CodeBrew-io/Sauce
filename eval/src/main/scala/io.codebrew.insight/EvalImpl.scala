package io.codebrew
package eval

import api.eval._

import com.github.jedesah.codesheet.api.ScalaCodeSheet

import com.twitter._
import util.{Future, FutureTask}

class EvalImpl extends Eval.FutureIface {

	import scala.tools.nsc.interactive.Global
	import scala.tools.nsc.Settings
	import scala.tools.nsc.reporters.StoreReporter
	import scala.reflect.internal.util._
	import scala.tools.nsc.interactive.Response

	val reporter = new StoreReporter()
	val settings = new Settings()
	settings.classpath.value = System.getProperty("replhtml.class.path")
	val compiler = new Global(settings, reporter)

	def insight(code: String): Future[Result] = Future {
		if (code == "") Result(None, Nil)
		else {
			val result = ScalaCodeSheet.computeResults(code, false)
			if(result.subResults.exists(_.isInstanceOf[ScalaCodeSheet.CompileErrorResult])) {
				Result(None, check(code))
			} else {
				Result(Some(InsightResult(result.userRepr, result.output)), Nil)
			}
		}
	}

	private val beginWrap = "package object Codebrew {\n"
	private val endWrap = "}"

	def autocomplete(code: String, pos: Int): Future[List[Completion]] = Future {
		if(code == "") Nil
		else {
			val file = reload(code)
			val ajustedPos = pos + beginWrap.length
			val position = new OffsetPosition(file, ajustedPos)
			val response = new Response[List[compiler.Member]]()
			compiler.askTypeCompletion(position, response)
			response.get match {
        		case Left(members) => compiler.ask( () =>
          			members.map(member => Completion(member.sym.decodedName, member.sym.defString))
        		)
        		case Right(e) => throw e
      		}
		}
	}

	private def check(code: String): List[CompilationInfo] = {
		if (code == "") (Nil)
		else {
			parse(code)
			reporter.infos.map {
				info => CompilationInfo(
					message = info.msg,
					pos = info.pos.point - beginWrap.length, 
					severity = convert(info.severity)
				)
			}.toList
		}
	}

	private def wrap(code: String): BatchSourceFile = {
		new BatchSourceFile("default", beginWrap + code + endWrap)
	}

	private def reload(code: String): BatchSourceFile = {
		val file = wrap(code)
		val response = new Response[Unit]()
		compiler.askReload(List(file), response)
		response.get // block
		file
	}

	private def parse(code: String): Unit = {
		autocomplete(code,0) // fixme
	}

	private def convert(severity: reporter.Severity): Severity = severity match {
		case reporter.INFO => Severity.Info
		case reporter.WARNING => Severity.Warning
		case reporter.ERROR => Severity.Error
	}
}
