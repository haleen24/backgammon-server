package hse.user.config

import hse.user.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class CustomUserDetails(
    private val user: User,
) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return ArrayList(user.roles.split(",").map { SimpleGrantedAuthority(it) })
    }

    override fun getPassword(): String {
        return user.password
    }

    override fun getUsername(): String {
        return user.username
    }
}