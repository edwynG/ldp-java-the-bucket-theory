class Utils {
    static void sleepSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static public void overflowFromA(Barrel[] barrels) {
        Barrel barrelA = barrels[0];
        Barrel barrelB = barrels[1];
        Barrel barrelC = barrels[2];

        int excesoA = barrelA.getCurrentAmount() - barrelA.getCapacity();
        if (excesoA > 0) {
            // A se ajusta a su capacidad máxima
            barrelA.setCurrentAmount(barrelA.getCapacity());
            // B recibe el exceso
            int nuevaCantidadB = barrelB.getCurrentAmount() + excesoA;
            int excesoB = nuevaCantidadB - barrelB.getCapacity();
            if (excesoB > 0) {
                // B se ajusta a su capacidad máxima
                barrelB.setCurrentAmount(barrelB.getCapacity());
                // El exceso de B va al barril de menor cantidad (A o C)
                Barrel minBarrel = barrelA.getCurrentAmount() <= barrelC.getCurrentAmount() ? barrelA : barrelC;
                int nuevaCantidadMin = minBarrel.getCurrentAmount() + excesoB;
                // Si quieres limitar también aquí, puedes usar Math.min(nuevaCantidadMin,
                // minBarrel.getCapacity())
                minBarrel.setCurrentAmount(Math.min(nuevaCantidadMin, minBarrel.getCapacity()));
            } else {
                barrelB.setCurrentAmount(nuevaCantidadB);
            }
        }
    }

    static public void overflowFromC(Barrel[] barrels) {
        Barrel barrelA = barrels[0];
        Barrel barrelB = barrels[1];
        Barrel barrelC = barrels[2];

        int excesoC = barrelC.getCurrentAmount() - barrelC.getCapacity();
        if (excesoC > 0) {
            // C se ajusta a su capacidad máxima
            barrelC.setCurrentAmount(barrelC.getCapacity());
            // B recibe el exceso
            int nuevaCantidadB = barrelB.getCurrentAmount() + excesoC;
            int excesoB = nuevaCantidadB - barrelB.getCapacity();
            if (excesoB > 0) {
                // B se ajusta a su capacidad máxima
                barrelB.setCurrentAmount(barrelB.getCapacity());
                // El exceso de B va al barril de menor cantidad (A o C)
                Barrel minBarrel = barrelA.getCurrentAmount() <= barrelC.getCurrentAmount() ? barrelA : barrelC;
                int nuevaCantidadMin = minBarrel.getCurrentAmount() + excesoB;
                minBarrel.setCurrentAmount(Math.min(nuevaCantidadMin, minBarrel.getCapacity()));
            } else {
                barrelB.setCurrentAmount(nuevaCantidadB);
            }
        }
    }

    static public void overflowFromB(Barrel[] barrels) {
        Barrel barrelA = barrels[0];
        Barrel barrelB = barrels[1];
        Barrel barrelC = barrels[2];

        int excesoB = barrelB.getCurrentAmount() - barrelB.getCapacity();
        if (excesoB > 0) {
            // B se ajusta a su capacidad máxima
            barrelB.setCurrentAmount(barrelB.getCapacity());
            // El exceso de B va al barril de menor cantidad (A o C)
            Barrel minBarrel = barrelA.getCurrentAmount() <= barrelC.getCurrentAmount() ? barrelA : barrelC;
            int nuevaCantidadMin = minBarrel.getCurrentAmount() + excesoB;
            minBarrel.setCurrentAmount(Math.min(nuevaCantidadMin, minBarrel.getCapacity()));
        }
    }

}
