public class Main {
    public static void main(String[] args) {
         Thread t = new Thread(() ->{
            String mensaje = "hola mundo";
            Utils.printMessage(mensaje);
         });

        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("El hilo ha terminado de imprimir el mensaje.");
    }   
}
