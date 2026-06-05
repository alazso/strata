package so.alaz.strata.gui

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Test
import so.alaz.strata.api.gui.Button
import so.alaz.strata.api.gui.GuiAction
import so.alaz.strata.api.gui.GuiSession
import so.alaz.strata.api.gui.PaginatedGui
import so.alaz.strata.api.gui.Slot

class GuiModelTest {

    @Test
    fun slotIndexMath() {
        assertThat(Slot.of(0, 0).index).isEqualTo(0)
        assertThat(Slot.of(1, 0).index).isEqualTo(9)
        assertThat(Slot.of(2, 4).index).isEqualTo(22)
        val s = Slot.ofIndex(22)
        assertThat(s.row).isEqualTo(2)
        assertThat(s.column).isEqualTo(4)
    }

    @Test
    fun guiActionFactories() {
        assertThat(GuiAction.none().kind).isEqualTo(GuiAction.Kind.NONE)
        assertThat(GuiAction.close().kind).isEqualTo(GuiAction.Kind.CLOSE)
        assertThat(GuiAction.refresh().kind).isEqualTo(GuiAction.Kind.REFRESH)
        val target = mockk<ChestStub>()
        val open = GuiAction.open(target)
        assertThat(open.kind).isEqualTo(GuiAction.Kind.OPEN)
        assertThat(open.target).isSameAs(target)
    }

    private fun button() = Button(mockk<ItemStack>()) { GuiAction.none() }

    @Test
    fun paginatedGuiLaysOutPageAndNavigation() {
        val content = (0 until 20).map { button() }
        val gui = PaginatedGui.builder(2)
            .title("<gray>Items")
            .content(content)
            .contentSlots((0 until 9).toList())
            .navigation(9, 17, mockk<ItemStack>(), mockk<ItemStack>())
            .build()

        assertThat(gui.pageCount()).isEqualTo(3) // ceil(20 / 9)

        val session = mockk<GuiSession>(relaxed = true)

        every { session.page() } returns 0
        val firstPage = gui.render(session)
        assertThat(firstPage.keys).contains(0, 8) // content
        assertThat(firstPage).containsKey(17)      // next present
        assertThat(firstPage).doesNotContainKey(9) // no previous on first page

        every { session.page() } returns 1
        val middlePage = gui.render(session)
        assertThat(middlePage).containsKey(9)  // previous
        assertThat(middlePage).containsKey(17) // next

        every { session.page() } returns 2
        val lastPage = gui.render(session)
        assertThat(lastPage).containsKey(9)       // previous
        assertThat(lastPage).doesNotContainKey(17) // no next on last page
    }
}

/** Marker type so the open() action test has a concrete Gui to reference via mockk. */
internal interface ChestStub : so.alaz.strata.api.gui.Gui
