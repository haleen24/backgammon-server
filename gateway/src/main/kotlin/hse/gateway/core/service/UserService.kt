//package hse.gateway.core.service
//
//import hse.gateway.core.entity.User
//import hse.gateway.core.repository.UserRepository
//import org.springframework.security.crypto.password.PasswordEncoder
//import org.springframework.stereotype.Service
//
//@Service
//class UserService(
//    val userRepository: UserRepository,
////    val passwordEncoder: PasswordEncoder,
//) {
//    fun createUser(user: User) {
//        userRepository.save(user.copy(password = passwordEncoder.encode(user.password)))
//    }
//
//    fun findUser(username: String): User? {
//        return userRepository.findByUsername(username)
//    }
//
//    fun authenticate(login: String, password: String): User? {
//        val user = findUser(login) ?: return null
//        return if (passwordEncoder.matches(password, user.password)) {
//            user
//        } else
//            null
//
//    }
//}