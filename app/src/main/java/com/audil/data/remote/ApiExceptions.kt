package com.audil.data.remote

/**
 * Thrown when the API client is used without being configured
 * (no API key or base URL set).
 */
class ApiNotConfiguredException(message: String = "API not configured. Please set your API key and endpoint in Settings.") :
    IllegalStateException(message)

/**
 * Thrown when the API returns HTTP 401 — the API key is invalid or expired.
 */
class ApiAuthenticationException(
    message: String = "Authentication failed (HTTP 401). Check your API key."
) : RuntimeException(message)

/**
 * Thrown when the API returns HTTP 429 — rate limit exceeded.
 */
class ApiRateLimitException(
    message: String = "Rate limit exceeded (HTTP 429). Wait and retry."
) : RuntimeException(message)

/**
 * Thrown when the API returns an unexpected HTTP error.
 */
class ApiServerException(
    val code: Int,
    message: String = "API error (HTTP $code). The server returned an unexpected status."
) : RuntimeException(message)

/**
 * Thrown on network-level failures (DNS, connect timeout, read timeout, etc.).
 */
class ApiNetworkException(
    message: String = "Network error. Check your connection and endpoint URL.",
    cause: Throwable? = null
) : RuntimeException(message, cause)
