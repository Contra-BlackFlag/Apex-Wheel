package com.racing.controller.network

object QrPayloadParser {
    data class ConnectionTarget(val ip: String, val port: Int)

    fun parse(rawText: String): ConnectionTarget? {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) return null

        try {
            // Check for query parameters: ...ip=...&port=...
            if (trimmed.contains("ip=", ignoreCase = true) && trimmed.contains("port=", ignoreCase = true)) {
                val ipRegex = Regex("""[?&]ip=([^&?#\s]+)""", RegexOption.IGNORE_CASE)
                val portRegex = Regex("""[?&]port=([0-9]+)""", RegexOption.IGNORE_CASE)

                val ip = ipRegex.find(trimmed)?.groupValues?.get(1)?.trim()
                val port = portRegex.find(trimmed)?.groupValues?.get(1)?.toIntOrNull()

                if (!ip.isNullOrBlank() && port != null && port in 1..65535) {
                    return ConnectionTarget(ip, port)
                }
            }

            // Check for JSON: {"ip":"...", "port":...}
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                val ipRegex = Regex(""""ip"\s*:\s*"([^"]+)"""")
                val portRegex = Regex(""""port"\s*:\s*([0-9]+)""")

                val ip = ipRegex.find(trimmed)?.groupValues?.get(1)?.trim()
                val port = portRegex.find(trimmed)?.groupValues?.get(1)?.toIntOrNull()

                if (!ip.isNullOrBlank() && port != null && port in 1..65535) {
                    return ConnectionTarget(ip, port)
                }
            }

            // Check for raw IP:Port or host:port e.g. 192.168.1.50:9876
            val cleaned = trimmed.removePrefix("http://").removePrefix("https://").substringBefore("/")
            val parts = cleaned.split(":")
            if (parts.size == 2) {
                val ip = parts[0].trim()
                val port = parts[1].trim().toIntOrNull()
                if (ip.isNotEmpty() && port != null && port in 1..65535) {
                    return ConnectionTarget(ip, port)
                }
            }
        } catch (_: Exception) {}

        return null
    }
}
