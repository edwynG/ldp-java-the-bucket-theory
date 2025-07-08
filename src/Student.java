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