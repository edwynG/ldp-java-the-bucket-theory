public class Utils {
    static void printMessage(String message) {
        for (char c : message.toCharArray()) {
            System.out.print(c);
            try {
                Thread.sleep(40); // Simulate some delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
            }
        }
        System.out.println();
    }

    static void sleepSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
