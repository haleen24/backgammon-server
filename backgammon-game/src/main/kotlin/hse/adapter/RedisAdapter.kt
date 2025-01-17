package hse.adapter

import org.springframework.stereotype.Component
import redis.clients.jedis.Jedis

@Component
class RedisAdapter(

    private val jedis: Jedis?
) {

    fun del(id: String): Long {
        return jedis?.del(id) ?: -1
    }

    fun exists(id: String): Boolean {
        return jedis?.exists(id) ?: false
    }

    fun get(id: String): String? {
        return jedis?.get(id)
    }

    fun set(id: String, value: String) {
        jedis?.set(id, value)
    }
}