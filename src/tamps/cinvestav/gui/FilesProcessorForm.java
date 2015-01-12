package tamps.cinvestav.gui;

import tamps.cinvestav.fileprocessors.GpsLoggerFileProcessor_Line;
import tamps.cinvestav.fileprocessors.GpsLoggerFileProcessor_Pins;
import tamps.cinvestav.fileprocessors.SmartphoneFileProcessor_Pins;
import tamps.cinvestav.fileprocessors.SmartphoneFileProcessor_PureLine;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class FilesProcessorForm {
    private JPanel panel1;
    private JButton btnSelectFile;
    private JLabel lblFileInputName;
    private JRadioButton rdSmartphone;
    private JRadioButton rdGpsLogger;
    private JRadioButton rdLines;
    private JRadioButton rdPins;
    private JButton btnProcess;
    private JButton btnExit;
    private File fileInput = null;

    public FilesProcessorForm() {
        btnSelectFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnVal = fileChooser.showOpenDialog(panel1);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    fileInput = fileChooser.getSelectedFile();
                    lblFileInputName.setText(fileInput.getPath());
                } else {
                    System.out.println("Open command cancelled by user.");
                }
            }
        });
        btnProcess.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validate()) {
                    process();
                }
            }

            private void process() {
                try {
                    String fileInputPath = getFileOutputPath();
                    String fileInputName = fileInput.getName().substring(0, fileInput.getName().lastIndexOf('.'));
                    String outputFileFullPath;
                    String inputFileFullPath = fileInput.getPath();
                    if (rdGpsLogger.isSelected()) {
                        if (rdLines.isSelected()) {
                            outputFileFullPath = fileInputPath + File.separator + fileInputName + "-lines.kml";
                            GpsLoggerFileProcessor_Line l = new GpsLoggerFileProcessor_Line(inputFileFullPath, outputFileFullPath);
                            l.translateToKMLLines();
                        } else {
                            outputFileFullPath = fileInputPath + File.separator + fileInputName +"-pins.kml";
                            GpsLoggerFileProcessor_Pins p = new GpsLoggerFileProcessor_Pins(inputFileFullPath, outputFileFullPath);
                            p.translateToKMLPins();
                        }
                    } else {
                        if (rdLines.isSelected()) {
                            outputFileFullPath = fileInputPath + File.separator + fileInputName +"-lines.kml";
                            SmartphoneFileProcessor_PureLine l = new SmartphoneFileProcessor_PureLine(inputFileFullPath, outputFileFullPath);
                            l.translateToKMLLines();
                        } else {
                            outputFileFullPath = fileInputPath + File.separator + fileInputName +"-pins.kml";
                            SmartphoneFileProcessor_Pins p = new SmartphoneFileProcessor_Pins(inputFileFullPath, outputFileFullPath);
                            p.translateToKMLPins();
                        }
                    }
                    JOptionPane.showMessageDialog(panel1, "File translated, look into: " + outputFileFullPath, "Success!", JOptionPane.INFORMATION_MESSAGE);
                }catch(Exception e){
                    System.out.println("E");
                    JOptionPane.showMessageDialog(panel1, "Something is wrong, I mean wrooooonnnng! " + "\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }

            private String getFileOutputPath() {
                String fullPath = fileInput.getPath();
                return fullPath.substring(0, fullPath.lastIndexOf(File.separator));
            }

            private boolean validate() {
                if (fileInput == null) {
                    JOptionPane.showMessageDialog(panel1, "Select a file", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                if (!rdGpsLogger.isSelected() && !rdSmartphone.isSelected()) {
                    JOptionPane.showMessageDialog(panel1, "Select a file input type", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                if (!rdLines.isSelected() && !rdPins.isSelected()) {
                    JOptionPane.showMessageDialog(panel1, "Select a file output type", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                return true;
            }
        });
        btnExit.addActionListener(e -> {
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("FilesProcessorForm");
        frame.setContentPane(new FilesProcessorForm().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);
        frame.setVisible(true);
    }
}
