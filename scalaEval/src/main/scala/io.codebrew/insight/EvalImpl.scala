package io.codebrew
package eval

import api.eval._

import simpleinsight.Instrument
import simpleinsight.Instrument.Code

import scala.util.control.NonFatal
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
				try { 
					val insight = withTimeout(20.seconds){instrument(code)}.map{ result =>
						val output = new ArrayList[Instrumentation]()
						result.foreach{ case (line, res) =>
							val instrumentation = res match {
								case Code(code) => new Instrumentation(line, code, InstrumentationType.CODE)
							}
							output.add(instrumentation)
						}
						output
					}
					val result = new Result(compilerInfos, insight.isEmpty)
					if(!insight.isEmpty) {
						insight.map(result.setInsight)
					}
					result
				} catch {
					case NonFatal(e) => {
						val result = new Result(compilerInfos, false)
						for {
							e1 <- Option(e.getCause)
							e2 <- Option(e1.getCause)
						} yield {
							val error = new RuntimeError(e2.toString, e2.getStackTrace.head.getLineNumber)
							result.setRuntimeError(error)
						}
						result
					}
				}
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
				thread.stop()
			}
		}
	}

	private val beginWrap = "object Codebrew {\n"
	private val endWrap = "\n}"

	private val wrapOffset = beginWrap.size

	def autocomplete(code: String, pos: Int): JList[Completion] = {
		if(code == "") new ArrayList[Completion]()
		else {
			val file = reload(code)
			val ajustedPos = pos + wrapOffset
			val position = new OffsetPosition(file, ajustedPos)
			val response = withResponse[List[compiler.Member]](r => 
				compiler.askTypeCompletion(position, r)
			)

			response.get match {
        		case Left(members) => compiler.ask( () => {
        			val list = new ArrayList[Completion]()
          			val res = members.map(member => 
          				new Completion(member.sym.decodedName, member.sym.signatureString)
          			)
					res.foreach(list.add)
					list
        		})
        		case Right(e) => throw e
      		}
		}
	}

	private def check(code: String): (JList[CompilationInfo], Boolean) = {
		parse(code)
		def annoying(info: CompilationInfo) = {
			info.message == "a pure expression does nothing in statement " +
				"position; you may be omitting necessary parentheses" &&
			info.severity == Severity.WARNING
		}
		val res = reporter.infos.map {
			info => new CompilationInfo(
				info.msg, // message
				info.pos.point - wrapOffset, // pos
				convert(info.severity) 
			)
		}.filterNot(annoying)
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