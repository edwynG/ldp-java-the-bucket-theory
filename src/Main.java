import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Debe proporcionar la ruta del archivo como argumento.");
            return;
        }

        String filePath = args[0];
        List<Barrel> barrels = new ArrayList<>();
        int numStudents = 0;
        int numProviders = 0;

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
            return;
        }

        if (numStudents <= 0) {
            System.err.println("El número de estudiantes debe ser mayor a 0.");
            return;

        }
        if (barrels.size() != 3) {
            System.err.println("Debe haber exactamente 3 barriles.");
            return;
        }
        
        Barrel[] barrelsArray = barrels.toArray(new Barrel[0]);
        Utils.overflowFromA(barrelsArray);
        Utils.overflowFromC(barrelsArray);
        Utils.overflowFromB(barrelsArray);
        Barrels monitor = new Barrels(barrelsArray);

        System.out.println("Estado inicial de los barriles");
        for (Barrel barrel : barrelsArray) {
            System.out.println("ID: " + barrel.getId() +
                    ", Cantidad: " + barrel.getCurrentAmount() +
                    ", Capacidad: " + barrel.getCapacity());
        }

        List<Thread> threads = new ArrayList<>();
        int estudiantesValidos = 0;

        for (int i = 0; i < numStudents; i++) {
            int edad = 16 + (i % 10);
            int tickets = 5 + (i % 6);
            if (edad >= 18 && tickets > 0) {
                Student s = new Student("Estudiante " + (i + 1), edad, tickets, monitor);
                threads.add(s);
                s.start();
                estudiantesValidos++;
            } else {
                System.out.println("Estudiante " + (i + 1) + " no es válido para participar. (Edad: " + edad
                        + ", Tickets: " + tickets + ")");
            }
        }

        if (estudiantesValidos == 0) {
            System.out.println("\n No hay estudiantes válidos para la fiesta. Se cancela el evento.");
            return;
        }

        for (int i = 0; i < numProviders; i++) {
            Provider p = new Provider(monitor, i + 1); // ID del proveedor (empezando desde 1)
            threads.add(p);
            p.start();
        }

        for (Thread t : threads) {
            if (t instanceof Student) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                t.interrupt();
            }
        }

        System.out.println("\nFiesta finalizada");
        for (Barrel barrel : barrelsArray) {
            System.out.println("ID: " + barrel.getId() +
                    ", Cantidad final: " + barrel.getCurrentAmount());
        }

        System.out.println("Total de cerveza perdida por desbordamiento: " + monitor.getLostBeer() + " unidades.");
    }
}
