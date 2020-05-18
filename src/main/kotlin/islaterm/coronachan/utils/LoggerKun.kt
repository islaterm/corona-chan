package islaterm.coronachan.utils

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.appender.ConsoleAppender
import org.apache.logging.log4j.core.config.Configuration
import org.apache.logging.log4j.core.config.ConfigurationFactory
import org.apache.logging.log4j.core.config.ConfigurationSource
import org.apache.logging.log4j.core.config.Order
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration
import org.apache.logging.log4j.core.config.plugins.Plugin
import java.net.URI
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Corona-chan's childhood friend.
 *
 * Since Corona-chan isn't really talkative Logger-kun speaks for her most of the time, he's really good at expressing
 * his (and her) ideas.
 * But he's never been able to express his feelings for Corona.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.1-a.4
 * @since 1.0
 */
class LoggerKun<in R : Any> : ReadOnlyProperty<R, Logger> {
  var isConfigured = false

  /**
   * Returns the value of the property for the given object.
   * @param thisRef the object for which the value is requested.
   * @param property the metadata for the property.
   * @return the property value.
   */
  override fun getValue(thisRef: R, property: KProperty<*>): Logger {
    if (!isConfigured) {
      ConfigurationFactory.setConfigurationFactory(LoggerKunConfigFactory())
      isConfigured = true
    }
    return LogManager.getLogger(thisRef.javaClass)
  }
}

/**
 * Custom logging configuration factory for Logger-kun.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.1-a.4
 * @since 1.0
 */
@Plugin(name = "LoggerKunConfigFactory", category = ConfigurationFactory.CATEGORY)
@Order(50)
class LoggerKunConfigFactory : ConfigurationFactory() {
  /**
   * Returns the configuration for a given logger.
   */
  override fun getConfiguration(loggerContext: LoggerContext?, source: ConfigurationSource?): Configuration =
    getConfiguration(loggerContext, "$source", null)

  /**
   * Returns the configuration for a given logger using a specified location.
   *
   * If the URI of the specified location is ``null`` then returns the configuration defined in [createConfiguration].
   */
  override fun getConfiguration(loggerContext: LoggerContext?, name: String?, configLocation: URI?): Configuration {
    val builder = ConfigurationBuilderFactory.newConfigurationBuilder()
    return createConfiguration(name!!, builder)
  }

  /**
   * Returns the supported types of configurations as a String array.
   */
  override fun getSupportedTypes() = arrayOf("*")
}

/**
 * Sets up the custom configuration for Logger-kun.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.5-b.3
 * @since 1.0
 */
fun createConfiguration(name: String, builder: ConfigurationBuilder<BuiltConfiguration>): Configuration =
  builder.setConfigurationName(name)
    .setStatusLevel(Level.ERROR)
    .add(
      builder
        .newFilter("ThresholdFilter", Filter.Result.ACCEPT, Filter.Result.NEUTRAL)
        .addAttribute("level", Level.DEBUG)
    )
    .add(
      builder
        .newAppender("Stdout", "CONSOLE")
        .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT)
        .add(builder.newLayout("PatternLayout").addAttribute("pattern", "%d [%C{2}] %-5level: %msg%n%throwable"))
    )
    .add(
      builder
        .newLogger("islaterm.coronachan", Level.DEBUG).add(builder.newAppenderRef("Stdout"))
        .addAttribute("additivity", false)
    )
    .add(
      builder
        .newRootLogger(Level.ERROR)
        .add(builder.newAppenderRef("Stdout"))
    )
    .build()