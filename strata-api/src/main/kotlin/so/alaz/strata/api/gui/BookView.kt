package so.alaz.strata.api.gui

import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus

/** Opens a virtual written book (Adventure) for a player — a read-only text surface, no events. */
@ApiStatus.Experimental
public object BookView {

    @JvmStatic
    public fun open(player: Player, title: Component, author: Component, pages: List<Component>) {
        player.openBook(Book.book(title, author, pages))
    }
}
