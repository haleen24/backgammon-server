package hse.config

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableCaching
class CacheConfig {

    companion object {
        const val BACKGAMMON_RUNTIME_DAO_CACHE_NAME = "backgammon-runtime-dao-cache"
    }

    @Bean
    fun backgammonRuntimeDaoCacheManager(): CacheManager {
        return ConcurrentMapCacheManager(BACKGAMMON_RUNTIME_DAO_CACHE_NAME)
    }
}