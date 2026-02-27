package co.uniquindio.unityevents.domain.model

/**
 * Entidad de dominio que representa un usuario de la aplicacion UnityEvents.
 *
 * Este modelo es utilizado en la capa de dominio y en la interfaz de usuario
 * para representar la informacion del usuario autenticado.
 *
 * @param id Identificador unico del usuario.
 * @param name Nombre completo del usuario.
 * @param email Correo electronico del usuario, utilizado para autenticacion.
 * @param password Contrasena del usuario.
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val password: String
)
