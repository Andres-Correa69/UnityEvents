package co.uniquindio.unityevents.domain.model

/**
 * Resultado del analisis automatico de contenido (Gemini) sobre un evento.
 *
 * Se usa como pre-filtro ANTES de que el moderador humano vea el evento. La IA es solo
 * una primera capa de defensa: el moderador siempre tiene la palabra final.
 *
 * @property approved true si el contenido pasa el filtro automatico y se puede continuar
 *                    al flujo normal (creacion con status PENDING para moderador humano).
 * @property reasons Lista de motivos legibles cuando approved=false. Vacia si approved=true.
 */
data class ModerationResult(
    val approved: Boolean,
    val reasons: List<String> = emptyList(),
    /**
     * true cuando la IA NO pudo verificar y aplicamos fail-open. Lo usamos para mostrar
     * un mensaje distinto en la UI: "verificado por IA" vs "no se pudo verificar, pasa al
     * moderador humano". Asi el usuario sabe en que estado real esta el contenido.
     */
    val isFailOpen: Boolean = false
) {
    companion object {
        /**
         * "Fail-open": si la IA falla (sin red, timeout, JSON invalido, modelo 404, etc.)
         * NO bloqueamos la creacion del evento. El moderador humano es el filtro final,
         * por lo que es preferible dejar pasar contenido cuestionable que impedir publicar
         * eventos legitimos por una falla tecnica.
         */
        fun failOpen() = ModerationResult(approved = true, reasons = emptyList(), isFailOpen = true)
    }
}
