package so.alaz.strata.api.gui

import org.jetbrains.annotations.ApiStatus

/**
 * The kinds of UI surface Strata can drive. `INVENTORY` (chest menus), `ANVIL` (text input), `BOOK`,
 * and `CHAT_PROMPT` are implemented; `DIALOG` (Paper's experimental Dialog API) is reserved.
 */
@ApiStatus.Experimental
public enum class GuiSurface {
    INVENTORY,
    ANVIL,
    BOOK,
    CHAT_PROMPT,
    DIALOG,
}
