package io.codebrew
package eval

import api.eval._

import com.github.jedesah.codesheet.api.ScalaCodeSheet

import com.twitter._
import util.{Future, FutureTask}
import finagle._
import thrift.ThriftServerFramedCodec
import builder.ServerBuilder
import builder.Server

import org.apache.thrift.protocol.TBinaryProtocol
import java.net.InetSocketAddress
import java.io.{ File, FileOutputStream }
import java.lang.management.ManagementFactory

import scala.util.Try

object EvalServer {
	var server = Option.empty[Server]
	def start(evalPort: Int) {
		server = Some(
			ServerBuilder()
				.codec(ThriftServerFramedCodec())
				.name("scala-eval")
				.bindTo(new InetSocketAddress(evalPort))
				.build(new Eval.FinagledService(
					new EvalImpl, 
					new TBinaryProtocol.Factory()
				))
		)
	}

	def stop(){
		server.map(_.close())
	}
}

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
			val file = wrap(code)
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
			wrap(code)

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
		val file = new BatchSourceFile("default", beginWrap + code + endWrap)
		val response = new Response[Unit]()
		compiler.askReload(List(file), response)
		response.get // block until the presentation reloaded the code
		file
	}

	private def convert(severity: reporter.Severity): Severity = severity match {
		case reporter.INFO => Severity.Info
		case reporter.WARNING => Severity.Warning
		case reporter.ERROR => Severity.Error
	}
}

object Main extends App {
	createRunningPid()

	val evalPort = (for {
		env <- Option(System.getProperty("io.codebrew.scalaEvalPort"))
		port <- Try(env.toInt).toOption
	} yield (port)).getOrElse(Config.port)

	EvalServer.start(evalPort)
	Register.ready(evalPort)

	private def createRunningPid() = {
		for {
			pid <- ManagementFactory.getRuntimeMXBean.getName.split('@').headOption
			userDir <- Option(System.getProperty("user.dir")) } {

			val pidFile = new File(userDir, "RUNNING_PID")
			println(userDir)
			if (pidFile.exists) {
				println("This application is already running (Or delete " + 
					pidFile.getAbsolutePath + " file).")
				System.exit(-1)
			}

			new FileOutputStream(pidFile).write(pid.getBytes)
			Runtime.getRuntime.addShutdownHook(new Thread {
				override def run {
					pidFile.delete()
				}
			})
		}
	}
}