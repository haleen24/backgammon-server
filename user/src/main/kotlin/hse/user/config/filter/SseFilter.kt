package hse.user.config.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletOutputStream
import jakarta.servlet.WriteListener
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletResponseWrapper
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class SseFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        if (request.requestURI.contains("/view")) {
            chain.doFilter(request, SseResponseWrapper(response))
        } else {
            chain.doFilter(request, response)
        }
    }

    internal class SseResponseWrapper(response: HttpServletResponse?) : HttpServletResponseWrapper(response) {
        override fun getOutputStream(): ServletOutputStream {
            val sos = super.getOutputStream()
            return object : ServletOutputStream() {
                override fun isReady(): Boolean {
                    return sos.isReady
                }

                override fun setWriteListener(listener: WriteListener) {
                    sos.setWriteListener(listener)
                }

                override fun write(b: Int) {
                    sos.write(b)
                    flush()
                }

                override fun write(b: ByteArray, off: Int, len: Int) {
                    sos.write(b, off, len)
                    flush()
                }

                override fun flush() {
                    sos.flush()
                }

                override fun close() {
                    sos.close()
                }
            }
        }
    }
}