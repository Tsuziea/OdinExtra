package com.tsuziea.odinextra.events.dispatcher

import com.tsuziea.odinextra.events.TickStart
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

object FabricEventDispatcher {
    init {
        ClientTickEvents.START_CLIENT_TICK.register { TickStart.postAndCatch() }
    }
}
