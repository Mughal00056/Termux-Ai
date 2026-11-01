package com.example.data.executor

import android.content.Context
import com.example.domain.model.CommandResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

interface CommandExecutor {
    suspend fun execute(command: String, workingDir: String): CommandResult
}

class LocalProcessExecutor(
    private val context: Context,
    private val termuxBridge: TermuxBridge
) : CommandExecutor {

    override suspend fun execute(command: String, workingDir: String): CommandResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val trimmed = command.trim()

        if (trimmed.isEmpty()) {
            return@withContext CommandResult(
                command = command,
                output = "",
                exitCode = 0,
                durationMs = 0
            )
        }

        // Special in-app commands
        if (trimmed.equals("clear", ignoreCase = true)) {
            return@withContext CommandResult(
                command = command,
                output = "\u001B[2J\u001B[H",
                exitCode = 0,
                durationMs = System.currentTimeMillis() - startTime
            )
        }

        val workDirFile = File(workingDir).let {
            if (it.exists() && it.isDirectory && it.canRead()) it else context.filesDir
        }

        val shell = termuxBridge.getPreferredShell()

        return@withContext try {
            val processBuilder = ProcessBuilder(shell, "-c", trimmed)
            processBuilder.directory(workDirFile)

            val env = processBuilder.environment()
            val existingPath = env["PATH"] ?: "/system/bin:/system/xbin"
            env["PATH"] = "${TermuxBridge.TERMUX_BIN}:$existingPath"
            env["HOME"] = if (termuxBridge.isTermuxInstalled()) TermuxBridge.TERMUX_HOME else context.filesDir.absolutePath
            env["PREFIX"] = "/data/data/com.termux/files/usr"
            env["TERM"] = "xterm-256color"
            env["LANG"] = "en_US.UTF-8"

            val process = processBuilder.start()

            val stdoutBuilder = StringBuilder()
            val stderrBuilder = StringBuilder()

            val outThread = Thread {
                try {
                    BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            stdoutBuilder.append(line).append("\n")
                        }
                    }
                } catch (_: Exception) {}
            }

            val errThread = Thread {
                try {
                    BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            stderrBuilder.append(line).append("\n")
                        }
                    }
                } catch (_: Exception) {}
            }

            outThread.start()
            errThread.start()

            // Wait with safety timeout (30 seconds)
            val exited = process.waitFor(30, java.util.concurrent.TimeUnit.SECONDS)
            if (!exited) {
                process.destroyForcibly()
                stderrBuilder.append("\n[Process terminated: Timeout reached (30s)]")
            }

            outThread.join(1000)
            errThread.join(1000)

            val exitCode = if (exited) process.exitValue() else 124
            val duration = System.currentTimeMillis() - startTime

            // If command failed due to missing binary in Android system sandbox (e.g., pkg, apt, python)
            // and Termux app isn't active, provide helpful, realistic feedback and guidance!
            var outStr = stdoutBuilder.toString()
            var errStr = stderrBuilder.toString()

            if (exitCode != 0 && (errStr.contains("not found") || errStr.contains("No such file"))) {
                val binaryName = trimmed.split(" ").firstOrNull() ?: ""
                if (listOf("pkg", "apt", "npm", "pip", "python", "node", "git", "cargo").contains(binaryName)) {
                    if (!termuxBridge.isTermuxInstalled()) {
                        errStr += "\n💡 Tip: '$binaryName' requires Termux environment. Tap 'Connect Termux' on Home dashboard or install Termux from F-Droid to enable full Linux toolchains."
                    }
                }
            }

            CommandResult(
                command = command,
                output = outStr.trimEnd(),
                error = errStr.trimEnd(),
                exitCode = exitCode,
                durationMs = duration
            )
        } catch (e: Exception) {
            CommandResult(
                command = command,
                output = "",
                error = "Execution error: ${e.localizedMessage ?: e.message}",
                exitCode = 1,
                durationMs = System.currentTimeMillis() - startTime
            )
        }
    }
}
