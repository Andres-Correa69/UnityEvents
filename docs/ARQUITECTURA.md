# UnityEvents — Documentación de arquitectura

Guía técnica completa del proyecto. Pensada para sustentación: lees de arriba a abajo y entiendes **qué hace cada archivo y por qué está donde está**.

---

## Tabla de contenido

1. [Visión general](#1-visión-general)
2. [Stack y justificación](#2-stack-y-justificación)
3. [Estructura de paquetes](#3-estructura-de-paquetes)
4. [Capa de dominio (`domain/`)](#4-capa-de-dominio-domain)
5. [Capa de datos (`data/`)](#5-capa-de-datos-data)
6. [Capa de features (`features/`)](#6-capa-de-features-features)
7. [Núcleo compartido (`core/`)](#7-núcleo-compartido-core)
8. [Inyección de dependencias (Hilt)](#8-inyección-de-dependencias-hilt)
9. [Navegación](#9-navegación)
10. [Modelo de datos en Firestore](#10-modelo-de-datos-en-firestore)
11. [Seguridad (reglas Firestore y Storage)](#11-seguridad-reglas-firestore-y-storage)
12. [Sistema de reputación](#12-sistema-de-reputación)
13. [Flujos principales paso a paso](#13-flujos-principales-paso-a-paso)
14. [Archivos de configuración](#14-archivos-de-configuración)

---

## 1. Visión general

**UnityEvents** es una app Android para que la comunidad de la Universidad del Quindío descubra, cree y asista a eventos. Los eventos se ubican en un mapa, se pueden comentar y calificar, y cada asistente recibe un ticket digital con código QR que el organizador escanea en la entrada.

Hay tres roles:

| Rol | Capacidades |
|-----|-------------|
| **Usuario** | Explorar eventos en el mapa, crear eventos (quedan pendientes de moderar), obtener tickets, comentar y calificar, ganar puntos. |
| **Organizador** (cualquier usuario que creó al menos un evento) | Además de lo anterior: escanear QR de los asistentes de **sus** eventos para validar entradas. |
| **Moderador** | Panel de moderación para aprobar/rechazar eventos y revisar reportes. |

---

## 2. Stack y justificación

| Tecnología | Para qué | Por qué |
|-----------|----------|---------|
| **Kotlin 2.0.21** | Lenguaje | Estándar en Android desde 2019; null-safety, coroutines, flows. |
| **Jetpack Compose** | UI declarativa | Recomendado oficial de Google desde 2021, reemplaza XML. |
| **Material 3 (Material You)** | Sistema de diseño | Consistente con Android 12+, soporte nativo de tema oscuro y colores dinámicos. |
| **Hilt 2.53.1** | Inyección de dependencias | Oficial de Android, basado en Dagger; reduce boilerplate. |
| **KSP** | Procesador de anotaciones | Más rápido que kapt; recomendado para Hilt desde Kotlin 2.0. |
| **Firebase Auth** | Login (email + Google) | Backend gestionado, sin servidor propio. |
| **Cloud Firestore** | Base de datos NoSQL reactiva | Listeners en tiempo real con `Flow`, reglas declarativas. |
| **Firebase Storage** | Almacén de imágenes | Integrado con Firestore, reglas similares. |
| **Google Maps Compose** | Mapa de eventos | Integración oficial de Google Maps con Compose. |
| **Credential Manager** | Google Sign-In | API moderna que reemplaza al deprecado `GoogleSignInClient`. |
| **CameraX + ML Kit Barcode** | Escáner de QR | CameraX maneja el ciclo de vida, ML Kit detecta los QR. |
| **Coil** | Carga de imágenes remotas | Ligero, Kotlin-first, Compose integrado. |
| **ZXing** | Generación de QR | Generar el bitmap del QR del ticket. |

---

## 3. Estructura de paquetes

Raíz: `co.uniquindio.unityevents` en `app/src/main/java/`. La organización sigue **Clean Architecture** en 3 capas + core compartido:

```
co.uniquindio.unityevents/
├── core/                   # Código común, independiente de features
│   ├── component/          # Composables reutilizables en toda la app
│   ├── di/                 # Módulos de Hilt (infraestructura DI)
│   ├── navigation/         # NavHost, rutas, subgrafos
│   ├── theme/              # Color, Theme, Typography, Shape
│   └── utils/              # Utilidades (Formatters, QR, Location, Reputation, etc.)
│
├── data/                   # Capa de datos: DTOs y repositorios de Firebase
│   ├── model/              # DTOs que serializan/deserializan con Firestore
│   └── repository/         # Implementaciones concretas (AuthRepositoryImpl, etc.)
│
├── domain/                 # Lógica de negocio: entidades y contratos
│   ├── model/              # Entidades de dominio (User, Event, Ticket, etc.)
│   └── repository/         # Interfaces (AuthRepository, EventsRepository...)
│
├── features/               # Una carpeta por feature de UI
│   ├── welcome/            # Pantalla de bienvenida
│   ├── login/              # Login con email y Google
│   ├── register/           # Registro
│   ├── recover/            # Recuperar contraseña
│   ├── home/               # Mapa de eventos (pantalla principal)
│   ├── events/             # Crear evento + Detalle de evento
│   ├── tickets/            # Mis tickets + Ticket digital + Escáner QR
│   ├── profile/            # Perfil, editar, ajustes, niveles
│   ├── notifications/      # Bandeja de notificaciones
│   └── moderation/         # Panel del moderador, listas, reportes
│
├── MainActivity.kt         # Única Activity del proyecto
└── UnityEventsApp.kt       # Application (@HiltAndroidApp)
```

### ¿Por qué Clean Architecture?

- **`domain/`** no depende de nada (ni Android, ni Firebase). Contiene la lógica pura del problema. Puede testearse con JUnit sin emulador.
- **`data/`** implementa las interfaces de `domain/` usando Firebase. Si mañana cambiamos a otro backend, solo cambian las clases aquí.
- **`features/`** (la UI) depende de `domain/`, **no** de `data/`. Los ViewModels reciben `AuthRepository` (interfaz), no `AuthRepositoryImpl`.
- **`core/`** centraliza lo común para que las features no se reimplementen widgets.

---

## 4. Capa de dominio (`domain/`)

Son **clases Kotlin puras** sin dependencias de Android/Firebase. Representan el problema.

### `domain/model/`

| Archivo | Qué representa |
|---------|----------------|
| `User.kt` | Usuario autenticado: `uid`, `displayName`, `email`, `photoUrl`, **`role`** (USER/MODERATOR/ADMIN), `level`, `points`, `bio`, `city`. |
| `Event.kt` | Evento: título, descripción, categoría, lugar, **`latitude`/`longitude`** (geolocalización), fecha, precio, capacidad, `imageUrl`, organizador, **`status`** (PENDING/APPROVED/REJECTED), `rejectionReason`, `attendeesCount`. |
| `Comment.kt` | Comentario en un evento: autor, texto, **`rating`** (1..5), `createdAt`, `flagged`. |
| `Ticket.kt` | Ticket del asistente: `eventId`, `userId`, `purchasedAt`, **`usedAt`** (null hasta que se escanea), `qrPayload`. |
| `Notification.kt` | Notificación en bandeja: `type` (EVENT_APPROVED, EVENT_REJECTED, NEW_COMMENT, TICKET_PURCHASED…), `title`, `body`, `relatedId`, `read`. |
| `Report.kt` | Reporte de contenido: `reporterId`, `targetType` (EVENT/COMMENT), `reason`, `status`. |

### `domain/repository/`

Solo **interfaces**. Definen lo que la app necesita hacer, sin decir cómo.

| Interfaz | Qué hace |
|----------|----------|
| `AuthRepository` | Login email/Google, registro, recuperar contraseña, sign-out, observar estado de auth. |
| `EventsRepository` | CRUD de eventos + filtros por estado, crear con imagen a Storage. |
| `CommentsRepository` | Comentarios por evento (listar, agregar, borrar, reportar). |
| `TicketsRepository` | Comprar ticket, listar mis tickets, marcar usado (scan). |
| `NotificationsRepository` | Listar/marcar leídas las notificaciones del usuario. |
| `ProfileRepository` | Observar usuario actual, actualizar perfil, subir foto. |
| `ReportsRepository` | CRUD de reportes para moderadores. |

Todos los métodos de lectura devuelven `Flow<T>` (reactivos). Las operaciones de escritura devuelven `Result<T>` para manejar errores explícitamente.

---

## 5. Capa de datos (`data/`)

### `data/model/` (DTOs)

Los DTOs son **espejos** de los documentos Firestore. Tienen constructor vacío y `var` (requisitos de `toObject()` de Firestore) y funciones `toDomain()` / `fromDomain()` para convertir a la entidad de dominio.

Cada entidad de dominio tiene su DTO: `UserDto`, `EventDto`, `CommentDto`, `TicketDto`, `NotificationDto`, `ReportDto`.

Ejemplo `EventDto.kt`:
- Tiene los mismos campos que `Event` pero como `var`
- `status` y enums van como `String` (Firestore no soporta enums)
- `createdAt: Date?` con `@ServerTimestamp` para que Firestore lo asigne del servidor

### `data/repository/` (Implementaciones)

Cada `*Impl` implementa la interfaz del dominio usando Firebase.

| Clase | Responsabilidad |
|-------|-----------------|
| `AuthRepositoryImpl` | Usa `FirebaseAuth` + `FirebaseFirestore`. Crea el documento en `/users/{uid}` al registrarse. Para Google convierte el `idToken` en credencial Firebase. |
| `EventsRepositoryImpl` | Queries a `/events`. Al crear evento sube la imagen a `Storage` y guarda la URL. Al aprobar/rechazar, notifica al organizador y otorga puntos. |
| `CommentsRepositoryImpl` | Subcolección `/events/{eventId}/comments`. Al comentar otorga puntos y notifica al organizador. |
| `TicketsRepositoryImpl` | CRUD de `/tickets`. Al comprar incrementa `attendeesCount` del evento. Al marcar usado valida que el ticket sea del evento correcto y otorga +30 al asistente. |
| `NotificationsRepositoryImpl` | Subcolección `/users/{uid}/notifications`. Batch para marcar todas como leídas. |
| `ProfileRepositoryImpl` | Combina `FirebaseAuth` (uid, emailVerified) con el doc `/users/{uid}` de Firestore. |
| `ReportsRepositoryImpl` | `/reports`. Solo moderadores leen; cualquiera puede crear. |

Todas usan `callbackFlow { ... }` para convertir los `addSnapshotListener` de Firestore en `Flow`. Esto permite que la UI reaccione en vivo.

---

## 6. Capa de features (`features/`)

Cada feature tiene:
- **`XScreen.kt`** — un Composable que dibuja la pantalla.
- **`XViewModel.kt`** — un ViewModel con Hilt que expone `StateFlow<XUiState>` y métodos públicos llamados por la UI.
- Un **`XUiState`** (data class) que agrupa todo lo que la pantalla necesita mostrar.

### Patrón MVVM + UDF (Unidirectional Data Flow)

```
Usuario → onClick() → ViewModel.method()
                         ↓
                     Repository (suspend)
                         ↓
                     Firestore (async)
                         ↓
                     Flow emit
                         ↓
                     _state.update { ... }
                         ↓
            StateFlow.collectAsStateWithLifecycle()
                         ↓
                     Screen recompose
```

### Inventario de features

| Feature | Pantalla | Descripción |
|---------|----------|-------------|
| `welcome` | WelcomeScreen | Bienvenida con botones "Empezar" y "Iniciar sesión". |
| `login` | LoginScreen + ViewModel | Email+password, Google Sign-In, link a recuperar/registrar. |
| `register` | RegisterScreen + ViewModel | Nombre, email, password, confirm; valida formato. |
| `recover` | RecoverPasswordScreen + ViewModel | Envía correo de reset a través de Firebase. |
| `home` | HomeScreen + ViewModel | **Mapa con markers** de eventos, buscador + chips de filtro (Todos/Mis eventos/categorías). |
| `events` | CreateEventScreen + ViewModel | Form completo con **picker de ubicación en mapa** y botón GPS. |
| `events` | EventDetailScreen + ViewModel | Imagen, info, descripción, organizador, comentarios con estrellas, botón ticket (o escanear, si eres organizador). |
| `tickets` | MyTicketsScreen + ViewModel | Lista de tickets del usuario. |
| `tickets` | TicketDigitalScreen + ViewModel | Muestra el QR grande del ticket para validar entrada. |
| `tickets` | QrScannerScreen + ViewModel | Cámara + ML Kit. Valida que el QR sea del evento y marca usado. |
| `profile` | ProfileScreen + ViewModel | Avatar, nivel, puntos, stats, botones a Editar/Niveles/Panel Moderación/Ajustes. |
| `profile` | EditProfileScreen + ViewModel | Editar nombre/bio/ciudad/foto. |
| `profile` | LevelsScreen | Explica el sistema de puntos y niveles, muestra progreso actual. |
| `profile` | SettingsScreen + ViewModel | Opciones y cerrar sesión. |
| `notifications` | NotificationsScreen + ViewModel | Bandeja con badge en bottom nav. |
| `moderation` | ModeratorDashboardScreen + ViewModel | Contadores por estado. |
| `moderation` | ModerationListScreen + ViewModel | Aprobar/rechazar eventos pendientes. |
| `moderation` | ReportsListScreen + ViewModel | Validar reportes de contenido. |

---

## 7. Núcleo compartido (`core/`)

### `core/theme/`

| Archivo | Responsabilidad |
|---------|-----------------|
| `Color.kt` | Tokens de color tomados de Stitch: `BrandPrimary = #A436F2`, `BrandPrimaryBright`, `BrandPrimarySoft`, neutrals, semantic. |
| `Theme.kt` | `UnityEventsTheme` composable. Usa `lightColorScheme` con los tokens; soporta `dynamicColorScheme` si se habilita. Controla los íconos del status bar. |
| `Type.kt` | `Typography` con **Plus Jakarta Sans** descargada con `GoogleFont.Provider` (no empaquetada). Escalas Material 3 (display, headline, title, body, label). |
| `Shape.kt` | Radios grandes (8/12/16/24/32 dp) — apariencia "pill" del diseño de Stitch. |

### `core/component/`

Composables reutilizables (sin ViewModel, solo visuales):

| Archivo | Qué dibuja |
|---------|-----------|
| `EventCard.kt` | Tarjeta de evento con imagen, chip de categoría, **badge de estado** si no está APROBADO, fecha, lugar, asistentes, precio. |
| `CategoryChip.kt` | Chip pill con color primario para categorías. |
| `StatusBadge.kt` | Insignia "En revisión" / "Rechazado" / "Aprobado" con color. |
| `StarRating.kt` | Estrellas (1..5) editables o solo lectura. |
| `UserAvatar.kt` | Avatar circular: foto o iniciales. |
| `LoadingBox.kt` | Progress indicator centrado. |
| `EmptyState.kt` | Placeholder para listas vacías. |
| `AppBottomBar.kt` | Bottom navigation con 4 tabs (Home/Tickets/Alertas/Perfil) + badge de no leídas. |

### `core/utils/`

| Archivo | Para qué |
|---------|----------|
| `Formatters.kt` | Fechas en español, precios "Gratis" / "$ 12.000 COP". |
| `GoogleSignInHelper.kt` | Envuelve `Credential Manager` + `GoogleIdOption`; pide un ID token a Google. |
| `LocationHelper.kt` | Envuelve `FusedLocationProviderClient` para obtener la ubicación actual. |
| `QrCodeGenerator.kt` | Usa ZXing para generar un bitmap con el QR del ticket. |
| `ReputationService.kt` | **Motor de reputación**: `award(userId, points)` incrementa `points` de un usuario con `FieldValue.increment` y recalcula `level`. Catálogo de recompensas: `EVENT_APPROVED=50`, `COMMENT_ADDED=10`, `COMMENT_5_STARS=20`, `TICKET_SCANNED=30`. |

### `core/navigation/`

| Archivo | Qué hace |
|---------|----------|
| `AppDestinations.kt` | Constantes y helpers de rutas (`home`, `event_detail/{eventId}`, `qr_scanner/{eventId}`, etc.). |
| `AuthNavGraph.kt` | Subgrafo de autenticación (welcome → login/register/recover). |
| `AppNavHost.kt` | NavHost raíz. Decide grafo inicial (auth o main) según sesión. Muestra el bottom bar solo en las 4 tabs. |
| `BottomBarBadgeViewModel.kt` | ViewModel auxiliar para el número de notificaciones no leídas del bottom bar. |

---

## 8. Inyección de dependencias (Hilt)

`core/di/`:

### `AppModule.kt`
Provee singletons para todo el árbol de DI:

```kotlin
@Provides @Singleton fun provideFirebaseAuth() = Firebase.auth
@Provides @Singleton fun provideFirebaseFirestore() = Firebase.firestore
@Provides @Singleton fun provideFirebaseStorage() = Firebase.storage
@Provides @Singleton fun provideCredentialManager(context) = CredentialManager.create(context)
```

### `RepositoryModule.kt`
Hace el binding **interfaz → implementación** (así los ViewModels piden la interfaz y Hilt entrega la impl):

```kotlin
@Binds abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
@Binds abstract fun bindEventsRepository(impl: EventsRepositoryImpl): EventsRepository
// ... uno por cada repo
```

### Anotaciones clave

- `@HiltAndroidApp` en `UnityEventsApp` → habilita Hilt para toda la app.
- `@AndroidEntryPoint` en `MainActivity` → permite `@Inject lateinit var`.
- `@HiltViewModel` + `@Inject constructor(...)` en cada ViewModel.
- `hiltViewModel()` en los Composables para pedir un ViewModel al grafo.

---

## 9. Navegación

Se usa **Navigation Compose** con rutas string y argumentos por path.

```
AppNavHost (raíz)
├── AUTH_GRAPH (cuando no hay sesión)
│   ├── welcome
│   ├── login
│   ├── register
│   └── recover_password
│
└── MAIN_GRAPH (cuando hay sesión)
    ├── home                ← bottom tab (mapa)
    ├── my_tickets          ← bottom tab
    ├── notifications       ← bottom tab
    ├── profile             ← bottom tab
    │
    ├── event_detail/{eventId}
    ├── create_event
    ├── ticket_digital/{ticketId}
    ├── qr_scanner/{eventId}        ← solo lo abre el organizador
    │
    ├── edit_profile
    ├── settings
    ├── levels
    │
    └── moderator_dashboard         ← visible solo si role=MODERATOR
        ├── moderation_list/{filter}
        └── reports_list
```

El bottom bar aparece solo cuando la ruta actual está en `{home, my_tickets, notifications, profile}`.

---

## 10. Modelo de datos en Firestore

```
/users/{uid}
    displayName, email, photoUrl, role, level, points, bio, city, createdAt, updatedAt
    │
    └── /notifications/{notifId}
        type, title, body, relatedId, read, createdAt

/events/{eventId}
    title, description, category, placeName, address,
    latitude, longitude, startDate, endDate, price, capacity,
    imageUrl, organizerId, organizerName, organizerPhotoUrl,
    status, rejectionReason, createdAt, attendeesCount
    │
    └── /comments/{commentId}
        authorId, authorName, authorPhotoUrl, text, rating, createdAt, flagged

/tickets/{ticketId}
    eventId, eventTitle, eventImageUrl, eventStartDate,
    userId, userName, purchasedAt, usedAt, qrPayload

/reports/{reportId}
    reporterId, targetType, targetId, targetPreview, reason, status, createdAt
```

### Storage

```
/users/{uid}/avatar.jpg              ← foto de perfil
/events/{organizerUid}/{eventId}.jpg ← imagen del evento
```

---

## 11. Seguridad (reglas Firestore y Storage)

Archivos: `firestore.rules` y `storage.rules` en la raíz del repo.

### Helpers (Firestore)

```javascript
function isSignedIn()     → request.auth != null
function isSelf(uid)      → request.auth.uid == uid
function role()           → get(/users/$(auth.uid)).data.role
function isModerator()    → role() in ['MODERATOR', 'ADMIN']
```

### Reglas resumen

| Path | Lectura | Escritura |
|------|---------|-----------|
| `/users/{uid}` | Cualquier autenticado | Dueño, moderador, o cualquiera que solo toque `points`/`level`/`updatedAt` (para el sistema de reputación). |
| `/users/{uid}/notifications/{id}` | Solo el dueño | Crear: cualquier autenticado. Modificar/borrar: solo el dueño. |
| `/events/{eventId}` | Cualquier autenticado | Crear: el autor con `status='PENDING'`. Update: autor, moderador, o cualquiera que solo toque `attendeesCount` (para que otros compren tickets). |
| `/events/{eventId}/comments/{id}` | Cualquier autenticado | Crear: con su propio `authorId`. Update/delete: autor o moderador. |
| `/tickets/{id}` | Dueño, organizador del evento, o moderador | Crear: el propio usuario. Update: organizador del evento o moderador (para escanear). |
| `/reports/{id}` | Solo moderador | Crear: cualquier autenticado. Update: moderador. |

### Reglas de Storage

- `/users/{uid}/**` → lee cualquier autenticado, escribe el dueño.
- `/events/{organizerId}/**` → lee cualquier autenticado, escribe el organizador.
- Todo lo demás denegado.

---

## 12. Sistema de reputación

Implementado en `core/utils/ReputationService.kt`, llamado desde los repositorios al ocurrir eventos de dominio.

### Recompensas

| Acción | Puntos | Beneficiario |
|--------|--------|--------------|
| Tu evento es aprobado por un moderador | +50 | Organizador |
| Escribes un comentario | +10 | Comentarista |
| Tu evento recibe un comentario de 5 estrellas | +20 | Organizador |
| Tu ticket es validado al entrar al evento | +30 | Asistente |

### Niveles

Fórmula: `level = (points / 500) + 1`, con mínimo 1 y máximo 5.

| Nivel | Puntos | Beneficio |
|-------|--------|-----------|
| 1 | 0–499 | Miembro inicial |
| 2 | 500–999 | Avatar con marco destacado |
| 3 | 1000–1999 | Hasta 3 eventos simultáneos |
| 4 | 2000–3999 | Badge de "organizador experto" |
| 5 | 4000+ | Acceso anticipado a nuevas features |

### Por qué usar `FieldValue.increment`

Dos usuarios que reciben puntos simultáneamente no pisan el valor del otro. Firestore garantiza la operación atómica en el servidor.

---

## 13. Flujos principales paso a paso

### 13.1 Registro de usuario

1. `WelcomeScreen` → "Empezar" → `RegisterScreen`.
2. El usuario llena el form; `RegisterViewModel.onRegisterClick()` valida.
3. `AuthRepositoryImpl.registerWithEmail()`:
   - `firebaseAuth.createUserWithEmailAndPassword(...)`.
   - `updateProfile(displayName)`.
   - `firestore.collection("users").document(uid).set(UserDto(...))`.
4. El ViewModel detecta éxito → la UI navega al `MAIN_GRAPH`.

### 13.2 Crear evento

1. `HomeScreen` → FAB "Crear evento" → `CreateEventScreen`.
2. Usuario llena todos los campos y **toca el mapa** o pulsa "Usar mi ubicación" (Fused Location) → `onLocationPicked(lat, lng)`.
3. Usuario toca "Publicar".
4. `CreateEventViewModel.onSubmit()`:
   - Valida (título, descripción ≥20 chars, lugar, ubicación, fecha).
   - Llama a `EventsRepositoryImpl.createEvent(event, imageUri)`.
5. Repo:
   - `collection.document()` genera id.
   - Sube imagen a `Storage/events/{uid}/{id}.jpg`.
   - `docRef.set(EventDto.fromDomain(event).copy(status=PENDING))`.
6. Éxito → navegación a detalle del evento. El evento está **en revisión**.

### 13.3 Aprobación por moderador

1. `ProfileScreen` → "Panel de moderación" (visible solo si `role=MODERATOR`).
2. `ModeratorDashboardScreen` → tarjeta "Eventos pendientes".
3. `ModerationListScreen` → "Aprobar".
4. `ModerationListViewModel.approve(eventId)` → `EventsRepositoryImpl.updateStatus(eventId, APPROVED)`.
5. Repo:
   - Actualiza el doc con `status=APPROVED`.
   - Llama a `ReputationService.award(organizerId, 50)`.
   - Crea una notificación `EVENT_APPROVED` en la bandeja del organizador.
6. El organizador:
   - Ve la notificación en la pestaña Alertas.
   - Sus `points` suben +50 en Firestore, `level` se recalcula.
   - Su evento aparece en el mapa público.

### 13.4 Obtener y usar un ticket

1. Usuario toca un evento aprobado → `EventDetailScreen` → "Obtener ticket".
2. `EventDetailViewModel.onBuyTicketClick()` → `TicketsRepositoryImpl.purchaseTicket(event, userId, userName)`.
3. Repo:
   - Crea doc en `/tickets/{ticketId}` con `qrPayload = ticketId`.
   - Incrementa `attendeesCount` del evento (regla especial permite esto a cualquier autenticado).
   - Crea notificación `TICKET_PURCHASED`.
4. Usuario navega a `TicketDigitalScreen` que muestra el QR grande (ZXing).
5. En la entrada, el organizador abre su propio `EventDetailScreen` → botón "Escanear tickets" (visible solo si `currentUser.uid == event.organizerId`).
6. `QrScannerScreen` arranca la cámara, ML Kit detecta el QR.
7. `QrScannerViewModel.onScanned(payload)` → `TicketsRepositoryImpl.markTicketUsed(ticketId, expectedEventId)`.
8. Repo:
   - Valida que `ticket.eventId == expectedEventId` (si no, rechaza).
   - Marca `usedAt = now()`.
   - `ReputationService.award(attendeeUid, 30)`.
9. UI muestra dialog "Ticket validado ✓".

### 13.5 Comentar un evento

1. Usuario va al detalle del evento, escribe un comentario, toca estrellas, envía.
2. `EventDetailViewModel.onSubmitComment()` → `CommentsRepositoryImpl.addComment(comment)`.
3. Repo:
   - Guarda el comentario en `/events/{eventId}/comments/{id}`.
   - `ReputationService.award(authorId, 10)`.
   - Si `rating == 5`: `ReputationService.award(organizerId, 20)`.
   - Crea notificación `NEW_COMMENT` en la bandeja del organizador.
4. Los "efectos colaterales" (puntos + notificación) están en un `runCatching` interno: si alguno falla, el comentario queda bien igual.

---

## 14. Archivos de configuración

| Archivo | Qué configura |
|---------|---------------|
| `gradle/libs.versions.toml` | Catálogo único de versiones y dependencias. Todo cambio de librería empieza aquí. |
| `build.gradle.kts` (raíz) | Plugins globales `apply false`. |
| `app/build.gradle.kts` | Dependencias, Hilt, KSP, Compose, manifest placeholders (inyecta `MAPS_API_KEY` y `WEB_CLIENT_ID`). |
| `app/src/main/AndroidManifest.xml` | Permisos (INTERNET, CAMERA, LOCATION), registro de `MainActivity`, meta-data con `MAPS_API_KEY`. |
| `local.properties` | **No se commitea.** Contiene `WEB_CLIENT_ID` (Google Sign-In) y `MAPS_API_KEY` (Google Maps). |
| `app/google-services.json` | **No se commitea.** Configuración del proyecto Firebase. Se descarga desde Firebase Console. |
| `firestore.rules` | Reglas de seguridad de Cloud Firestore. |
| `firestore.indexes.json` | Declaración de índices compuestos para queries complejas. Se despliega con `firebase deploy --only firestore:indexes`. |
| `storage.rules` | Reglas de seguridad de Firebase Storage. |
| `firebase.json` | Mapea archivos locales a servicios de Firebase CLI. |
| `.firebaserc` | Proyecto Firebase asociado (`unityevents-c77e4`). |
| `CLAUDE.md` | Reglas arquitecturales del proyecto (estructura, convenciones). |

### Reglas de código seguidas (de `CLAUDE.md`)

- **100% Jetpack Compose**. Cero XML para contenido.
- **100% Material 3**. Siempre `MaterialTheme.colorScheme`, nunca colores hardcodeados.
- **Tipografía** siempre vía `MaterialTheme.typography`.
- **Comentarios en español** (KDoc y `//`).
- **Nombres de clases/funciones en inglés.**
- **Cada Screen en su propio archivo** dentro de su feature.
- **Cada ViewModel junto a su Screen.**
- Preferencia por **inmutabilidad** (`val > var`), **coroutines** + `Flow`, y **null-safety**.

---

## Anexo: ¿Cómo probar el proyecto end-to-end?

Recomendado: dos dispositivos (dos emuladores o uno físico + emulador) para simular los roles.

1. **Usuario A** (organizador): registra cuenta → crea evento con ubicación.
2. **Moderador** (edita `role=MODERATOR` en Firestore): aprueba el evento → se otorgan +50 puntos a A.
3. **Usuario B**: abre el mapa → ve el evento → entra al detalle → obtiene ticket → ve el QR.
4. **Usuario A**: en el detalle de su evento → "Escanear tickets" → escanea el QR de B → +30 puntos a B.
5. **Usuario B**: comenta el evento de A con 5 estrellas → +10 a B, +20 a A, A recibe notificación.
6. **Verificar en Firestore:** los campos `points` y `level` de ambos usuarios deben reflejar los cambios.

---

*Última actualización: Fase B completa (eventos, tickets, QR, mapa, reputación, moderación).*
