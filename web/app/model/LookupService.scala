package model

import ca.polymtl.log4900.api._
import lookup._
import eval.Insight

import com.twitter.util.Future
import com.twitter.finagle._
import thrift.ThriftServerFramedCodec
import thrift.ThriftClientFramedCodec
import builder.ClientBuilder
import builder.ServerBuilder
import builder.Server
import scala.util.Try

import scala.util.Try

import org.apache.thrift.protocol.TBinaryProtocol
import java.net.InetSocketAddress

object LookupService {
	var server = Option.empty[Server]
	def start() {
		val port = (for {
			env <- Option(System.getProperty("io.codebrew.lookupPort"))
			port <- Try(env.toInt).toOption
		} yield port).getOrElse(Config.port)

		server = Some(
			ServerBuilder()
				.codec(ThriftServerFramedCodec())
				.name("lookup")
				.bindTo(new InetSocketAddress(port))
				.build(new Lookup.FinagledService(
					new LookupImpl, 
					new TBinaryProtocol.Factory()
				))
		)
	}

	def stop(){
		server.map(_.close())
	}
}

object Registry {
	def getEval: Option[Insight.FinagledClient] = evals.headOption
	def add(eval: Insight.FinagledClient): Unit = {
		evals = eval :: evals
	}
	private var evals = List.empty[Insight.FinagledClient]	
}

class LookupImpl extends Lookup.FutureIface {
	def register(info: ServiceInfo): Future[Unit] = {
		println(s"registering service $info")
		val service = ClientBuilder()
			.hosts(s"${info.host}:${info.port}")
		    .codec(ThriftClientFramedCodec())
		    .hostConnectionLimit(1)
		    .build()

		info.name match {
			case eval.Config.name => Registry.add(new Insight.FinagledClient(service))
		}
		Future.value(())
	}
}