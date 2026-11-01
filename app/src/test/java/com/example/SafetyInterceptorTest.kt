package com.example

import com.example.data.ai.OfflineHeuristicProvider
import com.example.domain.model.SafetyLevel
import com.example.security.SafetyInterceptor
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SafetyInterceptorTest {

    @Test
    fun testSafeCommandsPassWithoutConfirmation() {
        val safeCommands = listOf(
            "ls -la",
            "pwd",
            "git status",
            "python main.py",
            "pkg update",
            "df -h",
            "free -m"
        )
        for (cmd in safeCommands) {
            val result = SafetyInterceptor.checkCommand(cmd)
            assertEquals("Expected SAFE for: $cmd", SafetyLevel.SAFE, result.level)
            assertFalse(result.requiresConfirmation)
        }
    }

    @Test
    fun testDangerousCommandsIntercepted() {
        val rmResult = SafetyInterceptor.checkCommand("rm -rf /storage/emulated/0")
        assertEquals(SafetyLevel.DANGEROUS, rmResult.level)
        assertTrue(rmResult.requiresConfirmation)

        val ddResult = SafetyInterceptor.checkCommand("dd if=/dev/zero of=/dev/block/sda")
        assertEquals(SafetyLevel.DANGEROUS, ddResult.level)
        assertTrue(ddResult.requiresConfirmation)

        val mkfsResult = SafetyInterceptor.checkCommand("mkfs.ext4 /dev/sda1")
        assertEquals(SafetyLevel.DANGEROUS, mkfsResult.level)
        assertTrue(mkfsResult.requiresConfirmation)

        val rebootResult = SafetyInterceptor.checkCommand("reboot")
        assertEquals(SafetyLevel.WARNING, rebootResult.level)
        assertTrue(rebootResult.requiresConfirmation)

        val forkBomb = SafetyInterceptor.checkCommand(":(){ :|:& };:")
        assertEquals(SafetyLevel.BLOCKED, forkBomb.level)
        assertTrue(forkBomb.requiresConfirmation)
    }

    @Test
    fun testOfflineHeuristicProviderGeneratesExpectedCommands() = runBlocking {
        val provider = OfflineHeuristicProvider()

        val gitResponse = provider.generateCommand("Git install karna hai")
        assertEquals("pkg install git", gitResponse.command)

        val nodeResponse = provider.generateCommand("Install nodejs")
        assertEquals("pkg install nodejs", nodeResponse.command)

        val chmodExplanation = provider.explainCommand("chmod -R 755 project")
        assertTrue(chmodExplanation.explanation.contains("755"))
        assertTrue(chmodExplanation.explanation.contains("Owner"))

        val errorAnalysis = provider.analyzeError("./gradlew assembleDebug", "Missing debug.keystore", 1)
        assertTrue(errorAnalysis.rootCause.contains("keystore"))
    }
}
