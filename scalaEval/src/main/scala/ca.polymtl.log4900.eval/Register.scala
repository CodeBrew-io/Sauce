package ca.polymtl.log4900
package eval

import api._

import com.twitter.finagle._
import builder.ClientBuilder
import thrift.ThriftClientFramedCodec

object Register {
	def ready(): Unit = {
		val service = ClientBuilder()
			.hosts(s"${lookup.Config.host}:${lookup.Config.port}")
			.codec(ThriftClientFramedCodec())
			.hostConnectionLimit(1)
			.build()

		val client = new lookup.Lookup.FinagledClient(service)
		client.register(lookup.ServiceInfo(eval.Config.name, eval.Config.host, eval.Config.port))
	}
}