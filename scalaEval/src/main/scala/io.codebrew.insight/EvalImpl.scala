package io.codebrew
package eval

import api.eval._

import io.codebrew.simpleinsight.Instrument

import scala.concurrent.duration._
import java.util.concurrent.{TimeoutException, Callable, FutureTask, TimeUnit}

import java.util.{ArrayList, List => JList}

class EvalImpl extends Eval.Iface {

	import scala.tools.nsc.interactive.Global
	import scala.tools.nsc.Settings
	import scala.tools.nsc.reporters.StoreReporter
	import scala.reflect.internal.util._
	import scala.tools.nsc.interactive.Response

	val reporter = new StoreReporter()
	val settings = new Settings()
	settings.classpath.value = System.getProperty("replhtml.class.path")
	val compiler = new Global(settings, reporter)

	val instrument = new Instrument

	def insight(code: String): Result = {
		if (code == "") new Result(new ArrayList[CompilationInfo](), false)
		else {
			val (compilerInfos, hasErrors) = check(code)
			if(hasErrors) {
				new Result(compilerInfos, false)
			} else {
				val insight = withTimeout(5.seconds){ instrument(code) }.map{ result =>
					val output = new ArrayList[Instrumentation]()
					result.foreach{ case (line, res) =>
						output.add(new Instrumentation(line, res))
					}
					output
				}
				val result = new Result(compilerInfos, insight.isEmpty)
				if(!insight.isEmpty) {
					insight.map(result.setInsight)
				}
				result
			}
		}
	}

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

	def autocomplete(code: String, pos: Int): JList[Completion] = {
		if(code == "") new ArrayList[Completion]()
		else {
			val file = reload(code)
			val ajustedPos = pos + beginWrap.length
			val position = new OffsetPosition(file, ajustedPos)
			val response = withResponse[List[compiler.Member]](r => 
				compiler.askTypeCompletion(position, r)
			)

			response.get match {
        		case Left(members) => compiler.ask( () => {
        			val list = new ArrayList[Completion]()
          			val res = members.map(member => new Completion(member.sym.decodedName, member.sym.signatureString))
					res.foreach(list.add)
					list
        		})
        		case Right(e) => throw e
      		}
		}
	}

	private def check(code: String): (JList[CompilationInfo], Boolean) = {
		parse(code)
		val res = reporter.infos.map {
			info => new CompilationInfo(
				info.msg, // message
				info.pos.point - beginWrap.length, // pos
				convert(info.severity) 
			)
		}
		val list = new ArrayList[CompilationInfo]()
		res.foreach(list.add)
		(list, res.exists(_.severity == Severity.ERROR))
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
		case reporter.INFO => Severity.INFO
		case reporter.WARNING => Severity.WARNING
		case reporter.ERROR => Severity.ERROR
	}

	private def withResponse[A](op: Response[A] => Any): Response[A] = {
		val response = new Response[A]
		op(response)
		response
	}
}
