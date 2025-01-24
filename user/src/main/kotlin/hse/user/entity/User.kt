package hse.user.entity

import jakarta.persistence.*

@Entity
@Table(schema = "sch1")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val username: String,
    val password: String,
    val roles: String,
)