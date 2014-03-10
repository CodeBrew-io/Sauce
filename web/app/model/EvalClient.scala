package model

import io.codebrew.api.eval.{Eval, Config}

import org.apache.thrift._
import transport.TTransport;
import transport.TSocket;
import protocol.TBinaryProtocol;
import protocol.TProtocol;

import scala.util.Try

object EvalClient {
	private val fromEnv = for {
		env <- Option(System.getProperty("io.codebrew.scalaEvalPort"))
		port <- Try(env.toInt).toOption
	} yield port

	private val port = fromEnv getOrElse(Config.port)

    private val transport = new TSocket("localhost", port);
    private val protocol = new  TBinaryProtocol(transport);
    private val client = new Eval.Client(protocol);

    def get = client
	def start(): Unit = {
		transport.open();
	}

	def stop(): Unit = {
		transport.close();
	}
}