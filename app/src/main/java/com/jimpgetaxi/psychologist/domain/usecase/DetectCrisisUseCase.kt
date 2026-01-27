package com.jimpgetaxi.psychologist.domain.usecase

import javax.inject.Inject

class DetectCrisisUseCase @Inject constructor() {

    private val crisisKeywords = listOf(
        // Greek
        "αυτοκτον", "πεθανω", "τελος", "χαπια", "κοψω", "σκοτω", "κρεμαστω", "πηδηξω",
        "δεν αντέχω", "κουραστηκα", "ματαιο", "σκοταδι", "αιμα", "φλεβες",
        // English
        "suicid", "kill myself", "die", "end it", "overdose", "pills", "hang myself",
        "jump", "cannot go on", "tired of living", "hopeless", "cutting", "veins"
    )

    operator fun invoke(message: String): Boolean {
        val normalizedMessage = message.lowercase()
        return crisisKeywords.any { keyword ->
            normalizedMessage.contains(keyword)
        }
    }
}
