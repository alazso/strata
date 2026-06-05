package so.alaz.strata.api.gui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus
import kotlin.math.ceil
import kotlin.math.max

/**
 * A vertically scrolling chest menu: content fills a rectangular region [columns] wide, scrolling one
 * row at a time. [GuiSession.page] is reused as the top-row offset. Static buttons render every frame.
 */
@ApiStatus.Experimental
public class ScrollGui private constructor(
    private val title: Component,
    private val rows: Int,
    private val staticButtons: Map<Int, Button>,
    private val contentSlots: List<Int>,
    private val columns: Int,
    private val content: List<Button>,
    private val upSlot: Int,
    private val downSlot: Int,
    private val upItem: ItemStack?,
    private val downItem: ItemStack?,
) : Gui {

    override fun title(session: GuiSession): Component = title
    override fun rows(): Int = rows

    private fun visibleRows(): Int = if (columns <= 0) 0 else contentSlots.size / columns
    private fun totalRows(): Int = if (columns <= 0) 0 else ceil(content.size.toDouble() / columns).toInt()

    /** Largest valid top-row offset. */
    public fun maxOffset(): Int = max(0, totalRows() - visibleRows())

    override fun render(session: GuiSession): Map<Int, Button> {
        val offset = session.page().coerceIn(0, maxOffset())
        val result = HashMap<Int, Button>(staticButtons)

        val start = offset * columns
        contentSlots.forEachIndexed { i, slot ->
            content.getOrNull(start + i)?.let { result[slot] = it }
        }

        if (offset > 0 && upItem != null) {
            result[upSlot] = Button(upItem) { click ->
                click.session.setPage(click.session.page() - 1); click.session.refresh(); GuiAction.none()
            }
        }
        if (offset < maxOffset() && downItem != null) {
            result[downSlot] = Button(downItem) { click ->
                click.session.setPage(click.session.page() + 1); click.session.refresh(); GuiAction.none()
            }
        }
        return result
    }

    public class Builder(private val rows: Int) {
        private var title: Component = Component.empty()
        private val staticButtons = HashMap<Int, Button>()
        private var columns: Int = Slot.COLUMNS
        private var contentSlots: List<Int> = (0 until (rows.coerceIn(1, 6)) * Slot.COLUMNS).toList()
        private var content: List<Button> = emptyList()
        private var upSlot: Int = Slot.COLUMNS - 1
        private var downSlot: Int = rows.coerceIn(1, 6) * Slot.COLUMNS - 1
        private var upItem: ItemStack? = null
        private var downItem: ItemStack? = null

        public fun title(title: Component): Builder = apply { this.title = title }
        public fun title(miniMessage: String): Builder = apply { this.title = MiniMessage.miniMessage().deserialize(miniMessage) }
        public fun staticButton(slot: Int, button: Button): Builder = apply { staticButtons[slot] = button }
        public fun content(content: List<Button>): Builder = apply { this.content = content }
        public fun contentRegion(slots: List<Int>, columns: Int): Builder = apply {
            this.contentSlots = slots
            this.columns = columns
        }
        public fun scrollButtons(upSlot: Int, downSlot: Int, upItem: ItemStack, downItem: ItemStack): Builder = apply {
            this.upSlot = upSlot
            this.downSlot = downSlot
            this.upItem = upItem
            this.downItem = downItem
        }

        public fun build(): ScrollGui = ScrollGui(
            title, rows.coerceIn(1, 6), staticButtons.toMap(), contentSlots, columns, content,
            upSlot, downSlot, upItem, downItem,
        )
    }

    public companion object {
        @JvmStatic
        public fun builder(rows: Int): Builder = Builder(rows)
    }
}
