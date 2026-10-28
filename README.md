# Kit deportivo — Patrón Abstract Factory

Programa en Java que prepara el equipamiento de una sesión deportiva
(equipo, calzado y accesorio) usando el patrón **Abstract Factory**,
de forma que nunca se mezclan piezas de deportes distintos.

## Compilar y ejecutar

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out deportes.Main            # muestra un menú
java -cp out deportes.Main escalada   # o pasa el deporte directamente
```

Deportes: `fitness`, `running`, `escalada`, `gimnasia` (o `1`–`4`).

## Estructura

| Paquete | Contenido | Rol en el patrón |
|---|---|---|
| `deportes.productos` | `EquipoPrincipal`, `CalzadoDeportivo`, `AccesorioDeportivo` | Productos abstractos |
| `deportes.fabrica` | `FabricaDeportiva` | Fábrica abstracta |
| `deportes.familias.<deporte>` | `Fabrica<Deporte>` + `Equipo`, `Calzado` y `Accesorio<Deporte>` | Fábrica y productos concretos |
| `deportes.cliente` | `GestorKitDeportivo` | Cliente |
| `deportes` | `Main` | Elige la fábrica concreta |

Los productos concretos no son `public`: solo la fábrica de su paquete puede
crearlos, así que el compilador impide mezclar familias.

## Reparto del trabajo

La rama `main` contiene la base común (interfaces, cliente y `Main`).
Cada persona trabaja en su propia rama y solo toca sus archivos:

| | Persona A | Persona B |
|---|---|---|
| Rama | `persona-a/fitness-running` | `persona-b/escalada-gimnasia` |
| Carpetas nuevas | `familias/fitness/`, `familias/running/` | `familias/escalada/`, `familias/gimnasia/` |
| En `Main.java` | Solo bajo los comentarios `Persona A` | Solo bajo los comentarios `Persona B` |

Cada familia son 4 archivos: `Fabrica<Deporte>`, `Equipo<Deporte>`,
`Calzado<Deporte>` y `Accesorio<Deporte>`.

**Reglas para que el merge salga limpio**

1. No modificar nada de `productos/`, `fabrica/` ni `cliente/` en las ramas.
   Si hace falta cambiar una interfaz, se acuerda y se hace primero en `main`.
2. En `Main.java`, escribir solo debajo de tu comentario y no borrar los
   comentarios ni las líneas en blanco que separan las zonas.
3. Compilar antes de cada commit.

## Flujo con Git

Una sola vez, quien crea el repositorio:

```bash
git remote add origin <URL-del-repositorio>
git push -u origin main
```

Cada persona:

```bash
git clone <URL-del-repositorio>
cd kit-deportivo
git switch -c persona-a/fitness-running      # Persona B: persona-b/escalada-gimnasia

# ... crear sus clases, compilar, probar ...
git add .
git commit -m "Añade familias Fitness y Running"
git push -u origin persona-a/fitness-running
```

Integrar el trabajo (cuando las dos ramas estén terminadas):

```bash
git switch main
git pull
git merge persona-a/fitness-running
git merge persona-b/escalada-gimnasia
javac -encoding UTF-8 -d out $(find src -name "*.java")   # comprobar que compila
git push
```

También se puede hacer con un *pull request* por rama en GitHub o GitLab.
