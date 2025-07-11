import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Random;

public class Main {
    private static  int numStudents = 0; // Número total de estudiantes
    private static int numProviders = 0; // Número total de proveedores

    public static void main(String[] args) {
        Barrel[] barrelsArray = new Barrel[3];
        entryFromFile(args, barrelsArray);
        Barrels monitor = new Barrels(barrelsArray);
    
        System.out.println("Estado inicial de los barriles");
        for (Barrel barrel : barrelsArray) {
            System.out.println("ID: " + barrel.getId() +
                    ", Cantidad: " + barrel.getCurrentAmount() +
                    ", Capacidad: " + barrel.getCapacity());
        }
    
        List<Thread> threads = new ArrayList<>();
        int estudiantesValidos = 0;
        Random random = new Random();

        for (int i = 0; i < numStudents; i++) {
            int edad = 16 + random.nextInt(35);      
            int tickets = 1 + random.nextInt(15);      

        
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
            System.out.println("\nNo hay estudiantes válidos para la fiesta. Se cancela el evento.");
            return;
        }
    
        for (int i = 0; i < numProviders; i++) {
            Provider p = new Provider(monitor, i + 1); // ID del proveedor (empezando desde 1)
            threads.add(p);
            p.start();
        }
    
        // Esperar a que los estudiantes terminen
        for (Thread t : threads) {
            if (t instanceof Student) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    
        // Interrumpimos solo los proveedores que aún siguen vivos
        for (Thread t : threads) {
            if (t instanceof Provider && t.isAlive()) {
                t.interrupt();
            }
        }
    
        System.out.println("Todos los estudiantes se quedaron sin tickets.");
        System.out.println("Fiesta finalizada");
    
        for (Barrel barrel : barrelsArray) {
            System.out.println("ID: " + barrel.getId() +
                    ", Cantidad final: " + barrel.getCurrentAmount());
        }
    
        System.out.println("Total de cerveza perdida por desbordamiento: " + monitor.getLostBeer() + " unidades.");
    }
    

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

}