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

### Analisis

Durante la fiesta de fin de semestre, se organizará una "cervezada" en la que los estudiantes reutilizarán el sistema de tres barriles comunicantes desarrollado en proyectos anteriores. Esta vez, se requiere una solución concurrente en Java, utilizando monitores e hilos (Threads) para coordinar adecuadamente el acceso a los barriles y simular de forma realista el consumo y la reposición de cerveza.

### Procesos Involucrados
Estudiantes (Consumidores): Hilos que intentan consumir cerveza de los barriles, de acuerdo a la cantidad de tickets que poseen.

Proveedores (Productores): Hilos encargados de recargar los barriles cuando haya espacio disponible.

### Recursos Críticos
Barriles de cerveza A, B y C: Cada barril tiene una capacidad máxima y una cantidad actual de cerveza. Al ser accedidos por múltiples hilos de forma concurrente, su manipulación debe ser protegida mediante sincronización para evitar condiciones de carrera.

### Operaciones Críticas
Retiro de cerveza: Los estudiantes deben verificar la cantidad disponible y retirar cerveza de un barril de forma atómica. Si la cantidad requerida no está disponible, deben esperar a que el barril sea recargado.

Recarga de cerveza: Los proveedores deben agregar cerveza solo si hay espacio en el barril. En caso de que se intente recargar un barril lleno, se debe esperar. Si ocurre un desborde, este debe ser registrado y reportado al final de la ejecución.

### Condiciones de Sincronización
Espera por recarga: Si un estudiante desea más cerveza de la que hay disponible, su hilo debe quedar bloqueado hasta que se recargue el barril.

Espera por espacio: Si un proveedor intenta recargar un barril lleno, debe esperar hasta que se libere espacio (es decir, que algún estudiante haya consumido).

Notificaciones: Cuando cambia el estado de un barril (ya sea por consumo o por recarga), se debe notificar a los hilos en espera para que reevalúen su condición.

### Decisiones de diseño 
Decisiones de Diseño

-Se implementó un monitor Barrels que centraliza las operaciones sobre los barriles y maneja la sincronización de hilos.

-Se agregaron flags de control (flagWithdraw, flagRecharge) para evitar conflictos entre operaciones simultáneas.

-Los estudiantes se validan por edad (>= 18) y tickets (>0). Los inválidos son descartados.

-Se maneja el desbordamiento según reglas específicas, y se reporta la cerveza perdida al final de la ejecución.

## Documentación
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
// Monitor
public class Barrels {
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
                    Utils.sleepSeconds(3 + (int) (Math.random() * 3)); // Simula un tiempo de recarga aleatorio entre 3 y 5 segundos
                    flagWithdraw = false; // Indica que ya no se está retirando
                    notify(); // Notifica a otros hilos que pueden continuar
                    return barrel.withdraw(amount);
                }else {
                    flagWithdraw = false; // Indica que ya no se está retirando
                    notify(); // Notifica a otros hilos que pueden continuar
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
        flagRecharge = true; // Indica que se está recargando
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
                Utils.sleepSeconds(3 + (int) (Math.random() * 3)); // Simula un tiempo de recarga aleatorio entre 3 y 5 segundos
                flagRecharge = false; // Indica que ya no se está recargando
                notify();
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
import java.util.Random;

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
                } else {
                    synchronized (barrels) {
                        barrels.wait(); // Espera si no pudo retirar nada
                    }
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
La clase ``Student`` representa a un estudiante que participa en la simulación de una fiesta, en la cual puede retirar cerveza de barriles utilizando un número limitado de tickets. Esta clase extiende Thread, lo que permite que múltiples instancias se ejecuten de forma concurrente y autónoma dentro del sistema.

Cada objeto ``Student`` tiene un nombre, una edad, un número inicial de tickets y una referencia al objeto Barrels que gestiona los barriles de cerveza. También utiliza una instancia de Random para introducir comportamientos aleatorios y una constante ``BARREL_IDS`` que contiene los identificadores de los barriles disponibles ("A", "B" y "C").

El método run, que es el punto de entrada cuando se inicia el hilo, ejecuta un ciclo mientras el estudiante tenga tickets disponibles. En cada iteración:

1. Elige aleatoriamente uno de los barriles para intentar retirar cerveza.

2. Calcula una cantidad aleatoria de cerveza a pedir, siempre dentro del límite de sus tickets restantes.

3. Llama al método ``withdrawFromBarrel`` del objeto ``Barrels`` para intentar retirar esa cantidad del barril seleccionado.

4.Si logra obtener cerveza (obtained > 0), descuenta la cantidad obtenida de sus tickets y muestra un mensaje indicando la operación.

5.Si no logra obtener nada, se sincroniza sobre el objeto barrels y entra en estado de espera (``wait``) hasta que otro hilo (por ejemplo, un proveedor que recargue) lo notifique, permitiéndole intentar de nuevo.

Luego de cada intento exitoso o fallido, espera entre 0.5 y 1 segundo antes de volver a intentar, simulando el tiempo entre rondas en una fiesta.
Cuando el estudiante ha usado todos sus tickets, imprime un mensaje indicando que se retira de la fiesta.
Esta clase ilustra un comportamiento típico de consumidores en un entorno concurrente, donde varios hilos compiten por un recurso compartido. La combinación de wait y notify en el objeto Barrels permite una cooperación fluida entre los estudiantes y los productores (proveedores de cerveza) en la simulación, evitando el uso ineficiente de la CPU por espera activa.

### Proceso - proveedores
````
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
                synchronized (barrels) {
                    while (!barrels.hasAvailableCapacity()) {
                        barrels.wait();

                        // Si fue interrumpido durante la espera, salir inmediatamente
                        if (Thread.currentThread().isInterrupted()) return;
                    }

                    List<String> targets = new ArrayList<>();
                    if (barrels.getBarrel("A").getAvailableCapacity() > 0) targets.add("A");
                    if (barrels.getBarrel("C").getAvailableCapacity() > 0) targets.add("C");

                    if (!targets.isEmpty()) {
                        if (Thread.currentThread().isInterrupted()) return;

                        String idBarrel = targets.get(random.nextInt(targets.size()));
                        int amount = 5 + random.nextInt(6);

                        System.out.println(getName() + " va a recargar " + amount + " unidades en " + idBarrel);

                        barrels.rechargeBarrel(idBarrel, amount);
                    }

                    barrels.notifyAll();
                }

                Thread.sleep(1000 + random.nextInt(1000));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
````
La clase ``Provider`` representa a un proveedor que, en el contexto de la simulación concurrente de la fiesta, se encarga de recargar los barriles de cerveza cuando hay capacidad disponible. Esta clase extiende Thread, lo que permite ejecutar múltiples proveedores en paralelo de forma autónoma.

Cada instancia de ``Provider`` tiene una referencia al objeto Barrels, que gestiona el estado de los barriles compartidos, y un generador de números aleatorios (Random) para simular decisiones y tiempos variables. Además, cada proveedor recibe un identificador único que se utiliza para establecer su nombre de hilo (setName("Proveedor " + id)), facilitando la trazabilidad de su actividad en consola.

El comportamiento principal está definido en el método run, que ejecuta un ciclo mientras el hilo no sea interrumpido. En cada iteración:

1. Sincroniza sobre el objeto barrels para garantizar exclusión mutua con otros hilos consumidores o productores.

2. Comprueba si hay capacidad disponible en los barriles usando ``hasAvailableCapacity()``. Si no hay espacio, entra en estado de espera (wait) hasta que otro hilo (como un estudiante) consuma cerveza y libere capacidad.

3. Luego, identifica qué barriles se pueden recargar directamente (solamente "A" y "C", ya que "B" no se puede recargar directamente). Si alguno de ellos tiene capacidad disponible, selecciona uno de forma aleatoria.

4. Calcula una cantidad aleatoria a recargar, desde 1 hasta el 150% de la capacidad del barril elegido (simulando que algunos proveedores podrían intentar sobrecargar).

5. Llama al método ``rechargeBarrel``, el cual maneja internamente el desbordamiento y transferencia de exceso según la lógica definida en ``Barrels``.

6. Después de recargar, notifica a otros hilos (``notifyAll``) para que estudiantes o proveedores puedan continuar sus operaciones si estaban en espera.

7. Finalmente, duerme entre 1 y 2 segundos antes de iniciar otra ronda, simulando un tiempo de espera realista entre recargas.

En caso de que el hilo sea interrumpido en cualquier momento, el Provider termina su ejecución limpiamente, respetando el control del ciclo y el uso de ``Thread.interrupt()`` para una finalización segura.

Esta clase modela un productor en el clásico problema productor-consumidor, actuando de forma coordinada con los estudiantes (consumidores) y manteniendo la integridad del sistema mediante sincronización y comunicación con ``wait`` y ``notifyAll``.

