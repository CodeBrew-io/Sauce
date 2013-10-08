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

object InsightServer {
	var server = Option.empty[Server]
	def start() {
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