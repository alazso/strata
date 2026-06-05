package so.alaz.strata.api.pdc

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import org.junit.jupiter.api.Test

class PdcKeyTest {

    private val nsk = NamespacedKey("strata", "test")
    private val type = PersistentDataType.STRING
    private val key = PdcKey(nsk, type)

    @Test
    fun getReadsThroughHolderContainer() {
        val pdc = mockk<PersistentDataContainer>()
        val holder = mockk<PersistentDataHolder>()
        every { holder.persistentDataContainer } returns pdc
        every { pdc.get(nsk, type) } returns "value"

        assertThat(key.get(holder)).isEqualTo("value")
    }

    @Test
    fun setWritesToContainer() {
        val pdc = mockk<PersistentDataContainer>(relaxed = true)
        key.set(pdc, "x")
        verify { pdc.set(nsk, type, "x") }
    }

    @Test
    fun getOrDefaultFallsBack() {
        val pdc = mockk<PersistentDataContainer>()
        every { pdc.getOrDefault(nsk, type, "fallback") } returns "fallback"
        assertThat(key.getOrDefault(pdc, "fallback")).isEqualTo("fallback")
    }
}
