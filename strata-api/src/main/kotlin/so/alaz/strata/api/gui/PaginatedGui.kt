package so.alaz.strata.api.gui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus
import kotlin.math.ceil
import kotlin.math.max

/**
 * A paged chest menu: a flat list of content [Button]s laid into a set of content slots, with
 * optional previous/next navigation that updates [GuiSession.page] and refreshes. Static buttons
 * (borders, fixed actions) render on every page.
 *
 * Page state lives in the [GuiSession], so one instance can serve many viewers independently.
 */
@ApiStatus.Experimental
public class PaginatedGui private constructor(
    private val title: Component,
    private val rows: Int,
    private val staticButtons: Map<Int, Button>,
    private val contentSlots: List<Int>,
    private val content: List<Button>,
    private val previousSlot: Int,
    private val nextSlot: Int,
    private val previousItem: ItemStack?,
    private val nextItem: ItemStack?,
) : Gui {

    override fun title(session: GuiSession): Component = title
    override fun rows(): Int = rows

    /** Total number of pages (at least 1). */
    public fun pageCount(): Int =
        if (contentSlots.isEmpty()) 1 else max(1, ceil(content.size.toDouble() / contentSlots.size).toInt())

    override fun render(session: GuiSession): Map<Int, Button> {
        val pages = pageCount()
        val page = session.page().coerceIn(0, pages - 1)
        val result = HashMap<Int, Button>(staticButtons)

        val start = page * contentSlots.size
        contentSlots.forEachIndexed { i, slot ->
            content.getOrNull(start + i)?.let { result[slot] = it }
        }

        if (page > 0 && previousItem != null) {
            result[previousSlot] = Button(previousItem) { click ->
                click.session.setPage(click.session.page() - 1)
                click.session.refresh()
                GuiAction.none()
            }
        }
        if (page < pages - 1 && nextItem != null) {
            result[nextSlot] = Button(nextItem) { click ->
                click.session.setPage(click.session.page() + 1)
                click.session.refresh()
                GuiAction.none()
            }
        }
        return result
    }

    public class Builder(private val rows: Int) {
        private var title: Component = Component.empty()
        private val staticButtons = HashMap<Int, Button>()
        private var contentSlots: List<Int> = (0 until (rows.coerceIn(1, 6) - 1) * Slot.COLUMNS).toList()
        private var content: List<Button> = emptyList()
        private var previousSlot: Int = (rows.coerceIn(1, 6) - 1) * Slot.COLUMNS
        private var nextSlot: Int = rows.coerceIn(1, 6) * Slot.COLUMNS - 1
        private var previousItem: ItemStack? = null
        private var nextItem: ItemStack? = null

        public fun title(title: Component): Builder = apply { this.title = title }
        public fun title(miniMessage: String): Builder = apply { this.title = MiniMessage.miniMessage().deserialize(miniMessage) }
        public fun staticButton(slot: Int, button: Button): Builder = apply { staticButtons[slot] = button }
        public fun content(content: List<Button>): Builder = apply { this.content = content }
        public fun contentSlots(slots: List<Int>): Builder = apply { this.contentSlots = slots }

        /** Configures navigation: which slots hold the prev/next buttons and their icons. */
        public fun navigation(previousSlot: Int, nextSlot: Int, previousItem: ItemStack, nextItem: ItemStack): Builder = apply {
            this.previousSlot = previousSlot
            this.nextSlot = nextSlot
            this.previousItem = previousItem
            this.nextItem = nextItem
        }

        public fun build(): PaginatedGui = PaginatedGui(
            title, rows.coerceIn(1, 6), staticButtons.toMap(), contentSlots, content,
            previousSlot, nextSlot, previousItem, nextItem,
        )
    }

    public companion object {
        @JvmStatic
        public fun builder(rows: Int): Builder = Builder(rows)
    }
}
