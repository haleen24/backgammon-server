package hse.gateway.core.entity

import jakarta.persistence.*

@Entity
@Table(schema = "security")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val username: String,
    val password: String,
    val roles: String,
)