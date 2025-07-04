//Consumidor
public class Student extends Thread {
    private final String nombre;
    private final int edad;
    private int tickets;
    private final Barrels barrels;
    private final Random random = new Random();

    public Student(String nombre, int edad, int tickets, Barrels barrels) {
        this.nombre = nombre;
        this.edad = edad;
        this.tickets = Math.min(tickets, 10); // Máximo 10 tickets
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
                // Pedir entre 1 y 10 cervezas, pero no más de los tickets disponibles
                int requestedAmount = random.nextInt(10) + 1; 
                requestedAmount = Math.min(requestedAmount, tickets); // No exceder tickets
                
                int served = 0;
                
                while (served < requestedAmount) {
                    int remaining = requestedAmount - served;
                    int obtained = barrels.withdraw(remaining);//
                    
                    if (obtained > 0) {
                        served += obtained;
                        tickets -= obtained;
                        System.out.println(nombre + " retiró " + obtained + " cervezas. Tickets restantes: " + tickets);
                    }
                    
                    if (served < requestedAmount) {
                        synchronized (barrels) {
                            barrels.wait();
                        }
                    }
                }
                
                Thread.sleep(random.nextInt(1000)); // Tiempo entre pedidos
            }
            System.out.println(nombre + " se retira de la fiesta por no tener tickets");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public int getTickets() {
        return tickets;
    }
}