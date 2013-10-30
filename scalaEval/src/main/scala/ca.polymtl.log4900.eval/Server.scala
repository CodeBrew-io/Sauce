package ca.polymtl.log4900
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

object InsightServer {
	var server = Option.empty[Server]
	def start(evalPort: Int) {
		server = Some(
			ServerBuilder()
				.codec(ThriftServerFramedCodec())
				.name("scala-eval")
				.bindTo(new InetSocketAddress(evalPort))
				.build(new Insight.FinagledService(
					new InsightImpl, 
					new TBinaryProtocol.Factory()
				))
		)
	}

	def stop(){
		server.map(_.close())
	}
}

class InsightImpl extends Insight.FutureIface {

	import scala.tools.nsc.interactive.Global
	import scala.tools.nsc.Settings
	import scala.tools.nsc.reporters.StoreReporter
	import scala.reflect.internal.util._
	import scala.tools.nsc.interactive.Response

	val reporter = new StoreReporter()
	val settings = new Settings()
	settings.classpath.value = System.getProperty("replhtml.class.path")
	val compiler = new Global(settings, reporter)

	def eval(code: String, pos: Int): Future[Result] = {
		val insightResult = Future(ScalaCodeSheet.computeResults(code, false))
		val errorAndCompletionResult = Future(compileAndCompletion(code, pos))
		insightResult join errorAndCompletionResult map { case (insightResult, (infos, completions)) =>
			Result(
				insight = insightResult.userRepr,
				output = insightResult.output,
				infos = infos,
				completions = completions
			)
		}

	}

	def compileAndCompletion(code: String, pos: Int): (List[CompilationInfo], List[String]) = {
		if (code == "") (Nil, Nil)
		else {
			val beginWrap = "package object Codebrew {\n"
			val endWrap = "}"
			val ajustedPos = pos + beginWrap.length
			val file = new BatchSourceFile("default", beginWrap + code + endWrap)
			val response = new Response[Unit]()
			compiler.askReload(List(file), response)
			response.get
			val position = new OffsetPosition(file, ajustedPos)
			val response1 = new Response[List[compiler.Member]]()
			compiler.askTypeCompletion(position, response1)
			val members = response1.get match {
				case Left(members) => members
				case _ => Nil
			}
			val infos = reporter.infos.map {
				info => CompilationInfo(message = info.msg, pos = info.pos.point - beginWrap.length, severity = convert(info.severity))
			}.toList
			(infos, members.map(_.sym.name.toString))
		}
	}

	def convert(severity: reporter.Severity): Severity = severity match {
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

	InsightServer.start(evalPort)
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