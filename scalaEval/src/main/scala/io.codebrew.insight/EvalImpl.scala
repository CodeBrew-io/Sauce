package io.codebrew
package eval

import api.eval._

import com.github.jedesah.codesheet.api.ScalaCodeSheet

import com.twitter._
import util.Future

import scala.concurrent.duration._
import java.util.concurrent.{TimeoutException, Callable, FutureTask, TimeUnit}

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
		if (code == "") Result(insight = None, infos = Nil, timeout = false)
		else {
			val compilerInfos = check(code)
			if(compilerInfos.exists(_.severity == Severity.Error)) {
				Result(insight = None, infos = compilerInfos, timeout = false)
			} else {
				val insight = withTimeout(5.seconds){ ScalaCodeSheet.computeResults(code, false) }.map( r =>
					InsightResult(r.userRepr, r.output)
				)
				Result(insight, compilerInfos, timeout = insight.isEmpty)
			}
		}
	}

	// TODO: avoid creating another thread for each comp. result
	def withTimeout[T](timeout: Duration)(f: => T): Option[T]= {
		val task = new FutureTask(new Callable[T]() {
			def call = f
		})
		val thread = new Thread( task )
		try {
			thread.start()
			Some(task.get(timeout.toMillis, TimeUnit.MILLISECONDS))
		} catch {
			case e: TimeoutException => None
		} finally { 
			if( thread.isAlive ){
				thread.interrupt()
			}
		}
	}

	private val beginWrap = "object Codebrew {\n"
	private val endWrap = "\n}"

	def autocomplete(code: String, pos: Int): Future[List[Completion]] = Future {
		if(code == "") Nil
		else {
			val file = reload(code)
			val ajustedPos = pos + beginWrap.length
			val position = new OffsetPosition(file, ajustedPos)
			val response = withResponse[List[compiler.Member]](r => 
				compiler.askTypeCompletion(position, r)
			)

			response.get match {
        		case Left(members) => compiler.ask( () =>
          			members.map(member => Completion(member.sym.decodedName, member.sym.signatureString))
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
		withResponse[Unit](r => compiler.askReload(List(file), r)).get
		file
	}

	private def parse(code: String): Unit = {
		val file = reload(code)
		withResponse[compiler.Tree](r => compiler.askStructure(false)(file, r)).get
	}

	private def convert(severity: reporter.Severity): Severity = severity match {
		case reporter.INFO => Severity.Info
		case reporter.WARNING => Severity.Warning
		case reporter.ERROR => Severity.Error
	}

	private def withResponse[A](op: Response[A] => Any): Response[A] = {
		val response = new Response[A]
		op(response)
		response
	}
}
