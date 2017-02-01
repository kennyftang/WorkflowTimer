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
        //Panel contains the time as a JLabel
        JPanel timePanel = new JPanel();
        //Panel contains the task list
        taskPanel = new JPanel();
        //Panel contains timeLeft and timeRight buttons
        JPanel buttonPanel = new JPanel();
        //Set the default text of the label
        timeText = new JLabel(timeString);
        //Set the default text of the two buttons
        timeRight = new JButton("Start");
        timeLeft = new JButton("Reset");
        //Do not start timing
        timing = false;
        //Start timing the next task when timer starts
        newTask = true;
        //This layout is for WorkflowTimer
        BoxLayout rootLayout = new BoxLayout(getContentPane(), BoxLayout.Y_AXIS);
        //This layout is for the TaskPanel
        BoxLayout taskLayout = new BoxLayout(taskPanel, BoxLayout.Y_AXIS);
        //This layout is for the button panel, displaying left -> right
        FlowLayout buttonLayout = new FlowLayout(FlowLayout.LEFT, 10, 5);
        //This layout is for the time panel and is used to center the label
        GridBagLayout timeLayout = new GridBagLayout();

        //Set the font of the time label to helvetica since it looks nicer
        timeText.setFont(new Font("Helvetica", Font.PLAIN, 48));
        //Set no vertical gap

        //Set the layout of WorkflowTimer and all the panels
        this.setLayout(rootLayout);
        timePanel.setLayout(timeLayout);
        taskPanel.setLayout(taskLayout);
        buttonPanel.setLayout(buttonLayout);
        //Align the time label to the center

        //Disable the reset button
        timeLeft.setEnabled(false);
        //ActionListener for the left button
        timeLeft.addActionListener((ActionEvent e) -> {
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
                }
        });
        //AcitonListener for the right button
        timeRight.addActionListener((ActionEvent e) -> {
            String currentText = ((JButton) e.getSource()).getText();
            switch (currentText) {
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
        });

        //Setup the gridBagLayout
        GridBagConstraints timeTextC = new GridBagConstraints();
        timeTextC.gridx = 1;
        timeTextC.gridy = 0;
        timeTextC.gridwidth = 3;
        timeTextC.anchor = GridBagConstraints.CENTER;
        timeTextC.weighty = 1;
        timeTextC.weightx = 1;

        GridBagConstraints taskPanelC = new GridBagConstraints();
        taskPanelC.gridx = 4;
        taskPanelC.gridy = 0;
        taskPanelC.gridwidth = 1;
        taskPanelC.anchor = GridBagConstraints.NORTHEAST;
        taskPanelC.weighty = 1;
        taskPanelC.weightx = 1;
        taskPanelC.insets = new Insets(10, 10, 10, 10);

        //Add all the components to the corresponding containers
        timePanel.add(timeText, timeTextC);
        timePanel.add(taskPanel, taskPanelC);

        taskListLabel = new JLabel("Task List");
        taskListLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        taskListLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        taskPanel.add(taskListLabel);
        buttonPanel.add(timeLeft);
        buttonPanel.add(timeRight);
        this.add(timePanel);
        this.add(buttonPanel);

        //Default properties for WorkflowTimer
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setAlwaysOnTop(true);
        this.setVisible(true);
        this.setPreferredSize(new Dimension(400, 150));
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
        JLabel task = new JLabel(name, SwingConstants.RIGHT);
        //Allow deleting of the label
        task.addMouseListener(new ClickListener());
        //Add task to the task panel
        taskPanel.add(task);
        this.pack();
        //Tell thread to set lastTaskTime
        newTask = true;
    }

    @Override
    public void run() {
        //Run forever
        while(true) {
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
            if(newTask) {
                lastTaskTime = theTime;
                newTask = false;
            }
            //Get each time unit
            long elapsedMilli = TimeUnit.MILLISECONDS.toMillis(theTime) % 100;
            long elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(theTime) % 60;
            long elapsedMin = TimeUnit.MILLISECONDS.toMinutes(theTime) % 60;
            long elapsedHours = TimeUnit.MILLISECONDS.toHours(theTime);
            //Find out how many colons we need and format the string as appropriate
            if(elapsedHours > 0)
                timeString = String.format("%02d:%02d:%02d:%02d\t\t", elapsedHours, elapsedMin, elapsedSeconds, elapsedMilli);
            else if(elapsedMin > 0)
                timeString = String.format("%02d:%02d:%02d\t\t", elapsedMin, elapsedSeconds, elapsedMilli);
            else
                timeString = String.format("%02d:%02d\t\t", elapsedSeconds, elapsedMilli);
            //Set the time label's text to the timeString
            if(timeString.length() != timeText.getText().length()) {
                timeText.setText(timeString);
                this.pack();
            } else
                timeText.setText(timeString);
        }
    }

    private class ClickListener implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            WorkflowTimer.this.taskPanel.remove(e.getComponent());
            newTask = true;
            System.out.println(WorkflowTimer.this.getSize());
        }

        @Override
        public void mousePressed(MouseEvent e) {}

        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}
    }
}