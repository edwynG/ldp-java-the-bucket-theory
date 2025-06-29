# Java the bucket theory
En la fiesta de fin de semestre, se planea realizar una cervezada en la que los estudiantes de la materia 
reutilizarán el sistema de tres barriles comunicantes, previamente trabajado en los proyectos de
programación funcional y lógica. Se requiere implementar una solución concurrente para este problema,
utilizando monitores e hilos en Java.

## Estructura del proyecto
```{html}
Java-the-bucket-theory/
├── docs/               # Enunciado y documentación
├── bytecode/           # archivos bytecode
├── src/                # Código fuente
│   ├── Main.java       # Punto de entrada del programa
│   └── ...             # Otros módulos
├── tests/              # Archivos de pruebas
│   └── ...             # Casos de prueba
├── Makefile            # Archivo makefile para ejecutar
└── README.md           # Este archivo
```
> [!Note]
> Al culminar el proyecto todos los archivos que tengan el **codigo fuente** seran unidos en un unico archivo, la razón de esta estructura es simplemente para un mejor desarrollo.

> [!Warning]
> Si estas en windows, recomiendo usar la consola **Git bash**. Puede llegar a fallar alguno de los comandos que se usaron en el makefile si se usa **poweshell** o la **cmd** de windows.

- Windows
    ```{powershell}
        mingw32-make run ARGS="ubicacion/txt"
    ```
  
- Linux
    ```{bash}
        make run ARGS="ubicacion/txt"
    ```

> [!Note]
> **make** es la herramienta que se utiliza para ejecutar archivos **makefile**. En windows este viene junto con la instalación de **C/C++**. Y si estas en linux este viene junto con el entorno **Unix**.

