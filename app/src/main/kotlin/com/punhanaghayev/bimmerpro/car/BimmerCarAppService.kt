package com.punhanaghayev.bimmerpro.car

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.model.Action
import androidx.car.app.model.Header
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.validation.HostValidator
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.punhanaghayev.bimmerpro.core.bmw.DtcMemoryType
import com.punhanaghayev.bimmerpro.core.bmw.ExhaustFlapMode
import com.punhanaghayev.bimmerpro.core.protocol.ConnectionState
import com.punhanaghayev.bimmerpro.data.BimmerRepositoryProvider
import kotlinx.coroutines.launch

/**
 * Android Auto Car App service exposing live BMW sensor telemetry,
 * fault memory status, and exhaust flap control safely to iDrive displays.
 */
class BimmerCarAppService : CarAppService() {

    override fun createHostValidator(): HostValidator = HostValidator.ALLOW_ALL_HOSTS_VALIDATOR

    override fun onCreateSession(): Session = BimmerCarSession()
}

class BimmerCarSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen = BimmerCarDashboardScreen(carContext)
}

class BimmerCarDashboardScreen(carContext: CarContext) : Screen(carContext) {

    private val repository = BimmerRepositoryProvider.get(carContext)

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                owner.lifecycleScope.launch {
                    repository.session.collect {
                        invalidate()
                    }
                }
            }
        })
    }

    override fun onGetTemplate(): Template {
        val state = repository.session.value
        val profile = state.vehicleProfile

        if (profile == null || state.connectionState !is ConnectionState.Connected) {
            return MessageTemplate.Builder(
                "Connect to a BMW/MINI adapter or simulator in BMW Expert PRO on your phone to view live gauges and controls."
            )
                .setHeader(
                    Header.Builder()
                        .setTitle("BMW Expert PRO • Android Auto")
                        .setStartHeaderAction(Action.APP_ICON)
                        .build()
                )
                .build()
        }

        val errCount = state.dtcs.count { it.memoryType == DtcMemoryType.ERROR_MEMORY }
        val infoCount = state.dtcs.count { it.memoryType == DtcMemoryType.INFO_MEMORY }
        val permCount = state.dtcs.count { it.memoryType == DtcMemoryType.PERMANENT_MEMORY }
        val flapMode = state.soundTuning.exhaustFlapMode

        val listBuilder = ItemList.Builder()
            .addItem(
                Row.Builder()
                    .setTitle("${profile.modelDisplayName} (${profile.chassis.code})")
                    .addText("VIN: ${profile.vin.takeLast(7)} • %.1f V • %,d km".format(profile.batteryVoltage, profile.odometerKm))
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("DTC Memory: ${state.dtcs.size} total")
                    .addText("Error: $errCount • Info: $infoCount • Permanent: $permCount")
                    .build()
            )
            .addItem(
                Row.Builder()
                    .setTitle("Exhaust Flap: ${flapMode.name}")
                    .addText("Tap to toggle OEM Auto / Always Open")
                    .setOnClickListener {
                        val next = if (flapMode == ExhaustFlapMode.FORCE_OPEN) {
                            ExhaustFlapMode.OEM_AUTO
                        } else {
                            ExhaustFlapMode.FORCE_OPEN
                        }
                        lifecycleScope.launch { repository.setExhaustFlapMode(next) }
                    }
                    .build()
            )

        val configuredChannels = state.androidAutoPidIds
            .mapNotNull { pid -> state.liveChannels.firstOrNull { it.id == pid } }
            .ifEmpty { state.liveChannels.take(3) }

        configuredChannels.take(4).forEach { ch ->
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(ch.name)
                    .addText("%.1f %s".format(ch.currentValue, ch.unit))
                    .build()
            )
        }

        val header = Header.Builder()
            .setTitle("BMW Expert PRO Live")
            .setStartHeaderAction(Action.APP_ICON)
            .addEndHeaderAction(
                Action.Builder()
                    .setTitle("Scan")
                    .setOnClickListener {
                        lifecycleScope.launch { repository.runFullVehicleScan() }
                    }
                    .build()
            )
            .addEndHeaderAction(
                Action.Builder()
                    .setTitle("Clear DTC")
                    .setOnClickListener {
                        lifecycleScope.launch { repository.clearAllDtcs() }
                    }
                    .build()
            )
            .build()

        return ListTemplate.Builder()
            .setHeader(header)
            .setSingleList(listBuilder.build())
            .build()
    }
}
