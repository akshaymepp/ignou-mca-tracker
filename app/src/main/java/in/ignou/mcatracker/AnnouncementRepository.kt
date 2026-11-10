package `in`.ignou.mcatracker

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class AnnouncementRepository {
    private val sources = listOf(
        "https://www.ignou.ac.in/announcements/0?nav=6" to "IGNOU Headquarters",
        "https://webservices.ignou.ac.in/assignments/schools/socis/mca_new/mca_new.html" to "SOCIS MCA_NEW Assignments",
        "https://rckozhikode.ignou.ac.in/" to "IGNOU Regional Centre Kozhikode"
    )

    suspend fun fetch(): List<Announcement> = withContext(Dispatchers.IO) {
        val results = mutableListOf<Announcement>()
        sources.forEach { (sourceUrl, sourceName) ->
            try {
                val doc = Jsoup.connect(sourceUrl)
                    .userAgent("Mozilla/5.0 (compatible; IGNOU-MCA-Tracker/1.0)")
                    .timeout(18000).get()
                doc.select("a[href]").forEach anchorLoop@{ a ->
                    val title = a.text().trim().replace(Regex("\\s+"), " ")
                    if (title.length < 12) return@anchorLoop
                    val context = a.closest("tr")?.text()
                        ?: a.parent()?.parent()?.text()
                        ?: a.parent()?.text().orEmpty()
                    val combined = "$title $context".lowercase()
                    if (!isRelevant(combined, sourceName)) return@anchorLoop
                    val href = a.absUrl("href").ifBlank { sourceUrl }
val rawDate = Regex("\\b\\d{1,2}[-/]\\d{1,2}[-/]\\d{4}\\b")
    .find(context)?.value ?: ""

val date = formatAnnouncementDate(rawDate)
                    results += Announcement(title, date, href, sourceName, classify(combined), context.take(260))
                }
            } catch (_: Exception) {
                // A source may be temporarily unavailable; keep results from other sources.
            }
        }
        results.distinctBy { it.url + it.title }.take(150)
    }

    private fun isRelevant(text: String, source: String): Boolean {
        val direct = listOf("mca_new", "mca new", "mcsp-232", "mcsp 232", "mca programme")
        val general = listOf("project", "synopsis", "viva", "term-end", "term end", "tee",
            "assignment", "examination", "exam form", "hall ticket", "result",
            "re-registration", "registration", "practical", "programme guide")
        return direct.any(text::contains) || general.any(text::contains) || source.contains("MCA_NEW")
    }

    private fun classify(text: String): String = when {
        listOf("synopsis", "project", "mcsp", "viva").any(text::contains) -> "Project"
        "assignment" in text -> "Assignments"
        listOf("term-end", "term end", "exam", "examination", "hall ticket", "date sheet").any(text::contains) -> "Exams"
        listOf("result", "grade card").any(text::contains) -> "Results"
        listOf("registration", "re-registration").any(text::contains) -> "Registration"
        else -> "General"
    }
}
