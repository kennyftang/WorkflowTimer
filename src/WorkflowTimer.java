import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.TimeUnit;

/**
 * Kenny Tang 2017.
 */
class WorkflowTimer extends JFrame implements Runnable {

    //Instance Variables and Components
    private JLabel timeText;
    private static JLabel taskListLabel;
    private JPanel taskPanel;
    private JButton timeRight;
    private JButton timeLeft;
    private String timeString;
    private boolean timing;
    private boolean newTask;
    private long theTime;
    private long lastTime;
    private long lastTaskTime;

    //Initiation
    WorkflowTimer() {
        //Default time value
        timeString = "00:00\t\t";
        //Panel contains the task list
        taskPanel = new JPanel();
        //Set the default text of the label to default time value
        timeText = new JLabel(timeString);
        //Set the default text of the two buttons
        timeRight = new JButton("Start");
        timeLeft = new JButton("Reset");
        //Start timing the next task when timer starts
        newTask = true;
        //This layout is for WorkflowTimer
        GridBagLayout rootLayout = new GridBagLayout();

        //Set the font of the time label to sans serif since it looks nicer
        timeText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 48));

        //Set the layout of WorkflowTimer and all the panels
        this.setLayout(rootLayout);
        //Set layout of the taskPanel to vertical box
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));

        //Disable the reset button
        timeLeft.setEnabled(false);
        //ActionListener for each button
        timeLeft.addActionListener(this::onButton);
        timeRight.addActionListener(this::onButton);

        //Setup the GridBagLayout Constraints
        GridBagConstraints timeTextC = new GridBagConstraints();
        timeTextC.gridx = 1;
        timeTextC.gridy = 0;
        timeTextC.gridwidth = 2;
        timeTextC.anchor = GridBagConstraints.CENTER;
        timeTextC.insets = new Insets(10, 10, 10, 10);

        GridBagConstraints taskPanelC = new GridBagConstraints();
        taskPanelC.gridx = 5;
        taskPanelC.weightx = 1;
        taskPanelC.gridy = 0;
        taskPanelC.anchor = GridBagConstraints.NORTHEAST;
        taskPanelC.insets = new Insets(10, 10, 10, 10);

        GridBagConstraints timeLeftC = new GridBagConstraints();
        timeLeftC.gridx = 1;
        timeLeftC.gridy = 1;
        timeLeftC.anchor = GridBagConstraints.SOUTHWEST;

        GridBagConstraints timeRightC = new GridBagConstraints();
        timeRightC.gridx = 2;
        timeRightC.gridy = 1;
        timeRightC.anchor = GridBagConstraints.SOUTHWEST;

        GridBagConstraints horizontalGlueC = new GridBagConstraints();
        horizontalGlueC.weightx = 1;
        horizontalGlueC.gridx = 2;
        horizontalGlueC.gridwidth = 5;

        //Header for the Task Panel
        taskListLabel = new JLabel("Task List");
        taskListLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        taskListLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        taskPanel.add(taskListLabel);

        //Add everything to the frame
        this.add(timeLeft, timeLeftC);
        this.add(timeRight, timeRightC);
        this.add(timeText, timeTextC);
        this.add(taskPanel, taskPanelC);

        //Default properties for WorkflowTimer
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setAlwaysOnTop(true);
        this.setVisible(true);
        this.setResizable(false);
        this.pack();

        //Thread used to keep time
        Thread stopwatch = new Thread(this);
        stopwatch.start();
    }

    void addTask(String name) {
        //Get the time since the last task
        long timeElapsed = theTime - lastTaskTime;
        long elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(timeElapsed) % 60;
        long elapsedMin = TimeUnit.MILLISECONDS.toMinutes(timeElapsed) % 60;
        long elapsedHours = TimeUnit.MILLISECONDS.toHours(timeElapsed);
        //Format the name with the time
        name += String.format("\t\t%02d:%02d:%02d", elapsedHours, elapsedMin, elapsedSeconds);
        //Create a JLabel with the name and time
        JLabel task = new JLabel(name);
        //Set the label to align to the right
        task.setAlignmentX(Component.RIGHT_ALIGNMENT);
        //Allow deleting of the label
        task.addMouseListener(new ClickListener());
        //Add task to the task panel
        taskPanel.add(task);
        this.pack();
        //Tell thread to set lastTaskTime
        newTask = true;
    }

    private void onButton(ActionEvent e) {
        String currentText = ((JButton) e.getSource()).getText();
        boolean enabled = ((JButton) e.getSource()).isEnabled();
        if (enabled)
            switch (currentText) {
                case "Reset":
                    theTime = 0;
                    timeString = "00:00\t\t";
                    timeText.setText(timeString);
                    this.timeLeft.setEnabled(false);
                    taskPanel.removeAll();
                    taskPanel.add(WorkflowTimer.taskListLabel);
                    break;
                case "Lap":
                    new LapWindow(this);
                    break;
                case "Start":
                    timeLeft.setText("Lap");
                    timeLeft.setEnabled(true);
                    timeRight.setText("Stop");
                    timing = true;
                    break;
                case "Stop":
                    timeLeft.setText("Reset");
                    timeRight.setText("Start");
                    timing = false;
            }
    }

    @Override
    public void run() {
        //Run forever
        while (true) {
            //Continue if timer is not running
            if (!timing) {
                Thread.yield();
                lastTime = System.currentTimeMillis();
                continue;
            }
            //Set the elapsedTime to current - last
            long curTime = System.currentTimeMillis();
            long elapsedInstanceTime = curTime - lastTime;
            lastTime = curTime;
            //Add elapsedTime to theTime
            theTime += elapsedInstanceTime;
            //Initialize lastTaskTime
            if (newTask) {
                lastTaskTime = theTime;
                newTask = false;
            }
            //Get each time unit
            long elapsedMilli = TimeUnit.MILLISECONDS.toMillis(theTime) % 100;
            long elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(theTime) % 60;
            long elapsedMin = TimeUnit.MILLISECONDS.toMinutes(theTime) % 60;
            long elapsedHours = TimeUnit.MILLISECONDS.toHours(theTime);
            //Find out how many colons we need and format the string as appropriate
            if (elapsedHours > 0)
                timeString = String.format("%02d:%02d:%02d:%02d\t\t", elapsedHours, elapsedMin, elapsedSeconds, elapsedMilli);
            else if (elapsedMin > 0)
                timeString = String.format("%02d:%02d:%02d\t\t", elapsedMin, elapsedSeconds, elapsedMilli);
            else
                timeString = String.format("%02d:%02d\t\t", elapsedSeconds, elapsedMilli);
            //Set the time label's text to the timeString
            if (timeString.length() != timeText.getText().length()) {
                timeText.setText(timeString);
                this.pack();
            } else
                timeText.setText(timeString);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {}
        }
    }

    private class ClickListener implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            WorkflowTimer.this.taskPanel.remove(e.getComponent());
            newTask = true;
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }
}