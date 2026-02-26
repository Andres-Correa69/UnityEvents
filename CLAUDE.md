# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

UnityEvents es una aplicacion Android (package: `co.uniquindio.unityevents`).

**Stack:** Kotlin 2.0.21, Jetpack Compose, Material You (Material 3), Gradle 8.13 (Kotlin DSL)
**Target:** Android Min SDK 34, Compile/Target SDK 36, Java 11

## Build & Test Commands

```bash
# Build
./gradlew build

# Run unit tests
./gradlew test

# Run a single unit test class
./gradlew test --tests "co.uniquindio.unityevents.ExampleUnitTest"

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Clean build
./gradlew clean build

# Install debug APK on connected device
./gradlew installDebug
```

## Estructura Obligatoria del Proyecto

Todo el codigo debe seguir esta estructura de paquetes sin excepcion. La raiz es `co.uniquindio.unityevents` ubicada en `app/src/main/java/co/uniquindio/unityevents/`.

```
co.uniquindio.unityevents/
├── core/                        # Codigo comun para toda la aplicacion
│   ├── component/               # Composables reutilizables en toda la app
│   ├── navigation/              # Navegacion principal de la app
│   ├── theme/                   # Temas y estilos de la app (Color.kt, Theme.kt, Type.kt)
│   └── utils/                   # Utilidades y funciones comunes
├── data/                        # Capa de datos
│   ├── model/                   # Modelos de datos comunes (DTOs)
│   └── repository/              # Implementaciones de repositorios de datos
├── domain/                      # Logica de negocio
│   ├── model/                   # Modelos de dominio (entidades)
│   └── repository/              # Interfaces de repositorios de dominio
├── features/                    # Funcionalidades especificas de la app
│   ├── home/                    # Feature: pantalla principal
│   ├── login/                   # Feature: inicio de sesion
│   └── register/                # Feature: registro de usuario
└── MainActivity.kt              # Actividad principal de la app
```

### Reglas de estructura:
- Cada nueva pantalla/funcionalidad se crea como un subpaquete dentro de `features/`
- Los composables compartidos entre features van en `core/component/`
- La navegacion centralizada va en `core/navigation/`
- Los DTOs (objetos de transferencia de datos) van en `data/model/`
- Las entidades de dominio van en `domain/model/`
- Las interfaces de repositorio van en `domain/repository/`, las implementaciones en `data/repository/`

## Reglas de UI: Material You (Material 3) + Jetpack Compose

- **TODO** el frontend se construye con Jetpack Compose. No usar Views XML.
- **TODO** el diseno visual debe usar Material You (Material 3) para lograr una apariencia nativa y moderna.
- Usar los componentes de Material 3: `MaterialTheme`, `TopAppBar`, `NavigationBar`, `Card`, `Button`, `TextField`, `Surface`, etc.
- Respetar el sistema de colores dinamicos de Material You (`dynamicColorScheme`).
- Usar `Typography` y `ColorScheme` del tema definido en `core/theme/`.
- No usar colores hardcodeados; siempre referenciar `MaterialTheme.colorScheme`.
- No usar tipografias hardcodeadas; siempre referenciar `MaterialTheme.typography`.

## Reglas de Codigo

- **Todo el codigo debe estar extensamente comentado.** Cada clase, funcion, parametro y bloque logico relevante debe tener comentarios claros en espanol que expliquen su proposito y funcionamiento.
- Usar KDoc (`/** */`) para documentar clases y funciones publicas.
- Usar comentarios en linea (`//`) para explicar logica interna compleja.
- Seguir buenas practicas de Kotlin: inmutabilidad (`val` sobre `var`), funciones de extension, null safety, scope functions.
- Nombres de clases, funciones y variables descriptivos y en ingles. Comentarios en espanol.
- Cada Screen composable debe estar en su propio archivo dentro de su feature.
- Cada ViewModel debe estar junto a su Screen dentro del paquete de la feature.

## Dependency Management

Versiones centralizadas en `gradle/libs.versions.toml`. Toda nueva dependencia se agrega ahi primero.

## Verificacion Obligatoria y Codigo Libre de Errores

**Antes de considerar cualquier tarea como terminada, se DEBE verificar que el codigo compila y no tiene errores.** Esta regla es obligatoria y no tiene excepciones.

### Reglas:
- **Imports completos:** Todo archivo debe incluir todos los imports necesarios. Nunca dejar imports faltantes o sin resolver.
- **Dependencias completas:** Si el codigo usa una libreria o dependencia que no esta en el proyecto, agregarla a `gradle/libs.versions.toml` y al `build.gradle.kts` correspondiente antes de usarla.
- **Compilacion sin errores:** Despues de hacer cambios, verificar que el proyecto compila correctamente ejecutando `./gradlew build` o al menos `./gradlew compileDebugKotlin`.
- **Sin referencias rotas:** No referenciar clases, funciones o variables que no existan. Si se renombra o elimina algo, actualizar todas las referencias en el proyecto.
- **Consistencia de tipos:** Asegurar que los tipos de datos sean consistentes en toda la cadena (modelo, repositorio, viewmodel, UI).
- **Recursos completos:** Si se usan strings, drawables u otros recursos en el codigo, asegurar que existan en los archivos de recursos (`res/`).
- **Gradle sync:** Si se modifican archivos de Gradle (`build.gradle.kts`, `libs.versions.toml`, `settings.gradle.kts`), verificar que la sincronizacion de Gradle sea exitosa.
- **Buenas practicas Android:** Seguir las buenas practicas oficiales de Android/Kotlin: manejo correcto del ciclo de vida, uso de coroutines para operaciones asincronas, inyeccion de dependencias cuando aplique, y manejo adecuado de estados en Compose.
