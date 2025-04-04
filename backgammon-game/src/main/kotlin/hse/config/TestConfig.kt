package hse.config

import hse.dao.GammonMoveDao
import hse.dao.GammonMoveDaoImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate

@Configuration
class TestConfig {

    @Bean
    fun gammonMoveDao(mongoTemplate: MongoTemplate): GammonMoveDao {
        return GammonMoveDaoImpl(mongoTemplate)
    }
}