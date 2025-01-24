package hse.playerservice.entity

import jakarta.persistence.*

@Entity
@Table(schema = "sch1", name = "\"user\"", uniqueConstraints = [UniqueConstraint(columnNames = ["username"])])
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val username: String,
    val password: String,
    val roles: String,
)