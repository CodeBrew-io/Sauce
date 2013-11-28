package model

import io.codebrew.api._
import lookup._
import eval.Eval

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
	def getEval: Option[Eval.FinagledClient] = eval
	def set(e: Eval.FinagledClient): Unit = {
		eval = Some(e)
	}
	private var eval: Option[Eval.FinagledClient] = _
}

class LookupImpl extends Lookup.FutureIface {
	def register(info: ServiceInfo): Future[Unit] = Future {
		hosts = new InetSocketAddress(info.host, info.port) :: hosts
		val service = ClientBuilder()
			.hosts(hosts)
		    .codec(ThriftClientFramedCodec())
		    .hostConnectionLimit(1)
		    .build()
		Registry.set(new Eval.FinagledClient(service))
	}
	private var hosts = List.empty[InetSocketAddress]
}