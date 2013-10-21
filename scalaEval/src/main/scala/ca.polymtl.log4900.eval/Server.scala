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

object InsightServer {
	var server = Option.empty[Server]
	def start() {
		createRunningPid()

		val protocol = new TBinaryProtocol.Factory()
		val serverService = new Insight.FinagledService(new InsightImpl, protocol)
		val address = new InetSocketAddress(Config.host, Config.port)

		val s = ServerBuilder()
			.codec(ThriftServerFramedCodec())
			.name("insight-service")
			.bindTo(address)
			.build(serverService)

		server = Some(s)
	}

	def stop(){
		server.map(_.close())
	}

	private def createRunningPid() = {
		java.lang.management.ManagementFactory.getRuntimeMXBean.getName.split('@').headOption.map { pid =>
			val pidFile = new File(".", "RUNNING_PID")
			if (pidFile.exists) {
				println("This application is already running (Or delete " + pidFile.getAbsolutePath + " file).")
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