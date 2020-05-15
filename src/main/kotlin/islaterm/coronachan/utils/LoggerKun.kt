package islaterm.coronachan.utils

import org.apache.logging.log4j.Level
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

/**
 * Custom logging configuration factory for Corona-chan.
 *
 * @author [Ignacio Slater Muñoz](islaterm@gmail.com)
 * @version 1.0.1-a.3
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
 * Sets up the custom configuration for Corona-chan.
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
        .add(builder.newLayout("PatternLayout").addAttribute("pattern", "%d [%C] %-5level: %msg%n%throwable"))
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