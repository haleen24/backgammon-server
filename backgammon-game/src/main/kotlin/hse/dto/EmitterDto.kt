package hse.dto

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

data class EmitterDto(
    val userId: Int,
    val emitter: SseEmitter
)