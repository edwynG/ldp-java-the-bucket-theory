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
