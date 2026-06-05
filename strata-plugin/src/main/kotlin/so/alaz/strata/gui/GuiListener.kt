package so.alaz.strata.gui

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.AnvilInventory

/**
 * Routes inventory + input events for Strata GUIs by holder identity (never title). Chest clicks are
 * always cancelled (covers shift-click, hotbar/offhand swaps, double-click collect); drags into a
 * menu are blocked; anvil result clicks dispatch the typed text; awaited chat lines are captured and
 * suppressed.
 */
internal class GuiListener(private val manager: DefaultGuiManager) : Listener {

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        when (val holder = event.inventory.holder) {
            is StrataInventoryHolder -> {
                event.isCancelled = true
                if (event.clickedInventory != event.view.topInventory) return
                manager.handleClick(holder.session, event.rawSlot, event.click, event.currentItem)
            }
            is AnvilInputHolder -> {
                event.isCancelled = true
                val anvil = event.inventory as? AnvilInventory ?: return
                manager.handleAnvilClick(holder, anvil, event.whoClicked as Player, event.rawSlot)
            }
        }
    }

    @EventHandler
    fun onDrag(event: InventoryDragEvent) {
        val holder = event.inventory.holder
        if (holder !is StrataInventoryHolder && holder !is AnvilInputHolder) return
        val topSize = event.view.topInventory.size
        if (event.rawSlots.any { it < topSize }) event.isCancelled = true
    }

    @EventHandler
    fun onPrepareAnvil(event: PrepareAnvilEvent) {
        if (event.inventory.holder !is AnvilInputHolder) return
        event.result = manager.buildAnvilResult(event.inventory)
    }

    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        when (val holder = event.inventory.holder) {
            is StrataInventoryHolder -> {
                holder.session.gui().onClose(holder.session)
                manager.untrack(holder.session)
            }
            is AnvilInputHolder -> manager.handleAnvilClose(holder, event.player as Player)
        }
    }

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val text = PlainTextComponentSerializer.plainText().serialize(event.message())
        if (manager.handleChat(event.player, text)) event.isCancelled = true
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        manager.forgetChat(event.player)
    }
}
