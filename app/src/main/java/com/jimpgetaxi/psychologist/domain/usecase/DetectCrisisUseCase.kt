package com.jimpgetaxi.psychologist.domain.usecase

import java.text.Normalizer
import java.util.regex.Pattern
import javax.inject.Inject

class DetectCrisisUseCase @Inject constructor() {

    private val crisisKeywords = listOf(
        // Greek (χωρίς τόνους)
        "αυτοκτον", "πεθανω", "τελος", "χαπια", "κοψω", "σκοτω", "κρεμαστω", "πηδηξω",
        "δεν αντεχω", "κουραστηκα", "ματαιο", "σκοταδι", "αιμα", "φλεβες",
        "τελειωσω", "εξαφανιστω", "πονος", "απελπισια",
        // English
        "suicid", "kill myself", "die", "end it", "overdose", "pills", "hang myself",
        "jump", "cannot go on", "tired of living", "hopeless", "cutting", "veins",
        "hurt myself", "pain", "despair"
    )

    operator fun invoke(message: String): Boolean {
        // Αφαίρεση τόνων και μετατροπή σε πεζά
        val normalizedMessage = removeAccents(message).lowercase()
        
        return crisisKeywords.any { keyword ->
            normalizedMessage.contains(keyword)
        }
    }

    private fun removeAccents(input: String): String {
        val nfdNormalizedString = Normalizer.normalize(input, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(nfdNormalizedString).replaceAll("")
    }
}