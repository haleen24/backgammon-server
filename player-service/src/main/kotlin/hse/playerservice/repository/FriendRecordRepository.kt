package hse.playerservice.repository

import hse.playerservice.entity.FriendRecord
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.repository.CrudRepository

interface FriendRecordRepository : CrudRepository<FriendRecord, Int> {

    fun existsFriendRecordByFirstUserAndSecondUser(firstUser: Long, secondUser: Long): Boolean

    @Modifying
    fun deleteFriendRecordByFirstUserAndSecondUser(firstUser: Long, secondUser: Long)
}