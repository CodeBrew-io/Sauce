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

object InsightServer {
	var server = Option.empty[Server]
	def start() {
		createRunningPid()

		server = Some(
			ServerBuilder()
				.codec(ThriftServerFramedCodec())
				.name("scala-eval")
				.bindTo(new InetSocketAddress("0.0.0.0", Config.port))
				.build(new Insight.FinagledService(
					new InsightImpl, 
					new TBinaryProtocol.Factory()
				))
		)
	}

	def stop(){
		server.map(_.close())
	}

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

class InsightImpl extends Insight.FutureIface {
	def eval(code: String): Future[List[String]] = {
		Future.value(ScalaCodeSheet.computeResults(code, false).userRepr.split('\n').toList)
	}
}

object Main extends App {
	InsightServer.start()
	Register.ready()
}