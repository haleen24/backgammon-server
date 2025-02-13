package hse.config

import hse.dao.GammonMoveDao
import hse.dao.GammonMoveDaoImpl
import hse.dao.GammonMoveRuntimeDao
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate

@Configuration
class TestConfig {

    @Bean
    fun gammonMoveDao(@Value("\${debug}") debug: Boolean, mongoTemplate: MongoTemplate?): GammonMoveDao {
        if (debug) {
            return GammonMoveRuntimeDao()
        }
        return GammonMoveDaoImpl(mongoTemplate!!)
    }
}