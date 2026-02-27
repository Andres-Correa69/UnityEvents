package co.uniquindio.unityevents.domain.repository

import co.uniquindio.unityevents.domain.model.User

/**
 * Interfaz del repositorio de usuarios.
 *
 * Define las operaciones disponibles para gestionar usuarios en la aplicacion.
 * La implementacion concreta se encuentra en la capa de datos.
 */
interface UserRepository {

    /**
     * Autentica un usuario con sus credenciales.
     *
     * @param email Correo electronico del usuario.
     * @param password Contrasena del usuario.
     * @return El [User] autenticado si las credenciales son correctas, null en caso contrario.
     */
    fun authenticate(email: String, password: String): User?

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param name Nombre completo del nuevo usuario.
     * @param email Correo electronico del nuevo usuario.
     * @param password Contrasena del nuevo usuario.
     * @return true si el registro fue exitoso, false si el email ya esta registrado.
     */
    fun register(name: String, email: String, password: String): Boolean

    /**
     * Verifica si un correo electronico ya esta registrado en el sistema.
     *
     * @param email Correo electronico a verificar.
     * @return true si el email ya existe, false en caso contrario.
     */
    fun emailExists(email: String): Boolean

    /**
     * Genera y almacena un codigo de recuperacion para el usuario con el email dado.
     *
     * @param email Correo electronico del usuario que solicita recuperacion.
     * @return true si el email existe y el codigo fue generado, false si el email no esta registrado.
     */
    fun generateRecoveryCode(email: String): Boolean

    /**
     * Valida un codigo de recuperacion para un email especifico.
     *
     * @param email Correo electronico del usuario.
     * @param code Codigo de recuperacion a validar.
     * @return true si el codigo es valido para el email dado, false en caso contrario.
     */
    fun validateRecoveryCode(email: String, code: String): Boolean

    /**
     * Restablece la contrasena de un usuario usando un codigo de recuperacion valido.
     *
     * @param email Correo electronico del usuario.
     * @param code Codigo de recuperacion previamente validado.
     * @param newPassword Nueva contrasena para el usuario.
     * @return true si la contrasena fue restablecida exitosamente, false en caso contrario.
     */
    fun resetPassword(email: String, code: String, newPassword: String): Boolean

    /**
     * Obtiene el usuario actualmente autenticado en la sesion.
     *
     * @return El [User] autenticado o null si no hay sesion activa.
     */
    fun getCurrentUser(): User?

    /**
     * Cierra la sesion del usuario actual.
     */
    fun logout()
}
