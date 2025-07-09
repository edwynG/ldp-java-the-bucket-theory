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

## Documentación

Durante la fiesta de fin de semestre, se organizará una "cervezada" en la que los estudiantes reutilizarán el sistema de tres barriles comunicantes desarrollado en proyectos anteriores. Esta vez, se requiere una solución concurrente en Java, utilizando monitores e hilos (Threads) para coordinar adecuadamente el acceso a los barriles y simular de forma realista el consumo y la reposición de cerveza.

#### Procesos Involucrados
Para la solución del problema se utilizo la logica de ``productor-consumidor``.
- **Estudiantes (Consumidores)**: Hilos que intentan consumir cerveza de los barriles, de acuerdo a la cantidad de tickets que poseen.

- **Proveedores (Productores)**: Hilos encargados de recargar los barriles cuando haya espacio disponible.

#### Recursos Críticos
Barriles de cerveza A, B y C: Cada barril tiene una capacidad máxima y una cantidad actual de cerveza. Al ser accedidos por múltiples hilos de forma concurrente, su manipulación debe ser protegida mediante sincronización para evitar condiciones de carrera.

#### Operaciones Críticas
- **Retiro de cerveza**: Los estudiantes deben verificar la cantidad disponible y retirar cerveza de un barril de forma atómica. Si la cantidad requerida no está disponible, deben esperar a que el barril sea recargado.

- **Recarga de cerveza**: Los proveedores deben agregar cerveza solo si hay espacio en el barril. En caso de que se intente recargar un barril lleno, se debe esperar. Si ocurre un desborde, este debe ser registrado y reportado al final de la ejecución.

#### Condiciones de Sincronización
- **Espera por recarga**: Si un estudiante desea más cerveza de la que hay disponible, su hilo debe quedar bloqueado hasta que se recargue el barril.

- **Espera por espacio**: Si un proveedor intenta recargar un barril lleno, debe esperar hasta que se libere espacio (es decir, que algún estudiante haya consumido).

- **Notificaciones**: Cuando cambia el estado de un barril (ya sea por consumo o por recarga), se debe notificar a los hilos en espera para que reevalúen su condición.

#### Decisiones de diseño 
- Se implementó un monitor Barrels que centraliza las operaciones sobre los barriles y maneja la sincronización de hilos.

- Se agregaron flags de control (flagWithdraw, flagRecharge) para evitar conflictos entre operaciones simultáneas.

- Los estudiantes se validan por edad (>= 18) y tickets (>0). Los inválidos son descartados.

- Se maneja el desbordamiento según reglas específicas, y se reporta la cerveza perdida al final de la ejecución.
### Procesador de entrada
```{java}
 public static void entryFromFile(String[] args, Barrel[] barrelsArray) {
         if (args.length < 1) {
            System.err.println("Debe proporcionar la ruta del archivo como argumento.");
            System.exit(1);
        }

        String filePath = args[0];
        List<Barrel> barrels = new ArrayList<>();

        int lineCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            Map<String, Barrel> barrelMap = new HashMap<>();
            Set<String> requiredBarrels = new HashSet<>(Arrays.asList("A", "B", "C"));
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                lineCount++;
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String name = parts[0].trim();
                    int capacity = Integer.parseInt(parts[1].trim());
                    int current = Integer.parseInt(parts[2].trim());
                    if (!requiredBarrels.contains(name)) {
                        throw new IllegalArgumentException("Barril inválido: " + name);
                    }

                    barrelMap.put(name, new Barrel(name, capacity, current));
                } else if (parts.length == 2) {
                    String type = parts[0].trim();
                    int count = Integer.parseInt(parts[1].trim());
                    if (type.equalsIgnoreCase("Estudiantes")) {
                        numStudents = count;
                    } else if (type.equalsIgnoreCase("Proveedores")) {
                        numProviders = count > 0 ? count : 0; // Asegurarse de que sea al menos 0
                    }
                }
            }
            if (lineCount != 5) {
                throw new IllegalArgumentException("El archivo debe contener exactamente 5 líneas válidas.");
            }
            // Validar que estén todos los barriles requeridos
            for (String b : requiredBarrels) {
                if (!barrelMap.containsKey(b)) {
                    throw new IllegalArgumentException("Falta el barril: " + b);
                }
            }
            barrels.add(barrelMap.get("A"));
            barrels.add(barrelMap.get("B"));
            barrels.add(barrelMap.get("C"));

        } catch (Exception e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());
            System.exit(1);
        }

        if (numStudents <= 0) {
            System.err.println("El número de estudiantes debe ser mayor a 0.");
            System.exit(1);

        }
        if (numProviders <= 0) {
            System.err.println("El número de proveedores debe ser mayor a 0.");
            System.exit(1);

        }

        if (barrels.size() != 3) {
            System.err.println("Debe haber exactamente 3 barriles.");
            System.exit(1);
        }
        
        Barrel[] arr = barrels.toArray(new Barrel[0]);
        Utils.overflowFromA(arr);
        Utils.overflowFromC(arr);
        Utils.overflowFromB(arr);
        for (int i = 0; i < barrelsArray.length; i++) {
            barrelsArray[i] = arr[i];
        }
    }
```
La función ``entryFromFile`` recibe como parámetros los argumentos de línea de comandos y un arreglo de objetos ``Barrel``, y su propósito es leer y procesar un archivo cuyo path se pasa como primer argumento. Primero verifica que se haya proporcionado el archivo y luego lo lee línea por línea, esperando exactamente cinco líneas válidas que describen tres barriles con nombre, capacidad y cantidad actual, además de dos líneas con conteos de estudiantes y proveedores. Valida que los barriles sean los requeridos ("A", "B" y "C") y que los números de estudiantes y proveedores sean mayores a cero, deteniendo el programa con mensajes de error si alguna condición no se cumple. Finalmente, convierte la información leída en objetos ``Barrel``, los procesa mediante métodos estáticos de la clase ``Utils`` para manejar posibles desbordamientos, y actualiza el arreglo ``barrelsArray`` con los barriles procesados. Las variables ``numStudents`` y ``numProviders`` son atributos de la clase y se actualizan durante la lectura del archivo. En caso de cualquier error de lectura o formato, el programa termina mostrando un mensaje de error.`

### Monitor - barriles
```{java}
class Barrels {
    private Barrel[] barrels;
    private boolean flagRecharge = false;
    private boolean flagWithdraw = false;
    private int lostBeer = 0;

    public Barrels(Barrel[] barrels) {
        if (barrels == null || barrels.length != 3) {
            throw new IllegalArgumentException("Se requieren exactamente 3 barriles.");
        }
        this.barrels = barrels;
    }

    public synchronized int withdrawFromBarrel(String barrelId, int amount) {
        while (flagWithdraw || flagRecharge) {
            try {
                wait(); // Espera si otro hilo está retirando
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
            }
        }
        flagWithdraw = true; // Indica que se está retirando
        for (Barrel barrel : barrels) {
            if (barrel.getId().equals(barrelId)) {
                if (barrel.getCurrentAmount() > 0) {
                    Utils.sleepSeconds(3 + (int) (Math.random() * 3)); // Simula un tiempo de recarga aleatorio entre 3
                                                                       // y 5 segundos
                    flagWithdraw = false; // Indica que ya no se está retirando
                    notify(); // Notifica a otros hilos que pueden continuar
                    return barrel.withdraw(amount);
                } else {
                    flagWithdraw = false; // Indica que ya no se está retirando
                    notifyAll(); // Notifica a otros hilos que pueden continuar
                    return 0; // No hay cerveza para retirar
                }
            }
        }
        throw new IllegalArgumentException("Barril no encontrado: " + barrelId);
    }

    public synchronized void rechargeBarrel(String barrelId, int amount) {
        if (barrelId.equalsIgnoreCase("B")) {
            return; // B no se puede recargar directamente
        }

        while (flagRecharge) {
            try {
                wait(); // Espera si otro hilo está recargando
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
            }

        }
        if (Thread.currentThread().isInterrupted()) {
        return; // o lanzar una excepción, o limpiar recursos
        }

        flagRecharge = true; // Indica que se está recargando
        System.out.println(Thread.currentThread().getName() + " va a recargar " + amount + " unidades en " + barrelId);

        for (Barrel barrel : barrels) {
            if (barrel.getId().equals(barrelId)) {
                int before = barrel.getCurrentAmount();
                barrel.recharge(amount);
                int after = barrel.getCurrentAmount();
                int overflow = (before + amount) - after;

                // Si hay desbordamiento y es A (pos 0) o C (pos 2), pasa a B (pos 1)
                if (overflow > 0 && (barrelId.equals(barrels[0].getId()) || barrelId.equals(barrels[2].getId()))) {
                    System.out.println(
                            "Desbordamiento en " + barrelId + ", transfiriendo " + overflow + " unidades a B.");
                    int beforeB = barrels[1].getCurrentAmount();
                    barrels[1].recharge(overflow);
                    int afterB = barrels[1].getCurrentAmount();
                    int overflowB = (beforeB + overflow) - afterB;

                    // Si B se desborda, va al barril con menor cantidad (A o C)
                    if (overflowB > 0) {
                        int idxA = 0, idxC = 2;
                        int minIdx = barrels[idxA].getCurrentAmount() <= barrels[idxC].getCurrentAmount() ? idxA : idxC;
                        System.out.println("Desbordamiento en B, transfiriendo " + overflowB + " unidades a "
                                + barrels[minIdx].getId() + ".");
                        int beforeMin = barrels[minIdx].getCurrentAmount();
                        barrels[minIdx].recharge(overflowB);
                        int afterMin = barrels[minIdx].getCurrentAmount();
                        int lost = (beforeMin + overflowB) - afterMin;
                        if (lost > 0) {
                            lostBeer += lost;
                            System.out.println("Cerveza perdida: " + lost + " unidades.");
                        }
                    }
                }
                Utils.sleepSeconds(3 + (int) (Math.random() * 3)); // Simula un tiempo de recarga aleatorio entre 3 y 5
                                                                   // segundos
                flagRecharge = false; // Indica que ya no se está recargando
                notifyAll();
                return;
            }
        }
        throw new IllegalArgumentException("Barril no encontrado: " + barrelId);
    }

    public synchronized Barrel getBarrel(String barrelId) {
        for (Barrel barrel : barrels) {
            if (barrel.getId().equals(barrelId)) {
                return barrel;
            }
        }
        throw new IllegalArgumentException("Barril no encontrado: " + barrelId);
    }

    public synchronized boolean hasAvailableCapacity() {
        for (Barrel barrel : barrels) {
            if (barrel.getAvailableCapacity() > 0) {
                return true;
            }
        }
        return false;
    }

    public synchronized int getLostBeer() {
        return lostBeer;
    }
}
````
La clase ``Barrels`` gestiona un conjunto de tres barriles y controla el acceso concurrente a ellos para evitar conflictos cuando varios hilos intentan retirar o recargar cerveza al mismo tiempo. El método ``withdrawFromBarrel`` permite retirar una cantidad específica de cerveza de un barril identificado por su ID. Para garantizar que no haya interferencia entre hilos, este método es sincronizado y utiliza una bandera (``flagWithdraw``) para indicar que un retiro está en curso, además de esperar si otro retiro o recarga está activo. Cuando encuentra el barril solicitado, verifica que tenga cerveza disponible; si es así, simula un tiempo de espera aleatorio entre 3 y 5 segundos para representar el proceso de retiro, luego realiza la operación y libera la bandera para que otros hilos puedan continuar. Si el barril está vacío, simplemente retorna cero, indicando que no se pudo retirar cerveza.

Por otro lado, el método ``rechargeBarrel`` se encarga de recargar un barril con una cantidad dada, excepto el barril "B", que no puede recargarse directamente. También es sincronizado y usa una bandera (``flagRecharge``) para evitar que múltiples recargas ocurran simultáneamente. Al recargar, si el barril se desborda (es decir, la cantidad recargada excede su capacidad), el exceso se transfiere al barril "B". Si "B" también se desborda, el sobrante se pasa al barril con menor cantidad entre "A" y "C". En caso de que aún haya exceso que no quepa en ninguno, se contabiliza como cerveza perdida. Este método también simula un tiempo de recarga aleatorio entre 3 y 5 segundos para reflejar el proceso real y notifica a otros hilos cuando termina, permitiendo que continúen sus operaciones.

Los métodos restantes de la clase Barrels permiten consultar y gestionar el estado de los barriles de forma segura en entornos concurrentes: ``getBarrel`` busca y devuelve un barril específico por su identificador, lanzando un error si no existe; ``hasAvailableCapacity`` verifica si alguno de los barriles tiene espacio disponible para más contenido; y ``getLostBeer`` retorna la cantidad total de cerveza que se ha perdido debido a desbordamientos que no pudieron almacenarse. Todos estos métodos están sincronizados para asegurar que las operaciones sean consistentes y libres de conflictos cuando varios hilos acceden simultáneamente a los barriles.

### Proceso - estudiantes
````
class Student extends Thread {
    private final String nombre;
    private final int edad;
    private int tickets;
    private final Barrels barrels;
    private final Random random = new Random();
    private static final String[] BARREL_IDS = {"A", "B", "C"};

    public Student(String nombre, int edad, int tickets, Barrels barrels) {
        this.nombre = nombre;
        this.edad = edad;
        this.tickets = tickets;
        this.barrels = barrels;
    }

    @Override
    public void run() {
        try {
            while (tickets > 0) {
                String barrelId = BARREL_IDS[random.nextInt(BARREL_IDS.length)];

                int request = Math.min(tickets, 1 + random.nextInt(tickets)); // Pide entre 1 y los tickets restantes

                int obtained = barrels.withdrawFromBarrel(barrelId, request);

                if (obtained > 0) {
                    tickets -= obtained;
                    System.out.println(nombre + " (Edad: " + edad + ") retiró " + obtained + " cerveza(s) del barril " + barrelId + ". Tickets restantes: " + tickets);
                }

                Thread.sleep(500 + random.nextInt(500)); // Simula el tiempo entre rondas
            }

            System.out.println(nombre + " se retira sin tickets.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
````
La clase ``Student`` representa a un estudiante que participa en una simulación de una fiesta, en la que puede retirar cerveza de barriles utilizando un número limitado de tickets. Al extender Thread, cada instancia de esta clase se ejecuta de manera concurrente, permitiendo simular múltiples estudiantes actuando de forma autónoma.

Cada objeto ``Student`` contiene:

- Un nombre (nombre)
- Una edad (edad)
- Una cantidad inicial de tickets (tickets) que puede canjear por cerveza
- Una referencia al objeto Barrels, que gestiona el acceso a los barriles compartidos
- Un generador de números aleatorios (Random) para simular decisiones impredecibles
- Un arreglo constante de IDs de barriles disponibles (BARREL_IDS), que son "A", "B" y "C".

El comportamiento principal se encuentra en el método run(), que es ejecutado cuando el hilo es iniciado. En este método:

Mientras el estudiante tenga tickets, entra en un bucle donde:

- Selecciona aleatoriamente un barril.
- Decide aleatoriamente cuántos tickets quiere usar en esta ronda (entre 1 y el número de tickets restantes).
- Intenta retirar esa cantidad de cerveza del barril seleccionado mediante el método ``withdrawFromBarrel``.
- Si logra obtener cerveza (obtained > 0):
- Se descuentan los tickets usados.
- Se imprime un mensaje con los detalles de la operación.
- Luego espera entre 500 y 1000 milisegundos antes de volver a intentar, simulando el paso del tiempo en una fiesta.

Cuando se le agotan los tickets, el estudiante imprime un mensaje indicando que se retira.

Este diseño modela de forma efectiva un patrón de consumidor concurrente, donde múltiples hilos (estudiantes) compiten por acceder a un recurso compartido (los barriles). 

### Proceso - proveedores
````
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

// Productor
class Provider extends Thread {
    private final Barrels barrels;
    private final Random random = new Random();

    public Provider(Barrels barrels, int id) {
        this.barrels = barrels;
        setName("Proveedor " + id); 
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
              
                    while (!barrels.hasAvailableCapacity()) {
                        // Si fue interrumpido durante la espera, salir inmediatamente
                        if (Thread.currentThread().isInterrupted()) return;
                    }

                    List<String> targets = new ArrayList<>();
                    if (barrels.getBarrel("A").getAvailableCapacity() > 0) targets.add("A");
                    if (barrels.getBarrel("C").getAvailableCapacity() > 0) targets.add("C");

                    if (!targets.isEmpty()) {
                        if (Thread.currentThread().isInterrupted()) return;

                        String idBarrel = targets.get(random.nextInt(targets.size()));
                        Barrel barrel = barrels.getBarrel(idBarrel);
                        int maxTheoretical = (int) Math.ceil(barrel.getCapacity() * 1.5);
                        int amount = 1 + random.nextInt(maxTheoretical);
                        barrels.rechargeBarrel(idBarrel, amount);
                    }

                
                Thread.sleep(1000 + random.nextInt(1000));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

````
La clase ``Provider`` representa a un proveedor en la simulación concurrente de una fiesta. Su función es recargar los barriles de cerveza cuando estos aún tienen capacidad disponible. Al extender la clase Thread, permite que múltiples proveedores se ejecuten en paralelo y de manera autónoma.

Cada instancia de Provider contiene:

- Una referencia al objeto ``Barrels``, que centraliza la gestión de todos los barriles compartidos.
- Un generador de números aleatorios (Random) para simular comportamientos impredecibles como la cantidad a recargar o el tiempo de espera.
- Un identificador numérico que se usa para nombrar el hilo con ``setName("Proveedor " + id)``, lo que facilita la depuración y trazabilidad en la salida por consola.

El comportamiento principal se encuentra en el método ``run()``, que se ejecuta al iniciar el hilo. Dentro de este método:

- El hilo entra en un ciclo que se mantiene activo mientras no sea interrumpido ``(!Thread.currentThread().isInterrupted())``.
- Verifica si hay capacidad disponible en algún barril mediante el método hasAvailableCapacity() del objeto Barrels. Si no hay capacidad, simplemente continúa iterando hasta que la haya.
- Identifica cuáles barriles son recargables directamente. Según la lógica del sistema, solo los barriles "A" y "C" pueden ser recargados directamente (el barril "B" se llena por transferencia interna).
- Si al menos uno de los barriles "A" o "C" tiene espacio disponible, selecciona uno aleatoriamente.
- Calcula una cantidad aleatoria de cerveza a recargar, desde 1 hasta el 150% de la capacidad del barril seleccionado, lo que simula intentos de sobrecarga (que pueden desencadenar lógicas de rebose o transferencia).
- Llama al método ``rechargeBarrel`` con la cantidad generada, el cual se encarga internamente de manejar desbordamientos o reglas personalizadas.
- Luego de cada intento de recarga, el proveedor espera entre 1 y 2 segundos ``(Thread.sleep)`` antes de iniciar una nueva iteración, simulando un intervalo entre cargas.
- Si el hilo es interrumpido en cualquier momento (ya sea mientras trabaja o duerme), atrapa la excepción ``InterruptedException``, marca el hilo como interrumpido nuevamente y finaliza de forma limpia.

### Explicación de la salida 
```
A,50,30
B,40,10
C,60,20
Estudiantes,5
Proveedores,2
```
Suponiendo este caso de prueba cuando se ejecuta el programa se vera lo siguiente: 
```
Estado inicial de los barriles  
ID: A, Cantidad: 30, Capacidad: 50  
ID: B, Cantidad: 10, Capacidad: 40  
ID: C, Cantidad: 20, Capacidad: 60  
```
Esto indica que los barriles se han leído correctamente del archivo y que ya tienen una cantidad inicial de cerveza.

Si hay estudiantes que son menores de edad tambien se muestra en la impresion de esta manera:  
```
Estudiante 1 (Edad: 17, Tickets: 5) no es válido para participar.
```

Si el estudiante es valido se muestra de la siguiente manera: 
```
Estudiante 2 (Edad: 21) retiró 3 cerveza(s) del barril B. Tickets restantes: 9 
```

Si hay espacio en los barriles entonces el proveedor puede recargarlo, tambien si se estan vaciando los recarga, y si existe desborde se muestra en la salida, reportando la cantidad de cerveza perdida. 
```
Proveedor 1 va a recargar 71 unidades en A  
Desbordamiento en A, transfiriendo 51 unidades a B.  
Desbordamiento en B, transfiriendo 21 unidades a C.  
Cerveza perdida: 5 unidades.  

Proveedor 2 va a recargar 40 unidades en C  
Desbordamiento en C, transfiriendo 10 unidades a B. 
```

La simulacion completa se veria de la siguiente forma: 
```
Estudiante 3 (Edad: 25) retiró 4 cerveza(s) del barril C. Tickets restantes: 3  
Proveedor 1 va a recargar 89 unidades en A  
Desbordamiento en A, transfiriendo 59 unidades a B.  
Desbordamiento en B, transfiriendo 19 unidades a C.  
Cerveza perdida: 3 unidades. 
```

Todo esto ocurre mientras los hilos se ejecutan de forma concurrente, creando una fiesta bien sincronizada.

Cuando todos los estudiantes se quedan sin tickets, el sistema imprime:
```
Estudiante 2 se retira sin tickets.  
Estudiante 3 se retira sin tickets.  
Estudiante 5 se retira sin tickets.  
Todos los estudiantes se quedaron sin tickets.  
Fiesta finalizada
```
Los proveedores son interrumpidos automáticamente para que el programa finalice sin dejar hilos colgados.

Finalmente, se imprime el estado final de los barriles y la cantidad de cerveza perdida:
```
ID: A, Cantidad final: 45  
ID: B, Cantidad final: 40  
ID: C, Cantidad final: 51  
Total de cerveza perdida por desbordamiento: 8 unidades.
```
Este programa simula una dinámica realista de productor-consumidor usando Threads, wait() y notifyAll() para sincronizar a múltiples hilos (estudiantes y proveedores) que comparten recursos limitados (barriles).

Se colocaron 4 casos de prueba en donde 3 de ellos son simulacion de la fiesta y 1 es una entrada invalida. 