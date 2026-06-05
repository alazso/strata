package so.alaz.strata.api.gui

import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus
import java.util.UUID

/** Stable identity for an open GUI, independent of inventory title (which is unreliable). */
@ApiStatus.Experimental
public class GuiSessionId private constructor(public val value: UUID) {
    override fun equals(other: Any?): Boolean = other is GuiSessionId && other.value == value
    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = "GuiSessionId($value)"

    public companion object {
        @JvmStatic
        public fun random(): GuiSessionId = GuiSessionId(UUID.randomUUID())
    }
}

/** A chest-grid coordinate. `index = row * 9 + column`. */
@ApiStatus.Experimental
public class Slot private constructor(public val row: Int, public val column: Int) {
    public val index: Int get() = row * COLUMNS + column

    public companion object {
        public const val COLUMNS: Int = 9

        @JvmStatic
        public fun of(row: Int, column: Int): Slot = Slot(row, column)

        @JvmStatic
        public fun ofIndex(index: Int): Slot = Slot(index / COLUMNS, index % COLUMNS)
    }
}

/**
 * Declarative result of a click — what the framework should do next (AnvilGUI-style). Optionally
 * carries a [sound] and/or [message] played/sent to the viewer before the navigation is applied,
 * chained via [withSound]/[withMessage]: `GuiAction.close().withSound(s).withMessage(m)`.
 */
@ApiStatus.Experimental
public class GuiAction private constructor(
    public val kind: Kind,
    public val target: Gui?,
    public val sound: Sound?,
    public val message: Component?,
) {
    public enum class Kind { NONE, CLOSE, REFRESH, OPEN }

    /** A copy that also plays [sound] for the viewer. */
    public fun withSound(sound: Sound): GuiAction = GuiAction(kind, target, sound, message)

    /** A copy that also sends [message] to the viewer. */
    public fun withMessage(message: Component): GuiAction = GuiAction(kind, target, sound, message)

    public companion object {
        /** Keep the menu open and unchanged (the click is always cancelled). */
        @JvmStatic public fun none(): GuiAction = GuiAction(Kind.NONE, null, null, null)

        /** Close the menu. */
        @JvmStatic public fun close(): GuiAction = GuiAction(Kind.CLOSE, null, null, null)

        /** Re-render the current menu's items. */
        @JvmStatic public fun refresh(): GuiAction = GuiAction(Kind.REFRESH, null, null, null)

        /** Open [gui] for the viewer. */
        @JvmStatic public fun open(gui: Gui): GuiAction = GuiAction(Kind.OPEN, gui, null, null)
    }
}

/** Context handed to a [Button]'s click handler. */
@ApiStatus.Experimental
public class GuiClick(
    public val session: GuiSession,
    public val slot: Int,
    public val clickType: ClickType,
    public val clickedItem: ItemStack?,
) {
    public val player: Player get() = session.viewer()
}

/** Handles a click on a [Button], returning the [GuiAction] to apply. */
@ApiStatus.Experimental
public fun interface GuiClickHandler {
    public fun handle(click: GuiClick): GuiAction
}

/** An item plus its click behaviour. A display-only button does nothing (and the click is cancelled). */
@ApiStatus.Experimental
public class Button @JvmOverloads constructor(
    public val item: ItemStack,
    public val onClick: GuiClickHandler = GuiClickHandler { GuiAction.none() },
) {
    public companion object {
        @JvmStatic public fun display(item: ItemStack): Button = Button(item)

        @JvmStatic public fun of(item: ItemStack, onClick: GuiClickHandler): Button = Button(item, onClick)
    }
}
