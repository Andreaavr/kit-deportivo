# Kit deportivo: patrón Abstract Factory

Aplicación de escritorio en Java (Swing) que muestra el equipamiento de cuatro
deportes usando el patrón **Abstract Factory**, de forma que nunca se mezclan
productos de deportes distintos. Cada deporte muestra **todos** sus productos
separados en tres pestañas: **Equipo principal**, **Calzado** y **Accesorios**.

Los productos son reales: nombre, marca, descripción, precio, foto y enlace.
Salen del catálogo de la tienda online de Decathlon Catar, guardado en una base
de datos SQLite propia. La ventana los muestra **en español** y con el precio
**en euros**.

| Deporte | Persona | Equipo principal | Calzado | Accesorios |
|---|---|---|---|---|
| Fitness | A | 84 | 5 | 68 |
| Running | A | 10 | 19 | 36 |
| Natación | B | 128 | 38 | 53 |
| Gimnasia | B | 22 | 8 | 22 |

(Productos disponibles en el catálogo importado; se calcularon con las consultas
reales de la tabla de criterios más abajo.)

## De dónde salen los datos

```
 decathlon.qa/products.json ─┐
                             ├─▶ ImportadorCatalogo ──▶ datos/catalogo.db ──▶ App
 open.er-api.com (QAR → €) ──┘   (se ejecuta 1 vez)     (SQLite, en Git)       │
                                                                               ▼
                                     api.mymemory.translated.net ◀── traduce lo que se muestra
                                     (se guarda en catalogo.db: cada texto se traduce una sola vez)
```

1. **`ImportadorCatalogo`** descarga el catálogo completo de la tienda
   (`/products.json?limit=250&page=N`, con una pausa entre páginas) y el tipo de
   cambio del rial catarí al euro, y lo guarda todo en **`datos/catalogo.db`**.
   No se escribe ningún dato a mano.
2. **`ClienteBaseDatos`** busca en esa base de datos con SQL y convierte los
   precios a euros.
3. **`FuenteTraducida`** traduce al español el nombre y la descripción de cada
   producto que se va a mostrar. Las traducciones se guardan en la tabla
   `traduccion` de la misma base de datos (**`TraductorConMemoria`**), así que cada
   texto se traduce una sola vez y después la app funciona sin internet
   (salvo las fotos).
4. **`ClienteShopify`** es una alternativa que consulta la tienda en vivo; se
   activa cambiando una línea de `Main`.

### Crear o actualizar la base de datos

En IntelliJ: abre `ImportadorCatalogo` y pulsa ▶ junto a su `main`.
Por terminal: `mvn compile exec:java -Dexec.mainClass=deportes.datos.ImportadorCatalogo`.

Al terminar muestra cuántos productos ha guardado y el cambio a euros.
**Las traducciones ya hechas se conservan** al volver a importar.

> Si la base de datos se importó con una versión anterior (sin cambio a euros),
> la app funciona igual pero muestra los precios en QAR. Vuelve a ejecutar el
> importador una vez para tenerlos en euros.

### Precio en euros

El cambio se obtiene de la API abierta de ExchangeRate-API
(`https://open.er-api.com/v6/latest/QAR`, gratuita y sin clave). El precio en
euros es **una conversión del precio de la tienda de Catar**, no el precio de
Decathlon España; la ventana lo indica junto con la fecha del cambio. La API
pide citarla: la barra lateral muestra «Rates By Exchange Rate API» con un enlace.

### Traducción al español

Se usa la API gratuita de **MyMemory** (`api.mymemory.translated.net`, inglés → español).

- Sin registro permite unos **5.000 caracteres al día**; indicando un correo,
  **50.000 al día**. Se configura con la variable de entorno `MYMEMORY_EMAIL`
  (así el correo no se sube a Git):
  IntelliJ → *Run* → *Edit Configurations…* → `Main` → *Environment variables* →
  `MYMEMORY_EMAIL=tu_correo@ejemplo.com`.
- Solo se traduce lo que se muestra: el nombre y la primera frase de la
  descripción (como mucho 300 caracteres).
- Traducir los cuatro deportes completos son unos 84.000 caracteres: con el
  correo configurado basta con abrir los deportes durante **dos días**. Lo que
  aún no esté traducido se muestra en inglés, y al día siguiente se completa.
- Las traducciones se guardan en `datos/catalogo.db`; al hacer commit de la base
  de datos, el compañero o compañera las recibe ya hechas.

### Tablas

- `producto`: `titulo`, `marca`, `tipo`, `naturaleza`, `subdepartamento`,
  `deportes` (separados por `|`), `num_deportes`, `descripcion`, `extracto`,
  `precio`, `url_imagen`, `url_enlace`, `disponible`.
- `importacion`: `tienda`, `moneda` (QAR), `fecha`, `productos`, `tasa_eur`, `fecha_tasa`.
- `traduccion`: `original` (inglés), `traducido` (español), `fecha`.

### Cómo se eligen los productos de cada categoría

Cada categoría se pide con un `Criterio`:

```java
Criterio.de("swimming goggles")                     // ordena: primero los que más lo tienen en el nombre
        .deporte("swimming")                        // debe aparecer en la columna deportes
        .tipo("swimming goggles", "swim cap", ...)  // palabras completas del tipo de producto
```

La consulta devuelve **todos** los productos que cumplen, siempre que estén
**disponibles** y **no sean genéricos**: los que llevan más de 8 deportes en sus
etiquetas (cables, camisetas básicas…) se descartan.

El **tipo** se compara con `tipo` y `naturaleza` (lo que es el producto) y por
**palabras completas**: `shoes` encaja con "Sport shoes", pero no con "Shoe bag";
`ball` no encaja con "Ballet". No se usa el subdepartamento porque es demasiado
general ("SHOES & APPAREL" incluye camisetas).

Las palabras de calzado son comunes a todos los deportes y están en
`Criterio.CALZADO`. Las de equipo y accesorio las decide cada fábrica.

### Criterios probados con el catálogo completo (3.619 productos)

| Deporte (`.deporte(...)`) | Equipo principal (`.tipo(...)`) | Calzado | Accesorios (`.tipo(...)`) |
|---|---|---|---|
| Fitness (`"fitness", "body building", "cross training"`) | `"dumbell", "kettlebell", "weight plate", "weight set", "bar", "weightlifting bar", "weightlifting bumper disc", "press bench", "squat rack", "treadmill", "exercise bike", "rowing machine", "cross trainer", "stepper", "home gym", "plyo box", "dips station", "step"` → **84** | `Criterio.CALZADO` → **5** | `"skipping rope", "elastic band", "resistance band", "glute band", "handgrip", "gloves", "lifting strap", "lumbar belt", "wrist support", "sports bag", "water bottle", "shaker", "fitness mat", "abdominal wheel", "suspension strap", "towel", "knee pad"` → **68** |
| Running (`"jogging", "road running"`) | `"watch", "fitness monitor", "hydration backpack", "running vest", "headlamp"` → **10** | `Criterio.CALZADO` → **19** | `"belt", "flask", "water bottle", "bottle", "arm guard", "smartphone holder", "insoles", "massage roller", "massage gun", "calf compression sleeves", "gloves", "headband", "cap", "visor"` → **36** |
| Natación (`"swimming"`) | `"swimming goggles", "swim cap", "swimming mask", "center snorkel", "flippers", "kick board", "pull buoy", "swimming paddle", "swim gloves"` → **128** | `Criterio.CALZADO` → **38** | `"towel", "nose clip", "earplug", "mesh bag", "pouch", "watertight pocket", "glasses case", "anti fogging", "changing robe", "bathrobe"` → **53** |
| Gimnasia (`"gymnastics"`) | `"fitness mat", "pilates mat", "balance board", "balance disc", "trampoline", "beam", "foam pad"` → **22** | `Criterio.CALZADO` → **8** | `"hoop", "jumping ball", "swiss ball", "gymnastic ribbon", "rg club", "skipping rope", "elastic band", "toning handle", "medicine ball", "soft ball", "weighted ball", "mat bag"` → **22** |

Nota: `"dumbell"` está escrito así a propósito, porque es como lo etiqueta la tienda.

**Aviso:** `/products.json` es un punto de acceso público de la tienda, no una
API oficial para terceros. Se usa sin fines comerciales, con pocas peticiones y
citando la fuente (la ventana muestra el origen, la fecha de importación, el
cambio de moneda y el servicio de traducción).

## Compilar y ejecutar

Es un proyecto **Maven** (Java 17): usa Gson (leer JSON) y sqlite-jdbc (base de datos).

- **IntelliJ:** abre la carpeta, acepta *Load Maven Project* y ejecuta `Main` con ▶.
- **Terminal:** `mvn compile exec:java`.

Se abre una ventana: elige el deporte en la barra lateral y cambia de categoría
con las pestañas. Cada tarjeta muestra foto, marca, nombre, descripción, precio
en euros y un enlace a la tienda.

## Patrones utilizados

| Patrón | Dónde | Para qué |
|---|---|---|
| **Abstract Factory** (principal) | `FabricaDeportiva` y `Fabrica<Deporte>` | Crear los productos de un deporte sin mezclar familias distintas |
| **Strategy** | `FuenteDatosMaterial` (`ClienteBaseDatos`, `ClienteShopify`) | Cambiar de dónde salen los datos sin tocar las fábricas |
| **Strategy** | `Traductor` (`TraductorMyMemory`) | Cambiar de servicio de traducción sin tocar el resto |
| **Decorator** | `FuenteConCache`, `FuenteTraducida` | Añadir caché y traducción a cualquier fuente sin modificarla |
| **Decorator** | `TraductorConMemoria` | Guardar las traducciones para no repetirlas |

En `Main` se ve cómo se componen los decoradores:

```java
FuenteDatosMaterial fuente = new FuenteConCache(new FuenteTraducida(baseDatos, traductor));
```

Diagrama UML completo: [`docs/uml.png`](docs/uml.png) (también en SVG).

## Estructura

| Paquete | Contenido | Rol |
|---|---|---|
| `deportes.fabrica` | `FabricaDeportiva` | Fábrica abstracta: `crearEquipos()`, `crearCalzados()`, `crearAccesorios()` |
| `deportes.familias.<deporte>` | `Fabrica<Deporte>` + `Equipo`, `Calzado` y `Accesorio<Deporte>` | Fábrica y productos concretos |
| `deportes.productos` | `EquipoPrincipal`, `CalzadoDeportivo`, `AccesorioDeportivo` | Productos abstractos |
| `deportes.productos` | `ProductoDeportivo`, `InfoMaterial` | Interfaz común de los productos y sus datos |
| `deportes.cliente` | `GestorKitDeportivo` | Cliente: pide las tres listas a la fábrica |
| `deportes.datos` | `FuenteDatosMaterial`, `Criterio` | Cómo se obtienen los datos y qué se pide |
| `deportes.datos` | `ClienteBaseDatos`, `ClienteShopify` | Fuentes concretas: base de datos local o tienda en vivo |
| `deportes.datos` | `FuenteConCache`, `FuenteTraducida` | Decoradores: caché y traducción |
| `deportes.datos` | `Traductor`, `TraductorMyMemory`, `TraductorConMemoria` | Traducción al español |
| `deportes.datos` | `ImportadorCatalogo` | Crea `datos/catalogo.db` desde la tienda |
| `deportes.datos` | `CargadorImagenes` | Descarga las fotos (en tamaño reducido) |
| `deportes.vista` | `VentanaKit`, `Pestanas`, `TarjetaProducto`, `TarjetaEsqueleto`, `ImagenCabecera`, `PanelRedondeado`, `Estilo` | Interfaz gráfica |
| `deportes` | `Main` | Monta la fuente de datos, registra las fábricas y abre la ventana |

Los productos concretos no son `public`: solo la fábrica de su paquete puede
crearlos, así que el compilador impide mezclar familias.

## Reparto del trabajo

La rama `main` contiene la base común. Cada persona trabaja en su propia rama y
solo toca sus archivos:

| | Persona A | Persona B |
|---|---|---|
| Rama | `persona-a/fitness-running` | `persona-b/natacion-gimnasia` |
| Carpetas nuevas | `familias/fitness/`, `familias/running/` | `familias/natacion/`, `familias/gimnasia/` |
| En `Main.java` | Solo bajo los comentarios `Persona A` | Solo bajo los comentarios `Persona B` |

Cada familia son 4 archivos: `Fabrica<Deporte>`, `Equipo<Deporte>`,
`Calzado<Deporte>` y `Accesorio<Deporte>`. La fábrica recibe una
`FuenteDatosMaterial` en el constructor (depende de la abstracción, no de
`ClienteBaseDatos`) y crea cada lista envolviendo los datos en sus productos:

```java
@Override
public List<CalzadoDeportivo> crearCalzados() {
    return fuente.buscar(Criterio.de("pool sandals").deporte("swimming").tipo(Criterio.CALZADO))
            .stream().<CalzadoDeportivo>map(CalzadoNatacion::new).toList();
}
```

Los productos concretos implementan su interfaz, guardan su `InfoMaterial` y no son `public`:

```java
class CalzadoNatacion implements CalzadoDeportivo {
    private final InfoMaterial info;

    CalzadoNatacion(InfoMaterial info) {
        this.info = info;
    }

    @Override
    public InfoMaterial getInfo() {
        return info;
    }
}
```

Para que el deporte aparezca en la ventana, se registra en `Main`, en tu zona:

```java
// --- Imports Persona B (natación y gimnasia) ---
import deportes.familias.natacion.FabricaNatacion;
...
// --- Deportes Persona B (natación y gimnasia) ---
deportes.put("Natación", () -> new FabricaNatacion(fuente));
```


**Reglas para que el merge salga limpio**

1. No modificar nada de `productos/`, `fabrica/`, `datos/`, `cliente/` ni `vista/`
   en las ramas. Si hace falta cambiar algo común, se acuerda y se hace primero en `main`.
2. En `Main.java`, escribir solo debajo de tu comentario y no borrar los
   comentarios ni las líneas en blanco que separan las zonas.
3. No hacer commit de `datos/catalogo.db` desde las ramas (para evitar conflictos
   en un archivo binario): las traducciones se suben desde `main`.
4. Compilar y ejecutar antes de cada commit.

## Flujo con Git

Cada persona:

```bash
git clone <URL-del-repositorio>
cd kit-deportivo
git switch -c persona-a/fitness-running      # Persona B: persona-b/natacion-gimnasia

# ... crear sus clases, compilar, probar ...
git add src
git commit -m "Añade familias Fitness y Running"
git push -u origin persona-a/fitness-running
```

Integrar el trabajo (cuando las dos ramas estén terminadas):

```bash
git switch main
git pull
git merge persona-a/fitness-running
git merge persona-b/natacion-gimnasia
git push
```

También se puede hacer con un *pull request* por rama en GitHub.
