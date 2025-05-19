package hse.playerservice.entity

import jakarta.persistence.*

@Entity
@Table(
    schema = "sch1", name = "\"user_rating\"",
)
data class UserRating(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @OneToOne(cascade = [(CascadeType.ALL)])
    val user: User,
    @Column
    var backgammonDefault: Int,
    @Column
    var backgammonBlitz: Int,
    @Column
    var nardeBlitz: Int,
    @Column
    var nardeDefault: Int,
    @Column
    var numberOfGames: Int
)