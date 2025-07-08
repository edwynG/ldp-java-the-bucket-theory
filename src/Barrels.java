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