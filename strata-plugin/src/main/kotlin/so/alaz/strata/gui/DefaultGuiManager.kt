package so.alaz.strata.gui

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.AnvilInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import so.alaz.strata.api.gui.AnvilPrompt
import so.alaz.strata.api.gui.AnvilResponse
import so.alaz.strata.api.gui.ChatPrompt
import so.alaz.strata.api.gui.ChatResponse
import so.alaz.strata.api.gui.Gui
import so.alaz.strata.api.gui.GuiAction
import so.alaz.strata.api.gui.GuiClick
import so.alaz.strata.api.gui.GuiManager
import so.alaz.strata.api.gui.GuiSession
import so.alaz.strata.api.gui.GuiSessionId
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

internal class DefaultGuiManager(private val plugin: Plugin) : GuiManager {

    private val sessions = ConcurrentHashMap<UUID, DefaultGuiSession>()
    private val awaitingChat = ConcurrentHashMap<UUID, ChatPrompt>()
    private val reopeningAnvil = Collections.newSetFromMap(ConcurrentHashMap<UUID, Boolean>())

    // --- Chest GUIs ---

    override fun open(gui: Gui, player: Player): GuiSession {
        val session = DefaultGuiSession(GuiSessionId.random(), player, gui, this)
        val holder = StrataInventoryHolder(session)
        val size = gui.rows().coerceIn(1, 6) * 9
        val inventory = Bukkit.createInventory(holder, size, gui.title(session))
        holder.backing = inventory
        session.inventory = inventory

        renderInto(session)
        sessions[player.uniqueId] = session
        player.openInventory(inventory)
        gui.onOpen(session)
        return session
    }

    override fun sessionOf(player: Player): GuiSession? = sessions[player.uniqueId]

    override fun closeAll() {
        sessions.values.toList().forEach { it.viewer().closeInventory() }
    }

    fun render(session: DefaultGuiSession) = renderInto(session)

    private fun renderInto(session: DefaultGuiSession) {
        val inventory = session.inventory ?: return
        inventory.clear()
        for ((slot, button) in session.gui().render(session)) {
            if (slot in 0 until inventory.size) inventory.setItem(slot, button.item)
        }
    }

    fun handleClick(session: DefaultGuiSession, rawSlot: Int, clickType: ClickType, clickedItem: ItemStack?) {
        val button = session.gui().render(session)[rawSlot] ?: return
        applyAction(session, button.onClick.handle(GuiClick(session, rawSlot, clickType, clickedItem)))
    }

    private fun applyAction(session: DefaultGuiSession, action: GuiAction) {
        action.sound?.let { session.viewer().playSound(it) }
        action.message?.let { session.viewer().sendMessage(it) }
        when (action.kind) {
            GuiAction.Kind.NONE -> Unit
            GuiAction.Kind.CLOSE -> session.viewer().closeInventory()
            GuiAction.Kind.REFRESH -> renderInto(session)
            GuiAction.Kind.OPEN -> action.target?.let { open(it, session.viewer()) }
        }
    }

    fun untrack(session: DefaultGuiSession) {
        sessions.remove(session.viewer().uniqueId, session)
    }

    // --- Anvil input ---

    override fun openAnvil(prompt: AnvilPrompt, player: Player) {
        val holder = AnvilInputHolder(prompt)
        val inventory = Bukkit.createInventory(holder, InventoryType.ANVIL, prompt.title)
        holder.backing = inventory
        val left = (prompt.leftItem ?: ItemStack(Material.PAPER)).clone()
        left.editMeta { it.displayName(Component.text(prompt.initialText)) }
        inventory.setItem(0, left)
        player.openInventory(inventory)
    }

    /** Builds the result-slot item so the output is clickable; called from PrepareAnvilEvent. */
    fun buildAnvilResult(inventory: AnvilInventory): ItemStack {
        val text = inventory.renameText ?: ""
        val result = inventory.getItem(0)?.clone() ?: ItemStack(Material.PAPER)
        result.editMeta { it.displayName(Component.text(text)) }
        return result
    }

    fun handleAnvilClick(holder: AnvilInputHolder, inventory: AnvilInventory, player: Player, rawSlot: Int) {
        if (rawSlot != ANVIL_RESULT_SLOT) return
        val text = inventory.renameText ?: ""
        val response = runCatching { holder.prompt.onComplete.complete(player, text) }.getOrElse { AnvilResponse.close() }
        when (response.kind) {
            AnvilResponse.Kind.CLOSE -> player.closeInventory()
            AnvilResponse.Kind.KEEP_OPEN -> Unit
            AnvilResponse.Kind.REPLACE_TEXT -> {
                reopeningAnvil.add(player.uniqueId)
                openAnvil(holder.prompt.withText(response.text ?: ""), player)
                reopeningAnvil.remove(player.uniqueId)
            }
        }
    }

    fun handleAnvilClose(holder: AnvilInputHolder, player: Player) {
        if (player.uniqueId in reopeningAnvil) return
        holder.prompt.onClose?.accept(player)
    }

    // --- Chat input ---

    override fun openChat(prompt: ChatPrompt, player: Player) {
        player.closeInventory()
        prompt.promptMessage?.let { player.sendMessage(it) }
        awaitingChat[player.uniqueId] = prompt
    }

    /** Returns true if [player] was awaiting chat input (so the chat event should be cancelled). */
    fun handleChat(player: Player, message: String): Boolean {
        val prompt = awaitingChat[player.uniqueId] ?: return false
        Bukkit.getGlobalRegionScheduler().run(plugin) { _ ->
            if (message.equals(prompt.cancelToken, ignoreCase = true)) {
                awaitingChat.remove(player.uniqueId)
                prompt.onCancel?.accept(player)
                return@run
            }
            val response = runCatching { prompt.onInput.handle(player, message) }.getOrElse { ChatResponse.end() }
            when (response.kind) {
                ChatResponse.Kind.END -> awaitingChat.remove(player.uniqueId)
                ChatResponse.Kind.RETRY -> prompt.promptMessage?.let { player.sendMessage(it) }
            }
        }
        return true
    }

    fun forgetChat(player: Player) {
        awaitingChat.remove(player.uniqueId)
    }

    private companion object {
        const val ANVIL_RESULT_SLOT = 2
    }
}
