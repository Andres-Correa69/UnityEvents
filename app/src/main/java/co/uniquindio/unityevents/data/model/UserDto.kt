package co.uniquindio.unityevents.data.model

import co.uniquindio.unityevents.domain.model.User

/**
 * Objeto de transferencia de datos (DTO) para la capa de datos.
 *
 * Representa la forma en que se almacenan los datos del usuario
 * en el origen de datos (en este caso, en memoria).
 *
 * @param id Identificador unico del usuario.
 * @param name Nombre completo del usuario.
 * @param email Correo electronico del usuario.
 * @param password Contrasena del usuario.
 */
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    var password: String
)

/**
 * Convierte un [UserDto] de la capa de datos a un [User] del dominio.
 *
 * @return Instancia de [User] con los mismos datos del DTO.
 */
fun UserDto.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    password = password
)

/**
 * Convierte un [User] del dominio a un [UserDto] de la capa de datos.
 *
 * @return Instancia de [UserDto] con los mismos datos de la entidad de dominio.
 */
fun User.toDto(): UserDto = UserDto(
    id = id,
    name = name,
    email = email,
    password = password
)
