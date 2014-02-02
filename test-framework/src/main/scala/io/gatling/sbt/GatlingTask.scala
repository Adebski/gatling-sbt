package io.gatling.sbt

import java.io.{ PrintWriter, StringWriter }
import sbt.testing.{ EventHandler, Logger, OptionalThrowable, Task, TaskDef, TestSelector }

import io.gatling.app.{ Gatling, GatlingStatusCodes }
import io.gatling.core.scenario.Simulation

class GatlingTask(val taskDef: TaskDef, testClassLoader: ClassLoader, args: Array[String], remoteArgs: Array[String]) extends Task {

	val tags = Array.empty[String]

	def execute(eventHandler: EventHandler, loggers: Array[Logger]) = {
		// Load class
		val className = taskDef.fullyQualifiedName
		val simulationClass = testClassLoader.loadClass(className).asInstanceOf[Class[Simulation]]

		// Start Gatling and compute duration
		val before = System.nanoTime()
		val (returnCode, exception) =
			try {
				(Gatling.runGatling(args, Some(simulationClass)), None)
			} catch {
				case e: Exception =>
					val sw = new StringWriter
					e.printStackTrace(new PrintWriter(sw))
					loggers.map(_.error(sw.toString))
					(GatlingStatusCodes.assertionsFailed, Some(e))
			}
		val duration = (System.nanoTime() - before) / 1000

		// Prepare event data
		val simulationName = simulationClass.getSimpleName
		val selector = new TestSelector(simulationName)
		val optionalThrowable = exception.map(new OptionalThrowable(_)).getOrElse(new OptionalThrowable)
		val fingerprint = taskDef.fingerprint

		// Check return code and fire appropriate event
		val event = returnCode match {

			case GatlingStatusCodes.success =>
				loggers.map(_.info(s"Simulation $simulationName successful."))
				SimulationSuccessful(className, fingerprint, selector, optionalThrowable, duration)

			case GatlingStatusCodes.assertionsFailed =>
				loggers.map(_.error(s"Simulation $simulationName failed."))
				SimulationFailed(className, fingerprint, selector, optionalThrowable, duration)

			case GatlingStatusCodes.invalidArguments =>
				val formattedArgs = args.mkString("(", "", ")")
				loggers.map(_.error(s"Provided arguments $formattedArgs are not valid."))
				InvalidArguments(className, fingerprint, selector, optionalThrowable, duration)
		}

		eventHandler.handle(event)

		// No new task to launch
		Array.empty[Task]
	}

}
