package com.example.data.ai

import com.example.domain.model.AIAnalysisResult
import com.example.domain.model.AICommandPlan
import com.example.domain.model.AICommandResponse
import com.example.domain.model.AIPlanStep
import com.example.domain.model.StepStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Locale

class OfflineHeuristicProvider : AIProvider {

    override suspend fun generateCommand(prompt: String, terminalContext: String): AICommandResponse = withContext(Dispatchers.Default) {
        delay(300) // slight realistic calculation pause
        val q = prompt.lowercase(Locale.ROOT)

        when {
            q.contains("git install") || q.contains("install git") -> {
                AICommandResponse(
                    command = "pkg install git",
                    explanation = "Installs Git version control system from the Termux package repository.",
                    flags = mapOf("pkg" to "Termux package manager", "install" to "Fetch and setup package"),
                    alternativeCommands = listOf("apt install git", "git --version")
                )
            }
            q.contains("python project") || q.contains("create a python") -> {
                val projectName = extractProjectName(prompt, "myapp")
                AICommandResponse(
                    command = "mkdir $projectName && cd $projectName && touch main.py",
                    explanation = "Creates a directory '$projectName', navigates into it, and creates an entrypoint 'main.py'.",
                    flags = mapOf("mkdir" to "Make directory", "cd" to "Change directory", "touch" to "Create empty file"),
                    alternativeCommands = listOf("python -m venv venv", "python main.py")
                )
            }
            q.contains("install node") || q.contains("install nodejs") -> {
                AICommandResponse(
                    command = "pkg install nodejs",
                    explanation = "Installs Node.js runtime and npm package manager.",
                    flags = mapOf("pkg" to "Package manager", "nodejs" to "Node.js v20+ LTS"),
                    alternativeCommands = listOf("node -v", "npm -v")
                )
            }
            q.contains("install python") -> {
                AICommandResponse(
                    command = "pkg install python",
                    explanation = "Installs Python 3 and pip in your Termux environment.",
                    flags = mapOf("python" to "Python interpreter", "pip" to "Python package installer"),
                    alternativeCommands = listOf("pip install --upgrade pip", "python3 --version")
                )
            }
            q.contains("update") || q.contains("upgrade") -> {
                AICommandResponse(
                    command = "pkg update && pkg upgrade -y",
                    explanation = "Refreshes all Termux package lists and upgrades installed software to the newest versions.",
                    flags = mapOf("-y" to "Assume yes to all prompts without interactive pause"),
                    alternativeCommands = listOf("apt update", "apt upgrade")
                )
            }
            q.contains("check ram") || q.contains("memory") -> {
                AICommandResponse(
                    command = "free -m",
                    explanation = "Displays total, used, and available RAM in megabytes.",
                    flags = mapOf("-m" to "Show metrics in megabytes"),
                    alternativeCommands = listOf("cat /proc/meminfo", "top -n 1")
                )
            }
            q.contains("storage") || q.contains("disk") -> {
                AICommandResponse(
                    command = "df -h",
                    explanation = "Displays file system disk space usage in human-readable gigabytes and megabytes.",
                    flags = mapOf("-h" to "Human readable sizes (e.g. 1K, 234M, 2G)"),
                    alternativeCommands = listOf("du -sh *", "ls -lh")
                )
            }
            q.contains("web server") || q.contains("http server") -> {
                AICommandResponse(
                    command = "python -m http.server 8080",
                    explanation = "Launches a local lightweight HTTP static file server on port 8080.",
                    flags = mapOf("-m http.server" to "Python built-in web server module", "8080" to "Target listening port"),
                    alternativeCommands = listOf("npx serve", "php -S 0.0.0.0:8080")
                )
            }
            q.contains("ip") || q.contains("network") || q.contains("ports") -> {
                AICommandResponse(
                    command = "ip addr show",
                    explanation = "Prints network interfaces, IPv4, and IPv6 assigned addresses.",
                    flags = mapOf("show" to "Display network interface properties"),
                    alternativeCommands = listOf("netstat -tuln", "ping -c 3 8.8.8.8")
                )
            }
            q.contains("weather") -> {
                AICommandResponse(
                    command = "curl wttr.in",
                    explanation = "Queries the clean ASCII weather forecasting service for your current location via curl.",
                    flags = mapOf("curl" to "Network transfer tool"),
                    alternativeCommands = listOf("curl wttr.in/London")
                )
            }
            else -> {
                AICommandResponse(
                    command = "pkg search \"$prompt\"",
                    explanation = "Searches the available Termux package repository for software matching '$prompt'.",
                    flags = mapOf("search" to "Find packages by keyword"),
                    alternativeCommands = listOf("man $prompt", "which $prompt")
                )
            }
        }
    }

    override suspend fun explainCommand(command: String): AICommandResponse = withContext(Dispatchers.Default) {
        delay(250)
        val trimmed = command.trim()

        if (trimmed.startsWith("chmod")) {
            return@withContext AICommandResponse(
                command = trimmed,
                explanation = "This command modifies file system access permissions.\n\n755 permission breakdown:\n• Owner (7 = 4+2+1): Read + Write + Execute\n• Group (5 = 4+1): Read + Execute\n• Others (5 = 4+1): Read + Execute\n\nThe '-R' flag applies changes recursively to all subdirectories and child files.",
                flags = mapOf(
                    "-R" to "Recursive into directories",
                    "755" to "User: rwx (7), Group: r-x (5), Others: r-x (5)"
                )
            )
        }

        if (trimmed.startsWith("git")) {
            return@withContext AICommandResponse(
                command = trimmed,
                explanation = "Git version control command.\nOperates on local tracking and remote repository branches.",
                flags = mapOf("git" to "Distributed version control tool")
            )
        }

        if (trimmed.startsWith("pkg") || trimmed.startsWith("apt")) {
            return@withContext AICommandResponse(
                command = trimmed,
                explanation = "Termux Package Manager command for installing, upgrading, and maintaining packages on Android.",
                flags = mapOf("pkg" to "Termux front-end wrapper for apt")
            )
        }

        AICommandResponse(
            command = trimmed,
            explanation = "Executes the binary command '$trimmed' in the terminal shell environment with standard POSIX arguments.",
            flags = mapOf("shell" to "Standard execution")
        )
    }

    override suspend fun analyzeError(command: String, output: String, exitCode: Int): AIAnalysisResult = withContext(Dispatchers.Default) {
        delay(300)
        val combined = "$command\n$output".lowercase(Locale.ROOT)

        when {
            combined.contains("keystore") || combined.contains("debug.keystore") -> {
                AIAnalysisResult(
                    summary = "Missing or misconfigured Android debug signing keystore",
                    rootCause = "The debug signing configuration in the project's build.gradle references a keystore file that does not exist in the root directory.",
                    suggestedFix = "Generate a standard debug keystore using keytool or ensure debug.keystore is created.",
                    fixCommand = "keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname \"CN=Android Debug,O=Android,C=US\"",
                    explanation = "Android Gradle Plugin requires a valid keystore to sign debug APKs before emulator deployment."
                )
            }
            combined.contains("command not found") || combined.contains("not found") -> {
                val missingCmd = command.split(" ").firstOrNull() ?: "package"
                AIAnalysisResult(
                    summary = "Binary executable '$missingCmd' is not installed",
                    rootCause = "The shell could not locate '$missingCmd' in the system PATH.",
                    suggestedFix = "Install the package using the Termux package manager.",
                    fixCommand = "pkg install $missingCmd",
                    explanation = "Once installed, the executable will be added to /data/data/com.termux/files/usr/bin and become globally executable."
                )
            }
            combined.contains("permission denied") -> {
                AIAnalysisResult(
                    summary = "Insufficient file system execution or read permissions",
                    rootCause = "The target script or file does not have the executable bit (+x) set, or belongs to another user.",
                    suggestedFix = "Grant execution permission using chmod.",
                    fixCommand = "chmod +x ${command.split(" ").lastOrNull() ?: "script.sh"}",
                    explanation = "Adding '+x' sets the executable bit in the file's inode metadata."
                )
            }
            combined.contains("address already in use") || combined.contains("eaddrinuse") -> {
                AIAnalysisResult(
                    summary = "Port already bound by another background process",
                    rootCause = "A previous server instance is still actively listening on the target port.",
                    suggestedFix = "Find and kill the process holding the port.",
                    fixCommand = "lsof -i :8080 && fuser -k 8080/tcp",
                    explanation = "Terminates the previous zombie daemon so your new instance can bind to the socket."
                )
            }
            else -> {
                AIAnalysisResult(
                    summary = "Process exited with code $exitCode",
                    rootCause = "An error occurred during command execution. Check standard error output above.",
                    suggestedFix = "Check input parameters or inspect with verbose logging.",
                    fixCommand = "$command --verbose",
                    explanation = "Running with verbose or debug flags exposes deeper diagnostic stack traces."
                )
            }
        }
    }

    override suspend fun generateAgentPlan(goal: String, systemInfo: String): AICommandPlan = withContext(Dispatchers.Default) {
        delay(400)
        val q = goal.lowercase(Locale.ROOT)

        val steps = when {
            q.contains("react") || (q.contains("node") && q.contains("app")) -> {
                listOf(
                    AIPlanStep(
                        stepNumber = 1,
                        description = "Check and install Node.js runtime",
                        command = "pkg install nodejs -y"
                    ),
                    AIPlanStep(
                        stepNumber = 2,
                        description = "Initialize new Vite React project",
                        command = "npm create vite@latest my-react-app -- --template react"
                    ),
                    AIPlanStep(
                        stepNumber = 3,
                        description = "Install npm dependencies",
                        command = "cd my-react-app && npm install"
                    ),
                    AIPlanStep(
                        stepNumber = 4,
                        description = "Start React development preview server",
                        command = "npm run dev -- --host"
                    )
                )
            }
            q.contains("python") -> {
                listOf(
                    AIPlanStep(
                        stepNumber = 1,
                        description = "Install Python and pip packages",
                        command = "pkg install python -y"
                    ),
                    AIPlanStep(
                        stepNumber = 2,
                        description = "Create project directory and virtual environment",
                        command = "mkdir python_app && cd python_app && python -m venv venv"
                    ),
                    AIPlanStep(
                        stepNumber = 3,
                        description = "Activate environment and write sample starter script",
                        command = "echo 'print(\"Hello from AI Termux!\")' > main.py"
                    ),
                    AIPlanStep(
                        stepNumber = 4,
                        description = "Execute Python entrypoint",
                        command = "python main.py"
                    )
                )
            }
            else -> {
                listOf(
                    AIPlanStep(
                        stepNumber = 1,
                        description = "Update Termux package repositories",
                        command = "pkg update -y"
                    ),
                    AIPlanStep(
                        stepNumber = 2,
                        description = "Setup necessary toolchain for: $goal",
                        command = "pkg install git curl -y"
                    ),
                    AIPlanStep(
                        stepNumber = 3,
                        description = "Verify system environment and toolchain",
                        command = "uname -a && git --version"
                    )
                )
            }
        }

        AICommandPlan(
            goal = goal,
            steps = steps
        )
    }

    private fun extractProjectName(prompt: String, fallback: String): String {
        val words = prompt.split(" ")
        val calledIndex = words.indexOfFirst { it.equals("called", true) || it.equals("named", true) }
        if (calledIndex != -1 && calledIndex + 1 < words.size) {
            return words[calledIndex + 1].replace(Regex("[^a-zA-Z0-9_-]"), "")
        }
        return fallback
    }
}
