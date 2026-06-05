package so.alaz.strata.api.gui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus
import java.util.function.Consumer

/**
 * A static chest menu: fixed buttons at fixed slots.
 *
 * ```java
 * Gui gui = ChestGui.builder(3)
 *     .title("<gold>Shop")
 *     .button(13, Button.of(icon, click -> { buy(click.getPlayer()); return GuiAction.close(); }))
 *     .build();
 * StrataApi.gui().open(gui, player);
 * ```
 */
@ApiStatus.Experimental
public class ChestGui private constructor(
    private val title: Component,
    private val rows: Int,
    private val buttons: Map<Int, Button>,
    private val openHandler: Consumer<GuiSession>?,
    private val closeHandler: Consumer<GuiSession>?,
) : Gui {

    override fun title(session: GuiSession): Component = title
    override fun rows(): Int = rows
    override fun render(session: GuiSession): Map<Int, Button> = buttons
    override fun onOpen(session: GuiSession) { openHandler?.accept(session) }
    override fun onClose(session: GuiSession) { closeHandler?.accept(session) }

    public class Builder(private val rows: Int) {
        private var title: Component = Component.empty()
        private val buttons = HashMap<Int, Button>()
        private var patternRows: List<String> = emptyList()
        private val patternSymbols = HashMap<Char, Button>()
        private var openHandler: Consumer<GuiSession>? = null
        private var closeHandler: Consumer<GuiSession>? = null

        public fun title(title: Component): Builder = apply { this.title = title }
        public fun title(miniMessage: String): Builder = apply { this.title = MiniMessage.miniMessage().deserialize(miniMessage) }
        public fun button(slot: Int, button: Button): Builder = apply { buttons[slot] = button }
        public fun button(slot: Slot, button: Button): Builder = button(slot.index, button)

        /** A layout mask, one string per row (9 chars each). Map symbols to buttons via [define]. */
        public fun pattern(vararg rows: String): Builder = apply { patternRows = rows.toList() }
        public fun define(symbol: Char, button: Button): Builder = apply { patternSymbols[symbol] = button }
        public fun define(symbol: Char, item: ItemStack): Builder = define(symbol, Button.display(item))

        public fun onOpen(handler: Consumer<GuiSession>): Builder = apply { openHandler = handler }
        public fun onClose(handler: Consumer<GuiSession>): Builder = apply { closeHandler = handler }

        public fun build(): ChestGui {
            val resolved = HashMap<Int, Button>()
            // Pattern first, then explicit buttons override.
            patternRows.forEachIndexed { row, line ->
                line.forEachIndexed { col, symbol ->
                    patternSymbols[symbol]?.let { resolved[row * Slot.COLUMNS + col] = it }
                }
            }
            resolved.putAll(buttons)
            return ChestGui(title, rows.coerceIn(1, 6), resolved.toMap(), openHandler, closeHandler)
        }
    }

    public companion object {
        @JvmStatic
        public fun builder(rows: Int): Builder = Builder(rows)
    }
}
