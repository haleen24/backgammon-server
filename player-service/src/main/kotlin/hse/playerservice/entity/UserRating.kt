package hse.playerservice.entity

import jakarta.persistence.*

@Entity
@Table(
    schema = "sch1", name = "\"user_rating\"",
)
data class UserRating(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @OneToOne(cascade = [(CascadeType.ALL)])
    val user: User,
    var backgammonDefault: Int,
    var backgammonBlitz: Int,
    var nardeBlitz: Int,
    var nardeDefault: Int,
    var numberOfGames: Int
)