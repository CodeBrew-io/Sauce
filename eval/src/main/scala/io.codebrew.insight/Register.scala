package io.codebrew
package eval

import api._

import com.twitter.finagle._
import service.SimpleRetryPolicy
import builder.ClientBuilder
import thrift.ThriftClientFramedCodec

import com.twitter.conversions.time._

import com.twitter.util.Try

object Register {
	def ready(evalPort: Int): Unit = {
		val lookupHostname = Option(System.getProperty("io.codebrew.lookupHostname")).getOrElse("localhost")
		val lookupPort = (for {
			env <- Option(System.getProperty("io.codebrew.lookupPort"))
			port <- Try(env.toInt).toOption
		} yield (port)).getOrElse(lookup.Config.port)

		val forever = new SimpleRetryPolicy[Try[Nothing]]{
			def shouldRetry(why: Try[Nothing]) = true
			def backoffAt(retry: Int) = 1.second
		}

		val client = new lookup.Lookup.FinagledClient(
			ClientBuilder()
				.hosts(s"${lookupHostname}:${lookupPort}")
				.codec(ThriftClientFramedCodec())
				.hostConnectionLimit(1)
				.retryPolicy(forever)
				.build()
		)

		val evalHostname = Option(System.getProperty("io.codebrew.scalaEvalHostname")).getOrElse("localhost")
		client.register(lookup.ServiceInfo(eval.Config.name, evalHostname, evalPort))
	}
}