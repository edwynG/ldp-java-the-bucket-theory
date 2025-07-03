class Barrel {
    private final String id;
    private final int capacity;
    private int currentAmount;

    public Barrel(String id, int capacity, int initialAmount) {
        this.id = id;
        this.capacity = capacity;
        this.currentAmount = initialAmount;
    }

    public synchronized int withdraw(int requestedAmount) {
        if (currentAmount >= requestedAmount) {
            currentAmount -= requestedAmount;
            return requestedAmount;
        } else {
            int served = currentAmount;
            currentAmount = 0;
            return served;
        }
    }

    public synchronized void recharge(int amount) {
        if (currentAmount + amount > capacity) {
            currentAmount = capacity; // Fills to capacity
        } else {
            currentAmount += amount;
        }
    }

    public synchronized int getCurrentAmount() {
        return currentAmount;
    }

    public String getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }
}

// Monitor
public class Barrels {
    private Barrel[] barrels;

    public Barrels(Barrel[] barrels) {
        if (barrels == null || barrels.length != 3) {
            throw new IllegalArgumentException("Se requieren exactamente 3 barriles.");
        }
        this.barrels = barrels;
    }
}
