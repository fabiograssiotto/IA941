/*
 * IA941 - Laboratório de Arquiteturas Cognitivas
 * 1S2018 - FEEC - Unicamp
 * Autor: Fabio Grassiotto - RA 890441
 */
package IA941.ManualController;

public class Aula1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // Create window to capture the user input.
        /* Create and display the form */
        ControllerUi ui = new ControllerUi();

        // Create Robot Controller
        RobotController controller = new RobotController(ui);
        controller.createWorld();

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ui.setVisible(true);
            }
        });
        ui.setController(controller);
    }

}
