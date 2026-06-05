package so.alaz.strata.api.gui

import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.ApiStatus
import java.util.function.Consumer

/** Builds a simple yes/no confirmation menu (3 rows: confirm at slot 11, cancel at slot 15). */
@ApiStatus.Experimental
public object ConfirmGui {

    @JvmStatic
    public fun create(
        title: Component,
        confirmItem: ItemStack,
        cancelItem: ItemStack,
        onConfirm: Consumer<GuiSession>,
        onCancel: Consumer<GuiSession>,
    ): Gui = ChestGui.builder(3)
        .title(title)
        .button(11, Button.of(confirmItem) { click -> onConfirm.accept(click.session); GuiAction.close() })
        .button(15, Button.of(cancelItem) { click -> onCancel.accept(click.session); GuiAction.close() })
        .build()
}
