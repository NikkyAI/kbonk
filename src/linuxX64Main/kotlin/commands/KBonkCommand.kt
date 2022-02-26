package commands

import com.github.ajalt.clikt.completion.completionOption
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.parameters.options.versionOption
import fullBinaryPath
import logging.getLogger


class KBonkCommand : CliktCommand(
    name = fullBinaryPath.substringAfterLast("/"),
    invokeWithoutSubcommand = false
) {
    private val logger = getLogger()
    init {
        context {
            helpFormatter = CliktHelpFormatter(
                showDefaultValues = true,
                requiredOptionMarker = "*",
            )
        }
        versionOption(version = "0.0.1")
        completionOption()
        subcommands(
            ServeCommand(),
            TestCommand(),
        )
    }

    override fun run() {
        val invokedSubcommand = currentContext.invokedSubcommand
        if(invokedSubcommand == null) {
            logger.info { "main command" }
            logger.info { "running ${currentContext.command.commandName}" }
        } else {
            logger.info { "subcommand: $invokedSubcommand" }
        }
    }
}