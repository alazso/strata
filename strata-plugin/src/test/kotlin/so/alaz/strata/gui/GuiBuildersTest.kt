package so.alaz.strata.gui

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Test
import so.alaz.strata.api.gui.Button
import so.alaz.strata.api.gui.ChestGui
import so.alaz.strata.api.gui.GuiAction
import so.alaz.strata.api.gui.GuiSession
import so.alaz.strata.api.gui.LoreBuilder
import so.alaz.strata.api.gui.ScrollGui

class GuiBuildersTest {

    private fun button() = Button(mockk<ItemStack>()) { GuiAction.none() }

    @Test
    fun loreBuilderStripsItalicsAndKeepsOrder() {
        val lore = LoreBuilder().add("<red>one").blank().add("<green>two").build()
        assertThat(lore).hasSize(3)
    }

    @Test
    fun chestPatternFillsSymbolSlotsAndExplicitOverrides() {
        val border = button()
        val center = button()
        val gui = ChestGui.builder(3)
            .pattern(
                "#########",
                "#       #",
                "#########",
            )
            .define('#', border)
            .button(13, center)
            .build()

        val rendered = gui.render(mockk(relaxed = true))
        assertThat(rendered[0]).isSameAs(border)   // top-left from pattern
        assertThat(rendered[26]).isSameAs(border)  // bottom-right from pattern
        assertThat(rendered).doesNotContainKey(10) // inner blank
        assertThat(rendered[13]).isSameAs(center)  // explicit override
    }

    @Test
    fun scrollGuiOffsetsByRow() {
        val content = (0 until 27).map { button() } // 3 rows of 9
        val gui = ScrollGui.builder(3)
            .content(content)
            .contentRegion((0 until 9).toList(), columns = 9) // one visible row
            .scrollButtons(upSlot = 18, downSlot = 26, mockk<ItemStack>(), mockk<ItemStack>())
            .build()

        assertThat(gui.maxOffset()).isEqualTo(2) // 3 total rows - 1 visible

        val session = mockk<GuiSession>(relaxed = true)
        every { session.page() } returns 0
        val top = gui.render(session)
        assertThat(top[0]).isSameAs(content[0])
        assertThat(top).containsKey(26)        // down available
        assertThat(top).doesNotContainKey(18)  // up not available at top

        every { session.page() } returns 1
        val scrolled = gui.render(session)
        assertThat(scrolled[0]).isSameAs(content[9]) // scrolled down one row
        assertThat(scrolled).containsKey(18)         // up available
    }
}
