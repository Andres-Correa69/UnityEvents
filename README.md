# UnityEvents

Aplicacion Android para descubrir y organizar eventos de la Universidad del Quindio (package `co.uniquindio.unityevents`).

**Stack:** Kotlin 2.0.21 · Jetpack Compose · Material 3 · Hilt · Firebase (Auth + Firestore + Storage) · Navigation Compose · Credential Manager.

> Diseno visual: proyecto de Stitch `12495687145835338723` — color semilla `#A436F2`, Plus Jakarta Sans, esquinas redondeadas tipo "pill".

---

## Estado del proyecto

Actualmente se esta trabajando por **fases iterativas**:

| Fase | Alcance                                                                     | Estado            |
|------|------------------------------------------------------------------------------|-------------------|
| A    | Setup base + Autenticacion (Bienvenida, Login, Registro, Recuperar, Google) | **En curso**      |
| B    | Home / Mapa de Eventos, Detalle, Comentarios, Crear Evento, Ticket, QR      | Pendiente         |
| C    | Perfil, Editar Perfil, Ajustes, Reputacion, Niveles                         | Pendiente         |
| D    | Notificaciones, Mensajes                                                    | Pendiente         |
| E    | Moderacion (Panel, Aprobados, Rechazados, Validacion de contenido)           | Pendiente         |

---

## Primeros pasos

1. **Configurar Firebase** — sigue `docs/FIREBASE_SETUP.md` paso por paso.
   Esto implica: crear proyecto en Firebase Console, descargar `app/google-services.json`,
   habilitar Email/Password + Google Sign-In, crear Firestore, publicar `firestore.rules` y `storage.rules`,
   y agregar `WEB_CLIENT_ID` a `local.properties`.

2. **Sincronizar Gradle:**
   ```bash
   ./gradlew clean build
   ```

3. **Instalar en emulador/dispositivo:**
   ```bash
   ./gradlew installDebug
   ```

---

## Estructura del codigo

```
co.uniquindio.unityevents/
├── core/
│   ├── component/          # Composables reutilizables (vacio en Fase A)
│   ├── di/                 # AppModule (Firebase + CredentialManager), RepositoryModule
│   ├── navigation/         # AppDestinations, AppNavHost, AuthNavGraph
│   ├── theme/              # Color.kt, Theme.kt, Type.kt, Shape.kt
│   └── utils/              # GoogleSignInHelper
├── data/
│   ├── model/              # UserDto (Firestore)
│   └── repository/         # AuthRepositoryImpl
├── domain/
│   ├── model/              # User
│   └── repository/         # AuthRepository (interfaz)
├── features/
│   ├── welcome/            # WelcomeScreen
│   ├── login/              # LoginScreen + LoginViewModel
│   ├── register/           # RegisterScreen + RegisterViewModel
│   ├── recover/            # RecoverPasswordScreen + RecoverPasswordViewModel
│   └── home/               # HomePlaceholderScreen (stub para Fase B)
├── MainActivity.kt
└── UnityEventsApp.kt
```

---

## Comandos utiles

```bash
# Compilar sin tests / sin instalar
./gradlew compileDebugKotlin

# Build completo
./gradlew build

# Tests unitarios
./gradlew test

# SHA-1 del keystore debug (para Firebase Console)
./gradlew signingReport

# APK debug firmado -> app/build/outputs/apk/debug/
./gradlew assembleDebug
```

---

## Documentacion adicional

- [`docs/FIREBASE_SETUP.md`](docs/FIREBASE_SETUP.md) — configuracion paso a paso de Firebase Console.
- [`CLAUDE.md`](CLAUDE.md) — reglas arquitecturales del proyecto (estructura, Compose, comentarios en espanol).
- [`firestore.rules`](firestore.rules) y [`storage.rules`](storage.rules) — reglas de seguridad iniciales.
