/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aula1;

import java.awt.*;
import javax.swing.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 *
 * @author ia941
 */
public class Aula1 {
    
    private static void createWindow(RobotController controller) {
        // Create a small window to get user input, using a JFrame
        JFrame window = new JFrame("Aula 1 - IA941 1S2018");
        JLabel textLabel = new JLabel("To Move the Robot press the WASD keys",SwingConstants.CENTER);
                                       textLabel.setPreferredSize(new Dimension(300, 100));  
        textLabel.setFocusable(true);
        // Add a key listener
        textLabel.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent event) {
                char ch = event.getKeyChar();
                if (ch == 'w' ||ch == 'W'
                    || ch == 'a' || ch == 'A'
                    || ch == 's' || ch == 'S'
                    || ch == 'd' || ch == 'D') {
                // Log to console
                System.out.println(event.getKeyChar());
                }
                
                if (ch == 'w' || ch == 'W') {
                    controller.moveForward();
                } 
            }
            @Override
            public void keyReleased(KeyEvent event) {   
                char ch = event.getKeyChar();
            }
            @Override
            public void keyTyped(KeyEvent event) {
                char ch = event.getKeyChar();
            }             
        });     
        
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        window.getContentPane().add(textLabel, BorderLayout.CENTER); 
        window.setLocationRelativeTo(null);
        window.pack();
        window.setVisible(true);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
     
        // Create window for capturing keypresses.
        RobotController controller = new RobotController();
        controller.createWorld();
        createWindow(controller);
       
    }

}
