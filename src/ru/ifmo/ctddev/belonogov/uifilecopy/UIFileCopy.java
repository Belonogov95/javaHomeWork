package ru.ifmo.ctddev.belonogov.uifilecopy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.*;

/**
 * Created by vanya on 17.05.15.
 */
public class UIFileCopy implements ActionListener {

    public long calculateSize(String dir) {
        Path path = Paths.get(dir);
        CalcSize visitor = new CalcSize();
        try {
            Files.walkFileTree(path, visitor);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return visitor.getTotalSize();
    }

    public void copy(String from, String to) {
        System.out.println(calculateSize(from));



//
//
//        JPanel panel = new JPanel(new FlowLayout());
//        JButton button = new JButton("_text_");
//        button.setActionCommand("ttt");
//        button.addActionListener(this);
//        panel.add(button);
//
//        JFrame frame = new JFrame("SimpleDemo"); // Добавление панели к окну
//        frame.getContentPane().add(panel); // Удалять при закрытии
//        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//        frame.pack(); // подгонка размера
//        frame.setVisible(true); // Показать пользователю


    }

    public static void main(String[] args) {
        if (false) {
            assert (args.length == 2);
            assert (args[0] != null && args[1] != null);
            new UIFileCopy().copy(args[0], args[1]);
        } else {
            new UIFileCopy().copy("a", "b");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.err.println(e.getActionCommand());
        if ("ttt".equals(e.getActionCommand())) {
            System.out.println("pushed");
        }
    }
}
