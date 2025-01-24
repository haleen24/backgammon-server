package hse.playerservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import hse.playerservice.annotations.PlayerIntegrationTest
import hse.playerservice.repository.UserRepository
import hse.playerservice.service.JwtService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import player.response.JwtResponse


@PlayerIntegrationTest
class UserControllerTest {

    @LocalServerPort
    val port: Int = 80

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userRepository: UserRepository

    @MockitoBean
    lateinit var jwtService: JwtService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        Mockito.`when`(jwtService.generateToken(any())).thenReturn("token")
    }

    companion object {
        const val TOKEN = "token"
    }

    @Test
    fun `create user test`() {
        mockMvc.perform(
            post("http://localhost:$port/create").contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "test",
                      "password" : "123",
                      "roles": "user"
                    }
                """.trimIndent()
                )
        ).andExpect {
            val jwtResponse = objectMapper.readValue(it.response.contentAsString, JwtResponse::class.java)
            assertEquals(TOKEN, jwtResponse.token)
            assertEquals(1, jwtResponse?.userId)
        }

        val user = userRepository.findByUsername("test")
        assertNotNull(user)
        assertEquals(1, user!!.id)
    }

    @Test
    fun `login test`() {
        `create user test`()
        Mockito.`when`(jwtService.generateToken(any())).thenReturn("token2")
        mockMvc.perform(
            post("http://localhost:$port/login").contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "username": "test",
                      "password" : "123"
                    }
                """.trimIndent()
                )
        ).andExpect {
            val jwtResponse = objectMapper.readValue(it.response.contentAsString, JwtResponse::class.java)
            assertEquals(1, jwtResponse?.userId)
            assertEquals("token2", jwtResponse.token)
        }
    }
}