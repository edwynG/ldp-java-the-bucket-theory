// Productor
class Provider extends Thread {
    private final Barrels barrels;
    private final Random random = new Random();
    private final int id;

    public Provider(Barrels barrels, int id) {
        this.barrels = barrels;
        this.id = id;
        setName("Proveedor-" + id); 
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                synchronized (barrels) {
                    while (!barrels.hasAvailableCapacity()) {
                        barrels.wait();
                    }

                    List<String> targets = new ArrayList<>();
                    if (barrels.getBarrel("A").getAvailableCapacity() > 0) targets.add("A");
                    if (barrels.getBarrel("C").getAvailableCapacity() > 0) targets.add("C");

                    if (!targets.isEmpty()) {
                        String idBarrel = targets.get(random.nextInt(targets.size()));
                        int amount = 5 + random.nextInt(6);
                        barrels.rechargeBarrel(idBarrel, amount);
                        System.out.println("Proveedor " + id + " recargó " + amount + " unidades en " + idBarrel);
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