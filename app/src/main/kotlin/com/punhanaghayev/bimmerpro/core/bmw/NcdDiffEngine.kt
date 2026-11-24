package com.punhanaghayev.bimmerpro.core.bmw

/**
 * Computes deterministic NCD byte-level before/after diffs for staged Easy Mode presets
 * and Expert CAFD parameters so users can audit every byte change before ECU flash/write.
 */
data class NcdByteDiffEntry(
    val ecuName: String,
    val parameterName: String,
    val byteOffsetHex: String,
    val bitMaskHex: String,
    val beforeHex: String,
    val afterHex: String,
    val beforeLabel: String,
    val afterLabel: String
)

object NcdDiffEngine {

    fun computeStagedDiff(
        presets: List<CodingPreset>,
        cafds: List<CafdParameter>,
        stagedPresets: Map<String, Int>,
        stagedCafds: Map<String, String>
    ): List<NcdByteDiffEntry> {
        val diffs = mutableListOf<NcdByteDiffEntry>()

        // 1. Preset diffs (matched to CAFD parameter when available, or deterministic parameter byte)
        stagedPresets.forEach { (presetId, newIdx) ->
            val preset = presets.firstOrNull { it.id == presetId } ?: return@forEach
            if (newIdx == preset.currentOptionIndex) return@forEach
            val linkedCafd = cafds.firstOrNull { it.functionName.equals(preset.cafdKey, ignoreCase = true) }
            val beforeLabel = preset.options.getOrElse(preset.currentOptionIndex) { "0x%02X".format(preset.currentOptionIndex) }
            val afterLabel = preset.options.getOrElse(newIdx) { "0x%02X".format(newIdx) }

            val offset = linkedCafd?.byteOffset ?: (preset.cafdKey.hashCode() and 0x7F)
            val mask = linkedCafd?.bitMask ?: 0xFF
            val beforeByte = "0x%02X".format(preset.currentOptionIndex and 0xFF)
            val afterByte = "0x%02X".format(newIdx and 0xFF)

            diffs.add(
                NcdByteDiffEntry(
                    ecuName = preset.targetEcu,
                    parameterName = preset.cafdKey,
                    byteOffsetHex = "0x%02X".format(offset),
                    bitMaskHex = "0x%02X".format(mask),
                    beforeHex = beforeByte,
                    afterHex = afterByte,
                    beforeLabel = beforeLabel,
                    afterLabel = afterLabel
                )
            )
        }

        // 2. Expert CAFD diffs
        stagedCafds.forEach { (cafdId, newOptionLabel) ->
            val param = cafds.firstOrNull { it.id == cafdId } ?: return@forEach
            if (newOptionLabel == param.selectedOptionLabel) return@forEach
            val beforeHex = param.availableOptions[param.selectedOptionLabel] ?: param.currentHex
            val afterHex = param.availableOptions[newOptionLabel] ?: "0x01"
            diffs.add(
                NcdByteDiffEntry(
                    ecuName = param.ecuName,
                    parameterName = "${param.section}::${param.functionName}",
                    byteOffsetHex = "0x%02X".format(param.byteOffset),
                    bitMaskHex = "0x%02X".format(param.bitMask),
                    beforeHex = beforeHex,
                    afterHex = afterHex,
                    beforeLabel = param.selectedOptionLabel,
                    afterLabel = newOptionLabel
                )
            )
        }

        return diffs
    }
}
