package com.example.security

import com.example.domain.model.SafetyCheckResult
import com.example.domain.model.SafetyLevel

object SafetyInterceptor {

    private data class DangerRule(
        val regex: Regex,
        val level: SafetyLevel,
        val patternName: String,
        val reason: String,
        val alternative: String
    )

    private val rules = listOf(
        DangerRule(
            regex = Regex("""\brm\s+-[rRfF]{1,3}\b""", RegexOption.IGNORE_CASE),
            level = SafetyLevel.DANGEROUS,
            patternName = "rm -rf (Recursive file/directory deletion)",
            reason = "This will recursively and permanently delete directories and files without placing them in trash.",
            alternative = "Use 'rm -i' or backup files before deletion, or use trash-cli."
        ),
        DangerRule(
            regex = Regex("""\bdd\s+if=.*""", RegexOption.IGNORE_CASE),
            level = SafetyLevel.DANGEROUS,
            patternName = "dd (Direct disk writing)",
            reason = "Overwrites raw partitions or storage blocks directly, risking total system or data destruction.",
            alternative = "Verify output destination carefully or use safe file transfer tools."
        ),
        DangerRule(
            regex = Regex("""\bmkfs(\.[a-z0-9]+)?\b""", RegexOption.IGNORE_CASE),
            level = SafetyLevel.DANGEROUS,
            patternName = "mkfs (Format filesystem)",
            reason = "Creates a new filesystem on target partition, instantly wiping all existing data.",
            alternative = "Ensure the partition device name is exactly what you intend."
        ),
        DangerRule(
            regex = Regex("""\b(reboot|shutdown|poweroff|init\s+0)\b""", RegexOption.IGNORE_CASE),
            level = SafetyLevel.WARNING,
            patternName = "System reboot or poweroff",
            reason = "Will terminate all active sessions, unsaved processes, and restart or turn off the device/server.",
            alternative = "Save all ongoing work and safely sync disk buffers before running."
        ),
        DangerRule(
            regex = Regex("""\bchmod\s+(-[a-zA-Z]*R[a-zA-Z]*\s+)?(777|000)\b""", RegexOption.IGNORE_CASE),
            level = SafetyLevel.WARNING,
            patternName = "chmod 777 / 000 (Permissive or Lockout permissions)",
            reason = "777 grants full read/write/execute to everyone (security hazard); 000 revokes all access.",
            alternative = "Use targeted permissions like chmod 755 (directories) or chmod 644 (files)."
        ),
        DangerRule(
            regex = Regex("""\bchown\s+-[a-zA-Z]*R[a-zA-Z]*\b""", RegexOption.IGNORE_CASE),
            level = SafetyLevel.WARNING,
            patternName = "chown -R (Recursive ownership change)",
            reason = "Recursively changes user and group ownership for all target files and directories.",
            alternative = "Check target folder path carefully to avoid altering root or system ownership."
        ),
        DangerRule(
            regex = Regex("""\bgit\s+reset\s+--hard\b""", RegexOption.IGNORE_CASE),
            level = SafetyLevel.WARNING,
            patternName = "git reset --hard (Discard git uncommitted work)",
            reason = "Irreversibly destroys all uncommitted changes and tracked edits in the working tree.",
            alternative = "Use 'git stash' to preserve uncommitted work before resetting."
        ),
        DangerRule(
            regex = Regex("""\bgit\s+clean\s+-[a-zA-Z]*f[a-zA-Z]*\b""", RegexOption.IGNORE_CASE),
            level = SafetyLevel.WARNING,
            patternName = "git clean -f (Delete untracked files)",
            reason = "Permanently deletes untracked files from the repository.",
            alternative = "Run 'git clean -n' first to dry-run and see which files would be deleted."
        ),
        DangerRule(
            regex = Regex("""(:\{\s*:\|:&\s*\};:|:\(\)\s*\{\s*:\|:&\s*\};:)"""),
            level = SafetyLevel.BLOCKED,
            patternName = "Fork bomb",
            reason = "Exhausts CPU and process limits, freezing the device or system.",
            alternative = "Do not execute fork bombs."
        ),
        DangerRule(
            regex = Regex("""\bcurl\s+[^|]+\|\s*(ba|z)?sh\b""", RegexOption.IGNORE_CASE),
            level = SafetyLevel.WARNING,
            patternName = "curl | sh (Pipe uninspected remote script to shell)",
            reason = "Executes arbitrary remote script directly without inspecting contents first.",
            alternative = "Download the script with 'curl -O <url>', inspect it with 'cat' or 'less', then run."
        )
    )

    fun checkCommand(command: String): SafetyCheckResult {
        val trimmed = command.trim()
        if (trimmed.isEmpty()) {
            return SafetyCheckResult(SafetyLevel.SAFE, command)
        }

        for (rule in rules) {
            if (rule.regex.containsMatchIn(trimmed)) {
                return SafetyCheckResult(
                    level = rule.level,
                    command = trimmed,
                    matchedPattern = rule.patternName,
                    reason = rule.reason,
                    suggestedAlternative = rule.alternative
                )
            }
        }

        return SafetyCheckResult(
            level = SafetyLevel.SAFE,
            command = trimmed
        )
    }
}
