package co.uniquindio.unityevents.data.repository

import co.uniquindio.unityevents.data.model.UserDto
import co.uniquindio.unityevents.data.model.toDomain
import co.uniquindio.unityevents.domain.model.User
import co.uniquindio.unityevents.domain.repository.UserRepository
import java.util.UUID

/**
 * Implementacion en memoria del repositorio de usuarios.
 *
 * Esta clase almacena los usuarios en una lista mutable en memoria,
 * con datos quemados (hardcoded) para pruebas. En una implementacion
 * real, se conectaria a una base de datos o servicio remoto.
 *
 * Usuarios de prueba pre-cargados:
 * - demo@unityevents.co / Demo1234
 * - admin@unityevents.co / Admin1234
 */
class UserRepositoryImpl : UserRepository {

    /** Lista mutable de usuarios almacenados en memoria */
    private val users = mutableListOf(
        UserDto(
            id = UUID.randomUUID().toString(),
            name = "Usuario Demo",
            email = "demo@unityevents.co",
            password = "Demo1234"
        ),
        UserDto(
            id = UUID.randomUUID().toString(),
            name = "Admin Test",
            email = "admin@unityevents.co",
            password = "Admin1234"
        )
    )

    /** Mapa de codigos de recuperacion: email -> codigo */
    private val recoveryCodes = mutableMapOf<String, String>()

    /** Usuario actualmente autenticado en la sesion */
    private var currentUser: User? = null

    /**
     * Codigo de recuperacion fijo para pruebas.
     * En una implementacion real, se generaria un codigo aleatorio
     * y se enviaria por correo electronico.
     */
    companion object {
        /** Codigo de recuperacion hardcoded para desarrollo */
        const val FIXED_RECOVERY_CODE = "123456"
    }

    /**
     * Autentica un usuario buscando coincidencia de email y contrasena.
     * Si las credenciales son validas, establece el usuario como sesion activa.
     */
    override fun authenticate(email: String, password: String): User? {
        // Buscar usuario con email y contrasena coincidentes (ignorando mayusculas en email)
        val userDto = users.find {
            it.email.equals(email, ignoreCase = true) && it.password == password
        }

        // Si se encontro, convertir a dominio y guardar como usuario actual
        currentUser = userDto?.toDomain()
        return currentUser
    }

    /**
     * Registra un nuevo usuario si el email no esta en uso.
     * Genera un ID unico automaticamente.
     */
    override fun register(name: String, email: String, password: String): Boolean {
        // Verificar que el email no este registrado previamente
        if (emailExists(email)) return false

        // Crear nuevo usuario con ID unico y agregarlo a la lista
        val newUser = UserDto(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email,
            password = password
        )
        users.add(newUser)
        return true
    }

    /**
     * Verifica si un email ya esta registrado (comparacion sin distincion de mayusculas).
     */
    override fun emailExists(email: String): Boolean {
        return users.any { it.email.equals(email, ignoreCase = true) }
    }

    /**
     * Genera un codigo de recuperacion fijo y lo almacena para el email dado.
     * Solo genera el codigo si el email esta registrado.
     */
    override fun generateRecoveryCode(email: String): Boolean {
        // Solo generar codigo si el email existe en el sistema
        if (!emailExists(email)) return false

        // Almacenar el codigo de recuperacion fijo para este email
        recoveryCodes[email.lowercase()] = FIXED_RECOVERY_CODE
        return true
    }

    /**
     * Valida que el codigo de recuperacion proporcionado coincida
     * con el almacenado para el email dado.
     */
    override fun validateRecoveryCode(email: String, code: String): Boolean {
        return recoveryCodes[email.lowercase()] == code
    }

    /**
     * Restablece la contrasena del usuario si el codigo de recuperacion es valido.
     * Despues de restablecer, elimina el codigo de recuperacion usado.
     */
    override fun resetPassword(email: String, code: String, newPassword: String): Boolean {
        // Validar el codigo de recuperacion primero
        if (!validateRecoveryCode(email, code)) return false

        // Buscar el usuario y actualizar su contrasena
        val userDto = users.find { it.email.equals(email, ignoreCase = true) }
        userDto?.password = newPassword

        // Eliminar el codigo de recuperacion ya usado
        recoveryCodes.remove(email.lowercase())

        return userDto != null
    }

    /**
     * Retorna el usuario actualmente autenticado o null si no hay sesion.
     */
    override fun getCurrentUser(): User? = currentUser

    /**
     * Cierra la sesion eliminando la referencia al usuario actual.
     */
    override fun logout() {
        currentUser = null
    }
}
