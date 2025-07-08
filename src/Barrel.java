public class Barrel {
    private final String id;
    private final int capacity;
    private int currentAmount;

    public Barrel(String id, int capacity, int initialAmount) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("El ID del barril no puede ser nulo o vacío.");
        }
        if (capacity < 1) {
            throw new IllegalArgumentException("Barril " + "C" + " - La capacidad del barril debe ser al menos 1");
        }

        this.id = id;
        this.capacity = capacity;
        this.currentAmount = Math.max(initialAmount, 0);
                                                                          
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

    public synchronized int getAvailableCapacity(){
        return capacity - currentAmount;
    }


    public String getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCurrentAmount(int amount) {
        if (amount < 0 || amount > capacity) {
            throw new IllegalArgumentException("La cantidad debe estar entre 0 y la capacidad del barril.");
        }
        this.currentAmount = amount;
    }
}
