package ca.polymtl.log4900
package eval

import api._

import com.twitter.finagle._
import builder.ClientBuilder
import thrift.ThriftClientFramedCodec

object Register {
	def ready(): Unit = {
		import scala.sys.process._
		"hostname".!!.split('\n').headOption.map{ hostname =>
			val lookupHostname = Option(System.getProperty("io.codebrew.lookupHostname")).getOrElse("localhost")
			val service = ClientBuilder()
				.hosts(s"${lookupHostname}:${lookup.Config.port}")
				.codec(ThriftClientFramedCodec())
				.hostConnectionLimit(1)
				.build()

			val client = new lookup.Lookup.FinagledClient(service)
			client.register(lookup.ServiceInfo(
				eval.Config.name,
				hostname,
				eval.Config.port
			))
		}
	}
}