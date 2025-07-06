//Consumidor
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
        this.tickets = Math.min(tickets, 10);
        this.barrels = barrels;
    }

    @Override
    public void run() {
        if (edad < 18) {
            System.out.println(nombre + " es menor de edad. No puede consumir.");
            return;
        }

        try {
            while (tickets > 0) {
                String barrelId = BARREL_IDS[random.nextInt(BARREL_IDS.length)];
                int obtained = barrels.withdrawFromBarrel(barrelId, 1);
                if (obtained > 0) {
                    tickets -= obtained;
                    System.out.println(nombre + " retiró " + obtained + " cerveza(s) del barril " + barrelId + ". Tickets restantes: " + tickets);
                } else {
                    synchronized (barrels) {
                        barrels.wait();
                    }
                }
                Thread.sleep(500 + random.nextInt(500));
            }
            System.out.println(nombre + " se retira sin tickets.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}