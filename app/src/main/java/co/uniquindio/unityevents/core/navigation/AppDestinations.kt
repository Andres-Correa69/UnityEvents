package co.uniquindio.unityevents.core.navigation

/**
 * Identificadores (rutas) usadas por Navigation Compose. Centralizados aqui para evitar
 * cadenas magicas dispersas y facilitar refactors.
 */
object AppDestinations {

    // --- Subgrafos ---
    const val AUTH_GRAPH = "auth_graph"
    const val MAIN_GRAPH = "main_graph"

    // --- Pantallas de autenticacion ---
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val RECOVER_PASSWORD = "recover_password"

    // --- Pantallas principales (bottom nav) ---
    const val HOME = "home"
    const val MY_TICKETS = "my_tickets"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"

    // --- Pantallas pusheadas desde el main ---
    const val EVENT_DETAIL = "event_detail/{eventId}"
    fun eventDetail(id: String) = "event_detail/$id"

    const val CREATE_EVENT = "create_event"

    const val TICKET_DIGITAL = "ticket_digital/{ticketId}"
    fun ticketDigital(id: String) = "ticket_digital/$id"

    const val EDIT_PROFILE = "edit_profile"
    const val SETTINGS = "settings"
    const val LEVELS = "levels"

    // --- Moderacion ---
    const val MODERATOR_DASHBOARD = "moderator_dashboard"
    const val QR_SCANNER = "qr_scanner"
    const val MODERATION_LIST = "moderation_list/{filter}"
    fun moderationList(filter: String) = "moderation_list/$filter"
    const val REPORTS_LIST = "reports_list"
}
