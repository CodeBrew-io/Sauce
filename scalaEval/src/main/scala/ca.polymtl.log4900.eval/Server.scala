package ca.polymtl.log4900
package eval

import api.eval._

import com.github.jedesah.codesheet.api.ScalaCodeSheet

import com.twitter._
import util.Future
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

	def eval(code: String): Future[String] = {
		Future.value(ScalaCodeSheet.computeResults(code, false).userRepr)
	}

	import scala.tools.nsc.interactive.Global
	import scala.tools.nsc.Settings
	import scala.tools.nsc.reporters.StoreReporter
	import scala.reflect.internal.util._
	import scala.tools.nsc.interactive.Response

	val reporter = new StoreReporter()
	val settings = new Settings()
	settings.classpath.value = System.getProperty("replhtml.class.path")
	val compiler = new Global(settings, reporter)

	def codeComplete(code: String, pos: Int): Future[List[String]] = {
		val file = new BatchSourceFile("default", code)
		val response = new Response[Unit]()
		compiler.askReload(List(file), response)
		response.get
		val position = new OffsetPosition(file, pos)
		val response1 = new Response[List[compiler.Member]]()
		compiler.askTypeCompletion(position, response1)
		val members = response1.get match {
			case Left(members) => members
			case _ => Nil
		}
		Future.value(members.map(_.sym.name.toString))
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