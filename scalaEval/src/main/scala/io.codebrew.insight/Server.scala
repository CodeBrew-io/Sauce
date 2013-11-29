package io.codebrew
package eval

import api.eval._

import com.twitter._
import finagle._
import thrift.ThriftServerFramedCodec
import builder.ServerBuilder
import builder.Server

import org.apache.thrift.protocol.TBinaryProtocol
import java.net.InetSocketAddress
import java.io.{File, FileOutputStream}
import java.lang.management.ManagementFactory

import scala.util.Try

object EvalServer {
	var server = Option.empty[Server]
	def start(evalPort: Int) {
		server = Some(
			ServerBuilder()
				.codec(ThriftServerFramedCodec())
				.name("scala-eval")
				.maxConcurrentRequests(1)
				.bindTo(new InetSocketAddress(evalPort))
				.build(new Eval.FinagledService(
					new EvalImpl, 
					new TBinaryProtocol.Factory()
				))
		)
	}

	def stop(){
		server.map(_.close())
	}
}


object Main extends App {
	override def main(args: Array[String]) = {
		lazy val fromEnv = for {
		        env <- Option(System.getProperty("io.codebrew.scalaEvalPort"))
		        port <- Try(env.toInt).toOption
		} yield { createRunningPid(); port }

		val port = args.headOption.map(_.toInt) orElse fromEnv getOrElse(Config.port)

		EvalSecurity.start
		EvalServer.start(port)
		Register.ready(port)
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