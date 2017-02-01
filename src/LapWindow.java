import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Kenny Tang 2017.
 */
public class LapWindow extends JFrame {

    LapWindow(WorkflowTimer timerInstance) {
        JLabel infoLabel = new JLabel("Task Name");
        JTextField taskName = new JTextField();
        JButton submitButton = new JButton("Submit");
        BoxLayout rootLayout = new BoxLayout(getContentPane(), BoxLayout.Y_AXIS);

        this.setLayout(rootLayout);
        this.add(infoLabel);
        this.add(taskName);
        this.add(submitButton);

        submitButton.addActionListener((ActionEvent e) -> submit(timerInstance, taskName.getText()));
        taskName.addActionListener((ActionEvent e) -> submit(timerInstance, taskName.getText()));

        //Default properties for LapWindow
        this.setSize(200, 100);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setAlwaysOnTop(true);
        this.setVisible(true);
    }

    private void submit(WorkflowTimer timerInstance, String text) {
        timerInstance.addTask(text);
        this.dispose();
    }
}
