package so.alaz.strata.metrics

import dev.faststats.ErrorTracker

/**
 * Builds a FastStats [ErrorTracker]. Strata's default anonymizers scrub common secrets (emails,
 * bearer tokens, AWS keys, UUIDs, query-string secrets, JDBC URLs, SQL, filesystem paths) from
 * traces before they leave the server; callers can layer on their own via
 * [so.alaz.strata.api.metrics.MetricsBuilder.addAnonymization].
 */
internal object StrataErrorTracker {

    private val DEFAULT_ANONYMIZERS: List<Pair<String, String>> = listOf(
        "[\\w.-]+@([\\w-]+\\.)+[\\w-]{2,4}" to "[email hidden]",
        "Bearer [A-Za-z0-9._~+/=-]+" to "Bearer [token hidden]",
        "AKIA[0-9A-Z]{16}" to "[aws-key hidden]",
        "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}" to "[uuid hidden]",
        "([?&](?:api_?key|token|secret)=)[^&\\s]+" to "$1[redacted]",
        "jdbc:[^\\s\"']+" to "jdbc:[redacted]",
        "(?i)\\b(SELECT|INSERT|UPDATE|DELETE)\\b[^\\n]{0,200}" to "[sql hidden]",
        "[A-Za-z]:\\\\[^\\s\"']+" to "[path hidden]",
    )

    /** Context-aware tracker with the defaults plus any [customAnonymizers] and [ignoredErrors]. */
    fun build(
        customAnonymizers: List<Pair<String, String>>,
        ignoredErrors: List<Class<out Throwable>>,
    ): ErrorTracker {
        var tracker = ErrorTracker.contextAware()
        for ((pattern, replacement) in DEFAULT_ANONYMIZERS) tracker = tracker.anonymize(pattern, replacement)
        for ((pattern, replacement) in customAnonymizers) tracker = tracker.anonymize(pattern, replacement)
        for (type in ignoredErrors) tracker = tracker.ignoreError(type)
        return tracker
    }
}
