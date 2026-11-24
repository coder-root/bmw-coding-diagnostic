package com.punhanaghayev.bimmerpro.core.protocol

import com.punhanaghayev.bimmerpro.core.bmw.EcuAddress
import com.punhanaghayev.bimmerpro.core.i18n.AppLanguage

enum class AdapterReadinessGrade {
    EXCELLENT,
    GOOD,
    UNSTABLE,
    CRITICAL
}

data class AdapterHealthReport(
    val adapterName: String,
    val protocolStack: String,
    val pingCount: Int,
    val successCount: Int,
    val minLatencyMs: Long,
    val avgLatencyMs: Long,
    val maxLatencyMs: Long,
    val packetLossPercent: Int,
    val batteryVoltage: Double,
    val voltageStable: Boolean,
    val grade: AdapterReadinessGrade
) {
    fun localizedSummary(language: AppLanguage): String = when (grade) {
        AdapterReadinessGrade.EXCELLENT -> when (language) {
            AppLanguage.EN -> "Coding & Diagnostics Ready (Ultra-Low Latency)"
            AppLanguage.AZ -> "Kodlaşdırma və Diaqnostika üçün Hazırdır (Əla Gecikmə)"
            AppLanguage.RU -> "Готов к кодированию и диагностике (минимальная задержка)"
            AppLanguage.TR -> "Kodlama ve Diagnostik için Mükemmel (Düşük Gecikme)"
        }
        AdapterReadinessGrade.GOOD -> when (language) {
            AppLanguage.EN -> "Stable Connection — Diagnostics & Coding Supported"
            AppLanguage.AZ -> "Sabit Bağlantı — Diaqnostika və Kodlaşdırma Dəstəklənir"
            AppLanguage.RU -> "Стабильное соединение — диагностика и кодирование готовы"
            AppLanguage.TR -> "Kararlı Bağlantı — Diagnostik ve Kodlama Uygun"
        }
        AdapterReadinessGrade.UNSTABLE -> when (language) {
            AppLanguage.EN -> "High Latency / Packet Drops — Check Adapter or Battery"
            AppLanguage.AZ -> "Yüksək Gecikmə / Paket İtkisi — Adapteri Yoxlayın"
            AppLanguage.RU -> "Высокая задержка / потери пакетов — проверьте адаптер"
            AppLanguage.TR -> "Yüksek Gecikme / Paket Kaybı — Adaptör veya Aküyü Kontrol Edin"
        }
        AdapterReadinessGrade.CRITICAL -> when (language) {
            AppLanguage.EN -> "Unsafe for Coding — Connection or Voltage Unstable"
            AppLanguage.AZ -> "Kodlaşdırma üçün Təhlükəlidir — Bağlantı və ya Gərginlik Zəifdir"
            AppLanguage.RU -> "Небезопасно для кодирования — сбой связи или низкое напряжение"
            AppLanguage.TR -> "Kodlama için Riskli — Bağlantı veya Voltaj Kararsız"
        }
    }
}

object AdapterHealthDiagnostics {

    fun evaluate(
        adapterName: String,
        protocolStack: String,
        latenciesMs: List<Long>,
        totalPings: Int,
        batteryVoltage: Double
    ): AdapterHealthReport {
        val successCount = latenciesMs.size
        val minMs = latenciesMs.minOrNull() ?: 0L
        val maxMs = latenciesMs.maxOrNull() ?: 0L
        val avgMs = if (latenciesMs.isNotEmpty()) latenciesMs.sum() / latenciesMs.size else 0L
        val lossPct = if (totalPings > 0) (((totalPings - successCount) * 100) / totalPings).coerceIn(0, 100) else 100
        val voltageOk = batteryVoltage >= 12.2

        val grade = when {
            lossPct >= 40 || !voltageOk || successCount == 0 -> AdapterReadinessGrade.CRITICAL
            lossPct > 0 || avgMs > 180L -> AdapterReadinessGrade.UNSTABLE
            avgMs <= 45L && lossPct == 0 -> AdapterReadinessGrade.EXCELLENT
            else -> AdapterReadinessGrade.GOOD
        }

        return AdapterHealthReport(
            adapterName = adapterName,
            protocolStack = protocolStack,
            pingCount = totalPings,
            successCount = successCount,
            minLatencyMs = minMs,
            avgLatencyMs = avgMs,
            maxLatencyMs = maxMs,
            packetLossPercent = lossPct,
            batteryVoltage = batteryVoltage,
            voltageStable = voltageOk,
            grade = grade
        )
    }

    suspend fun runBenchmark(
        udsClient: UdsClient,
        adapterType: AdapterType,
        batteryVoltage: Double,
        pingCount: Int = 5
    ): AdapterHealthReport {
        val latencies = mutableListOf<Long>()
        val probePayload = UdsCodec.buildTesterPresent(suppressResponse = false)
        for (i in 1..pingCount) {
            val startNs = System.nanoTime()
            val ok = runCatching {
                val resp = udsClient.sendUds(
                    EcuAddress.ZGW_BDC,
                    probePayload,
                    "Adapter Health Ping #$i/$pingCount"
                )
                resp is UdsResponse.Positive
            }.getOrDefault(false)
            val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000L).coerceAtLeast(1L)
            if (ok) {
                latencies.add(elapsedMs)
            }
        }
        return evaluate(
            adapterName = adapterType.displayName,
            protocolStack = adapterType.protocolSummary,
            latenciesMs = latencies,
            totalPings = pingCount,
            batteryVoltage = batteryVoltage
        )
    }
}
