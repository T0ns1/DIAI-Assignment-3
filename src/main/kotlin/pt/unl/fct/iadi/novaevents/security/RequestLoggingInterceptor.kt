package pt.unl.fct.iadi.novaevents.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class RequestLoggingInterceptor : HandlerInterceptor {
    private val logger = LoggerFactory.getLogger(RequestLoggingInterceptor::class.java)

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        val authentication = SecurityContextHolder.getContext().authentication
        val principal = if (authentication == null || authentication is AnonymousAuthenticationToken) {
            "anonymous"
        } else {
            authentication.name
        }

        logger.info("[{}] {} {} [{}]", principal, request.method, request.requestURI, response.status)
    }
}
