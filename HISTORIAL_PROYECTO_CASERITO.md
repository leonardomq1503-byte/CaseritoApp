# 📱 Historial Completo y Bitácora de Desarrollo: Caserito App

**Proyecto:** CaseritoApp  
**Plataforma:** Android (Jetpack Compose, Kotlin 2.2.10, Material Design 3)  
**Paquete:** `com.example.caseritoapp`  
**Ubicación:** `c:\Users\Admin\AndroidStudioProjects\CaseritoApp`  
**Fecha de generación:** Noviembre 2026  

---

## 📑 Tabla de Contenidos
1. [Visión y Propósito del Proyecto](#1-visión-y-propósito-del-proyecto)
2. [Arquitectura General (MVVM + Single Source of Truth)](#2-arquitectura-general-mvvm--single-source-of-truth)
3. [Cronología y Resumen de las 7 Fases](#3-cronología-y-resumen-de-las-7-fases)
   - [Fase 1: Tema M3 y Componentes UI](#fase-1-tema-m3-y-componentes-ui)
   - [Fase 2: Navegación y Pantallas](#fase-2-navegación-y-pantallas)
   - [Fase 3: Base de Datos Local Room SQLite](#fase-3-base-de-datos-local-room-sqlite)
   - [Fase 4: Backend Supabase PostgreSQL](#fase-4-backend-supabase-postgresql)
   - [Fase 5: Repositorios y Sincronización Offline](#fase-5-repositorios-y-sincronización-offline)
   - [Fase 6: Lógica de Negocio y ViewModels](#fase-6-lógica-de-negocio-y-viewmodels)
   - [Fase 7: Google Maps con Estilo Clean y Notificaciones Push](#fase-7-google-maps-con-estilo-clean-y-notificaciones-push)
4. [Configuraciones y Credenciales Clave](#4-configuraciones-y-credenciales-clave)
5. [Incidencias Técnicas Críticas y Soluciones](#5-incidencias-técnicas-críticas-y-soluciones)
6. [Árbol de Archivos y Responsabilidades](#6-árbol-de-archivos-y-responsabilidades)
7. [Guía para Compilar y Ejecutar la Aplicación](#7-guía-para-compilar-y-ejecutar-la-aplicación)

---

## 1. Visión y Propósito del Proyecto
**Caserito** es una aplicación móvil diseñada para el ecosistema de comercio local en Perú (con foco inicial en Lima / Miraflores). Permite a los usuarios descubrir ofertas flash y promociones en tiendas cercanas, guardar productos favoritos, reservar artículos para retiro en local y guiarse con geolocalización.

### Principios de Diseño:
* **Offline-First:** La app siempre responde de inmediato mostrando los datos almacenados en SQLite Room local.
* **Sincronización en segundo plano:** Supabase actúa como backend central y sincroniza silenciosamente las ofertas cuando hay conexión.
* **Material Design 3:** Paleta oficial con Rojo Caserito (`#F04432`), Verde Éxito/Ahorro (`#178B63`) y compatibilidad con temas claro/oscuro.

---

## 2. Arquitectura General (MVVM + Single Source of Truth)

```
[ Capa UI (Jetpack Compose) ]
       ▲
       │ Observa StateFlow
       ▼
[ Capa ViewModel (StateFlow / Coroutines) ]
       ▲
       │ Llamadas asíncronas
       ▼
[ Capa Repositorio (Single Source of Truth) ]
   ┌───┴───────────────────────────┐
   │                               │
[ Room Local DB (SQLite) ]    [ Supabase Cloud (PostgreSQL / Auth) ]
```

* **Capa UI:** Pantallas declarativas en Jetpack Compose, organizadas en `ui/`.
* **Capa ViewModel:** Gestiona el estado de cada pantalla con `StateFlow` y maneja eventos sin bloquear el hilo principal.
* **Capa Repositorio:** Abstrae la persistencia local y la sincronización remota mediante Kotlin Coroutines y Flow.
* **Capa Local:** Room Database con 17 entidades relacionales.
* **Capa Remota:** Cliente Supabase con Postgrest (consultas REST), Auth (GoTrue) y Storage.

---

## 3. Cronología y Resumen de las 7 Fases

### Fase 1: Tema M3 y Componentes UI
* Paleta de colores personalizada M3:
  * Primario: `#F04432` (Rojo/Naranja Caserito)
  * Terciario: `#178B63` (Verde esmeralda para stock y ahorro)
  * Fondo/Superficie: `#FFF8F6` y `#1E1A1A` (Dark mode)
* Componentes reutilizables implementados:
  * `BuscadorCaserito`: Barra de búsqueda con icono de filtro.
  * `CategoriaChip`: Píldoras interactivas con estados de selección.
  * `OfertaCard`: Tarjeta de producto con foto, precios tachados, % descuento y botón de favorito.
  * `TiendaCard`: Tarjeta de tienda con categoría, dirección y distancia estimada.

### Fase 2: Navegación y Pantallas
* Implementación de Jetpack Navigation Compose con transiciones animadas:
  * `Screen.Home`: Pantalla principal con ofertas destacadas y comercios.
  * `Screen.Search`: Exploración y búsqueda con filtros.
  * `Screen.Map`: Mapa de ubicación de comercios.
  * `Screen.Alerts`: Buzón de notificaciones.
  * `Screen.Profile`: Perfil de usuario y ajustes.
  * `Screen.Login`: Inicio de sesión.
  * `Screen.Register`: Registro diferenciado (*Soy Cliente* vs *Tengo Negocio*).
  * `Screen.Verification`: Verificación de cuenta OTP.
* `CaseritoBottomBar`: Barra inferior con insignia numérica reactiva que oculta su visualización en flujos de autenticación.

### Fase 3: Base de Datos Local Room SQLite
* Configuración de **Room 2.7.2** con el procesador **KSP**:
* 17 Entidades creadas en `data/local/entity/`:
  `UsuarioEntity`, `SesionEntity`, `VerificacionEntity`, `TiendaEntity`, `DocumentoTiendaEntity`, `UsuarioTiendaEntity`, `UbicacionEntity`, `ResponsableUbicacionEntity`, `CategoriaEntity`, `ProductoOfertaEntity`, `ImagenProductoEntity`, `FavoritoEntity`, `ReservaEntity`, `ValoracionEntity`, `NotificacionEntity`, `MetricaProductoEntity`, `HistorialOfertaEntity`.
* 6 DAOs con consultas reactivas (`Flow`):
  `OfertaDao`, `TiendaDao`, `FavoritoDao`, `NotificacionDao`, `UsuarioDao`, `ReservaDao`.
* `CaseritoDatabase`: Singleton con soporte para migraciones destructivas controladas.

### Fase 4: Backend Supabase PostgreSQL
* Creación de proyecto Supabase (`nauxzjgubuyxvzpvyjaw`).
* Creación del esquema relacional en PostgreSQL con triggers y Row Level Security (RLS).
* Carga de datos semilla en Lima (Miraflores):
  * Categorías: Ropa, Electrónica, Hogar, Deportes, Comida, Accesorios.
  * Tiendas: TecnoStore, Moda & Estilo, Bazar Don Pepe.
  * Sucursales geolocalizadas: Av. Larco 450, Calle Cantuarias 120, Av. Benavides 1100.
  * Ofertas activas con stock y precios en Soles (S/).

### Fase 5: Repositorios y Sincronización Offline
* Configuración de **supabase-kt 3.1.4** y motor de red **Ktor 3.1.0**.
* Creación de DTOs serializables (`CategoriaDto`, `TiendaDto`, `UbicacionDto`, `ProductoOfertaDto`).
* Creación de `EntityMappers.kt` para transformar DTOs remotos en entidades de Room.
* Implementación de `OfertaRepository`, `TiendaRepository` y `AuthRepository`.
* Sincronización en segundo plano con control de errores para permitir navegación sin conexión.

### Fase 6: Lógica de Negocio y ViewModels
* Adición de librerías `lifecycle-viewmodel-compose` y `lifecycle-runtime-compose`.
* Implementación de ViewModels:
  * `HomeViewModel`: Sincronización automática de ofertas y tiendas; filtrado por categorías; toggle reactivo de favoritos.
  * `SearchViewModel`: Búsqueda de texto completo y cálculo dinámico de coincidencias.
  * `AuthViewModel`: Integración completa con Supabase GoTrue Auth para inicio de sesión, registro y cierre de sesión.
  * `ProfileViewModel`: Lectura reactiva del perfil, contadores de reservas activas y favoritos.
* Inyección de dependencias limpia a través de `CaseritoViewModelFactory`.
* Conexión de todas las pantallas UI a sus respectivos ViewModels eliminando datos quemados (*hardcoded*).

### Fase 7: Google Maps con Estilo Clean y Notificaciones Push
* Integración del SDK oficial de **Google Maps Compose 6.4.1** con la API Key del proyecto.
* **Estilo Visual Personalizado (`CaseritoMapStyle.kt`):**
  * Ocultamiento de POIs comerciales irrelevantes para destacar únicamente los comercios Caserito.
  * Tonos pastel en áreas verdes y Océano Pacífico de la Costa Verde.
  * Marcadores flotantes interactivos (`MarkerComposable`) con el nombre y logo de cada comercio.
* **Control Táctil Nativo:** Zoom por pellizco (*pinch-to-zoom*), desplazamiento y botón de centrado.
* **Navegación GPS:** Botón para abrir la ruta en la app de Google Maps instalada mediante `Intent` nativo.
* **Notificaciones Push Locales (`NotificationHelper.kt`):** Canal de notificaciones nativas de Android con vibración y prioridad alta, más botón de prueba de alertas flash en `AlertsScreen`.

---

## 4. Configuraciones y Credenciales Clave

### Supabase Cloud:
* **URL del Proyecto:** `https://nauxzjgubuyxvzpvyjaw.supabase.co`
* **Anon Public Key:** `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im5hdXh6amd1YnV5eHZ6cHZ5amF3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTM3MzE5MDAsImV4cCI6MjEwOTMwNzkwMH0.LnfxEfAJOSmcBQ9a59fa5JYcDDmOQ2vaMdLK4kGp3x4`
* **Publishable Key:** `sb_publishable_r5rYe1JuYmSFYIF5FAbG1Q_GfY_Ilh2`

### Google Maps Android SDK:
* **API Key:** `AIzaSyBQ0wuHXTi0xRpud8lbA61CyisvPc46bnw`
* **Ubicación en Manifest:** Dentro del tag `<application>` en `AndroidManifest.xml`.

---

## 5. Incidencias Técnicas Críticas y Soluciones

1. **Incompatibilidad de Kotlin Metadata (Supabase SDK 3.8.0 vs Kotlin 2.2.10):**
   * *Problema:* Supabase SDK 3.8.0 requería Kotlin metadata 2.4.0, provocando error de compilación en Gradle.
   * *Solución:* Se realizó downgrade controlado a **`supabase-kt 3.1.4`**, compilado con Kotlin 2.1.x, garantizando 100% de compatibilidad binaria con Kotlin 2.2.10.
2. **Límite de argumentos en operador `combine` de Coroutines:**
   * *Problema:* `HomeViewModel` intentaba combinar 6 flujos en una sola función `combine`, excediendo la firma tipada de Kotlin Coroutines.
   * *Solución:* Se agruparon `isSyncing` y `error` en un modelo `SyncStatus`, reduciendo a 5 los flujos combinados y eliminando casts inseguros.
3. **Restricción de Claves Foráneas en Favoritos para Invitados:**
   * *Problema:* Un usuario no autenticado provocaba violación de Foreign Key en Room al guardar un favorito.
   * *Solución:* Se implementó `getOrCreateDefaultUserId()` en `FavoritoRepository` para asegurar la existencia de un usuario local por defecto.

---

## 6. Árbol de Archivos y Responsabilidades

```text
app/src/main/java/com/example/caseritoapp/
├── CaseritoApplication.kt        # Application class (Singletons de DB y Repositorios)
├── MainActivity.kt               # Actividad principal con Scaffold y Badge dinámico
│
├── data/
│   ├── local/
│   │   ├── CaseritoDatabase.kt   # Base de datos Room
│   │   ├── dao/                  # DAOs de Room (Ofertas, Tiendas, Favoritos, etc.)
│   │   └── entity/               # 17 Tablas relacionales
│   ├── remote/
│   │   ├── SupabaseClientProvider.kt # Singleton del cliente Supabase
│   │   └── dto/                  # Objetos de transferencia de datos Supabase
│   ├── mapper/
│   │   └── EntityMappers.kt      # Mapeo DTO -> Entity
│   └── repository/
│       ├── OfertaRepository.kt   # Sincronización y consulta de ofertas
│       ├── TiendaRepository.kt   # Sincronización y consulta de comercios
│       ├── AuthRepository.kt     # Login, Registro y Logout en Supabase Auth
│       ├── FavoritoRepository.kt # Gestión de favoritos local
│       └── NotificacionRepository.kt # Gestión de alertas y notificaciones
│
├── navigation/
│   ├── Screen.kt                 # Definición de rutas de navegación
│   ├── AppNavigation.kt          # Grafo de navegación Compose
│   └── CaseritoBottomBar.kt      # Barra de navegación inferior
│
├── ui/
│   ├── theme/                    # Colores, tipografía y formas Material 3
│   ├── components/               # Buscador, Chips, Tarjetas de oferta y tienda
│   ├── viewmodel/                # ViewModels (Home, Search, Auth, Profile, Map, Alerts)
│   ├── home/HomeScreen.kt        # Feed de ofertas y tiendas
│   ├── search/SearchScreen.kt    # Búsqueda reactiva
│   ├── map/MapScreen.kt          # Google Maps con estilo Caserito Clean
│   ├── map/CaseritoMapStyle.kt   # JSON de estilo minimalista para Google Maps
│   ├── alerts/AlertsScreen.kt    # Buzón de notificaciones con prueba push
│   ├── profile/ProfileScreen.kt  # Perfil, reservas y cierre de sesión
│   └── auth/                     # Login, Registro y Verificación
│
└── util/
    └── NotificationHelper.kt     # Creación de canales y envío de notificaciones nativas
```

---

## 7. Guía para Compilar y Ejecutar la Aplicación

### Compilación desde Terminal (PowerShell):
```powershell
Set-Location c:\Users\Admin\AndroidStudioProjects\CaseritoApp
.\gradlew.bat assembleDebug
```

### Ubicación del APK Generado:
`c:\Users\Admin\AndroidStudioProjects\CaseritoApp\app\build\outputs\apk\debug\app-debug.apk`

### Ejecución en Android Studio:
1. Abrir Android Studio y cargar el proyecto `CaseritoApp`.
2. Seleccionar el emulador **`Small_Phone`** o un teléfono físico conectado vía depuración USB.
3. Presionar el botón verde **Run (▶)** (`Shift + F10`).
