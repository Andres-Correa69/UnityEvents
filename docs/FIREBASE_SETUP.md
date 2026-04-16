# Configuracion de Firebase para UnityEvents

Guia paso a paso para dejar el backend Firebase operativo en el proyecto. Necesitas hacerla **una sola vez**; despues el codigo ya esta integrado.

Servicios que usaremos en Fase A:

- **Authentication** (Email/Password + Google Sign-In)
- **Cloud Firestore** (perfiles de usuario — `/users/{uid}`)
- **Storage** (pendiente de uso en Fase B/C, pero lo inicializamos desde ya)

Prerequisitos:

- Cuenta Google (con acceso a Firebase Console).
- JDK 11 instalado (necesario para `./gradlew signingReport`).
- [Opcional] [Firebase CLI](https://firebase.google.com/docs/cli) si quieres publicar reglas por terminal (`firebase deploy --only firestore:rules`).

---

## 1. Crear el proyecto en Firebase Console

1. Abre [Firebase Console](https://console.firebase.google.com/).
2. Click en **Add project** (o **Crear un proyecto**).
3. Nombre: `UnityEvents` (o el que quieras; el ID queda a gusto de Google).
4. Si lo pide, acepta los terminos y **desactiva Google Analytics** para simplificar (puedes activarlo luego).
5. Termina el wizard.

---

## 2. Registrar la app Android dentro del proyecto

1. En el dashboard del proyecto, click en el icono **Android** (`</>` — en la seccion "Get started by adding Firebase to your app").
2. **Android package name:** `co.uniquindio.unityevents` (copia tal cual; es el `applicationId` del `app/build.gradle.kts`).
3. **App nickname:** `UnityEvents` (solo etiqueta interna).
4. **Debug signing certificate SHA-1:** ver seccion 3 abajo.
5. Click en **Register app**.

---

## 3. Obtener el SHA-1 del keystore debug

El SHA-1 es obligatorio para que **Google Sign-In** funcione.

En la raiz del repo ejecuta:

```bash
./gradlew signingReport
```

Busca el bloque `Variant: debug` y copia el valor de `SHA1`. Pegalo en el campo correspondiente de Firebase Console.

> Si mas adelante generas un keystore para release, tambien deberas registrar **ese** SHA-1 en Firebase (Project settings -> Your apps -> Add fingerprint).

---

## 4. Descargar `google-services.json`

1. En Firebase Console, despues de registrar la app, haz click en **Download google-services.json**.
2. Coloca el archivo en: `app/google-services.json` (dentro del modulo `:app`).
3. El plugin `com.google.gms.google-services` ya esta aplicado; al sincronizar Gradle leera este JSON automaticamente.

> **Nota:** `app/google-services.json` esta en `.gitignore`. Cada desarrollador lo descarga localmente. Si trabajas en equipo y quieren compartir el mismo proyecto Firebase, envia el archivo por un canal seguro o comparte acceso al proyecto Firebase.

---

## 5. Habilitar metodos de autenticacion

1. Firebase Console -> **Build -> Authentication -> Get started**.
2. En la pestana **Sign-in method**:
   - Habilita **Email/Password** (primer toggle).
   - Habilita **Google**. Al guardarlo Firebase crea un **Web client ID** (formato `xxxxx.apps.googleusercontent.com`). **Copialo**, lo necesitas en el paso 6.

---

## 6. Configurar `local.properties` con el Web Client ID

Abre (o crea) el archivo `local.properties` en la raiz del proyecto y agrega:

```properties
WEB_CLIENT_ID=TU_WEB_CLIENT_ID_AQUI.apps.googleusercontent.com
```

El `app/build.gradle.kts` lo lee y expone como `BuildConfig.WEB_CLIENT_ID`, que `GoogleSignInHelper` usa para pedir el ID token.

> `local.properties` NO se commitea (esta en `.gitignore`). Cada colaborador debe generarlo localmente.

---

## 7. Crear la base de datos Cloud Firestore

1. Firebase Console -> **Build -> Firestore Database -> Create database**.
2. Modo: **Production mode** (las reglas de abajo lo hacen seguro).
3. Ubicacion: `nam5 (us-central)` o `southamerica-east1 (Sao Paulo)` segun preferencia.
4. Click en **Enable**.

### Publicar las reglas

Las reglas iniciales estan en `firestore.rules` (raiz del repo). Tienes dos opciones:

**Opcion A — Firebase Console (mas facil):**

1. Firestore -> pestana **Rules**.
2. Copia el contenido de `firestore.rules` y pegalo en el editor.
3. Click en **Publish**.

**Opcion B — Firebase CLI:**

```bash
firebase login
firebase init firestore   # la primera vez; acepta usar el firestore.rules existente
firebase deploy --only firestore:rules
```

---

## 8. Inicializar Firebase Storage

1. Firebase Console -> **Build -> Storage -> Get started**.
2. Acepta las reglas por defecto (las vas a reemplazar).
3. Selecciona la misma region que Firestore.

### Publicar las reglas

Igual que Firestore, pero con `storage.rules`:

- **Console:** Storage -> Rules -> pegar contenido -> Publish.
- **CLI:** `firebase deploy --only storage`.

---

## 9. Sincronizar Gradle y probar

En la raiz del proyecto:

```bash
./gradlew clean build
./gradlew installDebug
```

Abre la app en el emulador/dispositivo. Deberias ver la pantalla de bienvenida. Pruebas rapidas:

- **Registro con email:** tap "Empezar" -> llena el formulario -> "Crear cuenta". Deberia llevarte a "Proximamente".
- **Verificacion:** en Firebase Console, **Authentication -> Users** debe mostrar la cuenta; **Firestore -> users/{uid}** debe mostrar el documento.
- **Cerrar sesion y entrar de nuevo:** toca "Cerrar sesion" -> "Iniciar sesion" -> deberia autenticarte sin crear usuario nuevo.
- **Recuperacion:** en la pantalla de login, "Olvidaste tu contrasena?" -> revisa tu bandeja de entrada.
- **Google Sign-In:** desde Login o Registro, toca "Continuar con Google" y elige una cuenta. Deberia completar el login y crear un documento Firestore si es la primera vez.

---

## Solucion de problemas

| Sintoma                                                      | Probable causa                                                                |
|--------------------------------------------------------------|-------------------------------------------------------------------------------|
| `DEVELOPER_ERROR` al presionar "Continuar con Google"        | SHA-1 del debug no registrado en Firebase, o `WEB_CLIENT_ID` mal copiado.     |
| "No se pudo iniciar con Google: cancelled"                   | El usuario cerro el selector de cuentas; no es un error real.                 |
| La app arranca en blanco                                     | Falta `app/google-services.json` o el plugin `google-services` no esta aplicado. |
| `Permission denied` al leer/escribir Firestore               | Reglas no publicadas; revisa que el SnapShot coincida con `firestore.rules`.  |
| El correo de recuperacion no llega                           | Revisa spam. Firebase no envia si el email no existe en Authentication.       |

---

## Para Fase B (siguiente iteracion)

Cuando empecemos la Fase B, extenderemos las reglas de Firestore para las colecciones de eventos, comentarios y tickets, y Storage para fotos de perfil/eventos. Esta guia se actualizara en ese momento.
