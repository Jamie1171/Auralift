package com.jamiewardle.auralift.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jamiewardle.auralift.R
import com.jamiewardle.auralift.model.SoundPreset

@Composable internal fun presetTitle(preset: SoundPreset) = stringResource(when (preset) {
    SoundPreset.BALANCED -> R.string.preset_balanced; SoundPreset.VOICE -> R.string.preset_voice
    SoundPreset.WARM -> R.string.preset_warm; SoundPreset.DETAIL -> R.string.preset_detail; SoundPreset.CUSTOM -> R.string.preset_custom
})
@Composable internal fun presetSubtitle(preset: SoundPreset) = stringResource(when (preset) {
    SoundPreset.BALANCED -> R.string.balanced_hint; SoundPreset.VOICE -> R.string.voice_hint
    SoundPreset.WARM -> R.string.warm_hint; SoundPreset.DETAIL -> R.string.detail_hint; SoundPreset.CUSTOM -> R.string.custom_hint
})
