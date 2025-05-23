package hse.user.config

import hse.user.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailService(
    private val userRepository: UserRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails {
        val user = username?.let { userRepository.findByUsername(it) }
        return user?.let { CustomUserDetails(it) } ?: throw UsernameNotFoundException("user $username not found")
    }

}