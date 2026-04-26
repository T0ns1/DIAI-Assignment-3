package pt.unl.fct.iadi.novaevents.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TestJwtService {
    private val jwtService = JwtService(
        "test-jwt-secret-key-for-hs256-signing-minimum-32-bytes",
        60_000
    )

    @Test
    fun `generate and validate round trips claims`() {
        val token = jwtService.generate("alice", listOf("ROLE_EDITOR"))

        val claims = jwtService.validate(token)

        assertEquals("alice", claims?.get("name"))
        assertEquals(listOf("ROLE_EDITOR"), claims?.get("roles"))
    }

    @Test
    fun `validate returns null for invalid token`() {
        assertNull(jwtService.validate("not-a-token"))
    }
}
