package com.tsuziea.odinextra.events

import com.odtheking.odin.events.core.Event
import com.tsuziea.odinextra.utils.dungeon.Section

class NewSectionEvent(val previous: Section) : Event