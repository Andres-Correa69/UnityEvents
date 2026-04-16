package co.uniquindio.unityevents.core.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helpers de formato usados en varias pantallas. Centralizados aqui para mantener
 * consistencia de idioma y estilo en toda la app.
 */
object Formatters {

    /** Formato legible de fecha tipo "lun, 14 abril 2026 · 7:00 PM". */
    fun formatEventDate(date: Date?): String {
        if (date == null) return "Fecha por definir"
        return SimpleDateFormat("EEE, d MMM yyyy · h:mm a", Locale("es", "CO")).format(date)
    }

    /** Formato corto tipo "14/04/2026". */
    fun formatShortDate(date: Date?): String {
        if (date == null) return "--/--/----"
        return SimpleDateFormat("dd/MM/yyyy", Locale("es", "CO")).format(date)
    }

    /** Formato de hora tipo "7:00 PM". */
    fun formatTime(date: Date?): String {
        if (date == null) return "--:--"
        return SimpleDateFormat("h:mm a", Locale("es", "CO")).format(date)
    }

    /** Precio como "Gratis" si es 0, o "$ 12.000 COP" con separadores de miles. */
    fun formatPrice(priceCop: Long): String {
        if (priceCop <= 0L) return "Gratis"
        val nf = NumberFormat.getNumberInstance(Locale("es", "CO"))
        return "$ ${nf.format(priceCop)} COP"
    }
}
