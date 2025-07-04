// Productor
public class Provider extends Thread {
    private final Barrels barrels;
    private final int totalCapacity;
    private boolean active = true;

    public Provider(Barrels barrels, int totalCapacity) {
        this.barrels = barrels;
        this.totalCapacity = totalCapacity;
    }

    @Override
    public void run() {
        try {
            while (active) {
                // Verificar si hay capacidad disponible en al menos un barril
                if (barrels.hasAvailableCapacity()) {
                    barrels.recharge(totalCapacity);
                    System.out.println("Proveedor recargó el sistema");
                }
                
                Thread.sleep(500 + (int)(Math.random() * 1000));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Proveedor interrumpido");
        }
    }
    
    public void deactivate() {
        this.active = false;
    }
}