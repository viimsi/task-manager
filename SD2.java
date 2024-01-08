import org.json.JSONArray;
import org.json.JSONObject;

import cos.CustomOutputStream;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class SD2 implements ActionListener {

    protected LinkedList<JSONObject> taskList = new LinkedList<JSONObject>();
    protected Deque<JSONObject> foundTasks = new LinkedList<>();

    public SD2() {
        readFromFile(taskList);
        createAndShowGUI();
    }
    
    public static void main(String[] args) {
        try { // change UI look
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        //start the program
        new SD2();
    }

    private void createAndShowGUI() {
        // create app window
        JFrame frame = new JFrame();

        // create a label to display text
        JLabel textBoxLabel = new JLabel("* Main Task List *");
        textBoxLabel.setForeground(Color.BLUE);
        textBoxLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(textBoxLabel, BorderLayout.NORTH);

        // create a panel to hold buttons
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        panel.setLayout(new GridLayout(5, 1));

        // REDIRECT OUTPUT TO WINDOW
        JTextArea textArea = new JTextArea(15, 1);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setFont(new Font("Arial Unicode MS", Font.PLAIN, 12));
        CustomOutputStream customOutStream = new CustomOutputStream(textArea);
        System.setOut(customOutStream.getPrintStream());
        System.setErr(customOutStream.getPrintStream());
        
        // BUTTON TO CHOOSE WHAT LIST TO VIEW
        JPanel listSelectionPanel = new JPanel();
        listSelection(textArea, textBoxLabel, listSelectionPanel);

        // CREATE BUTTON TO DELETE EITHER LIST
        JPanel listDeletionPanel = new JPanel();
        listDeletion(textArea, listDeletionPanel, textBoxLabel);

        // PANEL SETUP
        JPanel northPanel = new JPanel(new GridLayout(2, 1));
        northPanel.add(listSelectionPanel);
        northPanel.add(listDeletionPanel);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(northPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.add(mainPanel);
        outputList(taskList, textBoxLabel);

        // CREATE BUTTON TO ADD TASK
        addTask(textArea, panel, textBoxLabel);

        // CREATE DELETE TASK BUTTON
        deleteTask(textArea, panel, textBoxLabel);

        // CREATE A BUTTON TO FIND TASK
        findTask(textArea, panel, textBoxLabel);

        // CREATE A BUTTON TO SORT TASKS
        sortTasks(textArea, panel, textBoxLabel);

        // CREATE A BUTTON TO CHANGE TASK
        changeTask(textArea, panel, textBoxLabel);

        // add panel to frame
        frame.add(panel, BorderLayout.WEST);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() { // when app is closed, write taskList to file
            @Override
            public void windowClosing(WindowEvent e) {
                writeToFile(taskList);
                System.exit(0);
            }
        });
        frame.setResizable(false);
        frame.setTitle("Task Manager");
        frame.setSize(500, 700);
        frame.setVisible(true);
    }
    private void addTask(JTextArea textArea, JPanel panel, JLabel textBoxLabel) {
        // create a button to add task
        JButton addTaskButton = new JButton("Add Task");
        addTaskButton.addActionListener(e -> {
            JTextField positionField = new JTextField(),
                    nameField = new JTextField(),
                    descriptionField = new JTextField(),
                    deadlineField = new JTextField(),
                    subjectField = new JTextField();

            // set default values for date field
            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = today.format(formatter);

            // set default values for date field
            deadlineField.setText(formattedDate);

            // create error labels
            JLabel positionErrorLabel = new JLabel();
            positionErrorLabel.setForeground(Color.RED);
            JLabel nameErrorLabel = new JLabel();
            nameErrorLabel.setForeground(Color.RED);
            JLabel descErrorLabel = new JLabel();
            descErrorLabel.setForeground(Color.RED);
            JLabel dlErrorLabel = new JLabel();
            dlErrorLabel.setForeground(Color.RED);
            JLabel subjErrorLabel = new JLabel();
            subjErrorLabel.setForeground(Color.RED);

            // if list is empty, set position field to 1 and disable editing
            if(taskList.isEmpty()) {
                positionField.setEditable(false);
                positionField.setText("1");
            }

            // create draft for dialog box
            Object[] fields = {
                    "Position:", positionField,
                    positionErrorLabel,
                    "Name:", nameField,
                    nameErrorLabel,
                    "Subject:", subjectField,
                    subjErrorLabel,
                    "Deadline:", deadlineField,
                    dlErrorLabel,
                    "Description:", descriptionField,
                    descErrorLabel,
            };

            // create dialog box
            JOptionPane optionPane = new JOptionPane(fields, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
            JDialog dialog = optionPane.createDialog("Add Task");
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            // loop until user clicks cancel, X or enters valid input
            while (true) {
                dialog.pack(); // resize dialog box
                dialog.setVisible(true); // make dialog box visible

                if (optionPane.getValue() == null || JOptionPane.OK_OPTION != (int) optionPane.getValue()) {
                    break;
                }

                try {
                    // create variables from each entered field
                    int position = Integer.parseInt(positionField.getText());
                    String name = nameField.getText();
                    String description = descriptionField.getText();
                    String deadline = deadlineField.getText();
                    String subject = subjectField.getText();

                    // check if position is valid
                    if (position < 1 || position > (taskList.size()+1) && !taskList.isEmpty()) {
                        positionErrorLabel.setText("Invalid position. Please enter a number between 1 and " + (taskList.size()+1) + ".");
                        continue;
                    }
                    else {
                        positionErrorLabel.setText("");
                    } // check if name is valid
                    if (name == null || name.trim().isEmpty()) {
                        nameErrorLabel.setText("Name cannot be empty.");
                        continue;
                    }
                    else {
                        nameErrorLabel.setText("");
                    } // check if description is valid
                    if (description == null || description.trim().isEmpty()) {
                        descErrorLabel.setText("Description cannot be empty.");
                        continue;
                    } 
                    else {
                        descErrorLabel.setText("");
                    } // check if deadline is valid
                    if (deadline == null || deadline.trim().isEmpty()) {
                        dlErrorLabel.setText("Deadline cannot be empty.");
                        continue;
                    }
                    else {
                        dlErrorLabel.setText("");
                    }

                    try {
                        LocalDate deadlineDate = LocalDate.parse(deadline, formatter);
                        if (deadlineDate.isBefore(today)) {
                            dlErrorLabel.setText("Invalid deadline. Deadline cannot be older than today.");
                            continue;
                        }
                    } catch (DateTimeParseException ex1) {
                        dlErrorLabel.setText("Invalid deadline. Please enter a date in the format yyyy-MM-dd.");
                        continue;
                    }
                    
                    if (subject == null || subject.trim().isEmpty()) {
                        subjErrorLabel.setText("Subject cannot be empty.");
                        continue;
                    }

                    addTask(position - 1, taskList, name, description, deadline, subject);
                    textArea.setText("");
                    outputList(taskList, textBoxLabel);
                    break;
                } catch (NumberFormatException ex2) { // if NumberFormException is thrown, display error message
                positionErrorLabel.setText("Invalid position. Please enter a number.");
                }
            }
        });
        panel.add(addTaskButton);
    }
    private void deleteTask(JTextArea textArea, JPanel panel, JLabel textBoxLabel) {
        // create a button to delete task
        JButton deleteTaskButton = new JButton("Delete Task");
        deleteTaskButton.addActionListener(e -> {
            // if list is empty, display error message
            if (taskList.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Error: List is empty. Cannot delete item.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            else {
                // create array with options for dialog box
                String[] options = {"Delete by Position", "Delete by Name"};

                // create selection dialog box
                int selection = JOptionPane.showOptionDialog(null, "Select the way to delete a task:", "Delete Task",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                
                if (selection == 0) { //delete by position
                    JTextField positionField = new JTextField();

                    JLabel positionErrorLabel = new JLabel();
                    positionErrorLabel.setForeground(Color.RED);

                    Object[] fields = {
                        "Position:", positionField,
                        positionErrorLabel
                    };

                    JOptionPane optionPane = new JOptionPane(fields, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
                    JDialog dialog = optionPane.createDialog("Delete Task by Position");
                    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

                    while (true) {
                        dialog.pack();
                        dialog.setVisible(true);

                        if (optionPane.getValue() == null || JOptionPane.OK_OPTION != (int) optionPane.getValue()) {
                            break;
                        }

                        try {
                            int position = Integer.parseInt(positionField.getText());

                            if (position < 1 || position > taskList.size()) {
                                positionErrorLabel.setText("Invalid position. Please enter a number between 1 and " + taskList.size() + ".");
                                continue;
                            }
                            else {
                                positionErrorLabel.setText("");
                            }

                            deleteTaskByPosition(taskList, position - 1);
                            textArea.setText("");
                            outputList(taskList, textBoxLabel);
                            break;
                        } catch (NumberFormatException ex2) {
                            positionErrorLabel.setText("Invalid position. Please enter a number.");
                        }
                    }
                } else if (selection == 1) { // delete by name
                    Map<String, List<JSONObject>> tasksByName = taskList.stream().collect(Collectors.groupingBy(task -> task.getString("name")));

                    String[] taskNames = tasksByName.keySet().toArray(new String[tasksByName.size()]);

                    JComboBox<String> taskNameDropList = new JComboBox<>(taskNames);
                    int result1 = JOptionPane.showConfirmDialog(null, taskNameDropList, "Delete by Name", JOptionPane.OK_CANCEL_OPTION);
                    
                    if(result1 == JOptionPane.OK_OPTION){
                        String selectedTaskName = (String) taskNameDropList.getSelectedItem();

                        List<JSONObject> tasksWithSelectedName = tasksByName.get(selectedTaskName);
                        if (tasksWithSelectedName != null) {
                            if (tasksWithSelectedName.size() > 1) {
                                String[] positions = new String[tasksWithSelectedName.size()];
                                int selectedCount = 0;
                                for(int i = 0; i <taskList.size(); i++) {
                                    if(taskList.get(i).getString("name").equals(selectedTaskName)) {
                                        int pos = i + 1;
                                        positions[selectedCount] = String.valueOf(pos);
                                        selectedCount++;
                                    }
                                }

                                JComboBox<String> positionsOfSelectedNames = new JComboBox<>(positions);
                                int result2 = JOptionPane.showConfirmDialog(null, positionsOfSelectedNames, "Select Position:", JOptionPane.OK_CANCEL_OPTION);
                                
                                if(result2 == JOptionPane.OK_OPTION) {
                                    String selectedPosition = (String) positionsOfSelectedNames.getSelectedItem();
                            
                                    if (selectedPosition != null && !selectedPosition.isEmpty()) {
                                        int positionInt = Integer.parseInt(selectedPosition);
                                        deleteTaskByPosition(taskList, positionInt - 1);
                                        textArea.setText("");
                                        outputList(taskList, textBoxLabel);
                                    }
                                }
                            } else {
                                deleteTaskByName(taskList, selectedTaskName);
                                textArea.setText("");
                                outputList(taskList, textBoxLabel);
                            }
                        }
                    }
                }
            }
        });
        panel.add(deleteTaskButton);
    }
    private void sortTasks(JTextArea textArea, JPanel panel, JLabel textBoxLabel) {
        // create a button to sort tasks
        JButton sortTasksButton = new JButton("Sort Main Tasks");
        sortTasksButton.addActionListener(e -> {
            if (taskList.isEmpty()) { // if list is empty, display error message
            JOptionPane.showMessageDialog(null, "Error: List is empty. Cannot sort list.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            else { // sort list
                sortList(taskList);
                textArea.setText("");
                outputList(taskList, textBoxLabel);
            }
        });

        panel.add(sortTasksButton);
    }
    private void findTask(JTextArea textArea, JPanel panel, JLabel textBoxLabel) {
        // create a button to find task
        JButton findTaskButton = new JButton("Find Task");
        findTaskButton.addActionListener(e -> {
            if (taskList.isEmpty()) { // if list is empty, display error message
            JOptionPane.showMessageDialog(null, "Error: List is empty. Cannot find anything.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            else {
                // create array with options for dialog box
                String[] options = {"Find by Subject", "Find by Deadline"};

                // create selection dialog box
                int selection = JOptionPane.showOptionDialog(null, "Select the way to delete a task:", "Delete Task",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

                if (selection == 0) { // sort by subject
                    // create a set of all subjects,
                    // set is needed to remove duplicates
                    Set<String> taskSubjectsSet = taskList.stream().map(deadline -> deadline.getString("subject")).collect(Collectors.toSet());
                    // convert set to string array
                    String[] taskSubjects = taskSubjectsSet.toArray(new String[taskSubjectsSet.size()]);
                    Arrays.sort(taskSubjects); // sort array alphabetically
                    
                    // create drop down list with subjects
                    JComboBox<String> taskSubjectDropList = new JComboBox<>(taskSubjects);
                    int result = JOptionPane.showConfirmDialog(null, taskSubjectDropList, "Find by Subject", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    
                    // if user clicks OK, find tasks by subject
                    if(result == JOptionPane.OK_OPTION) {
                        String selectedTaskName = (String) taskSubjectDropList.getSelectedItem();
                        textBoxLabel.setText("* Found Task List *");
                        textBoxLabel.setForeground(Color.magenta);
                        findTasksBySubject(taskList, foundTasks, selectedTaskName);
                        textArea.setText("");
                        outputFoundList(foundTasks, textBoxLabel);
                    }
                    //otherwise, exit without changes

                    } else if (selection == 1) { // sort by deadline
                        // create a set of all deadlines,
                        // set is needed to remove duplicates
                        Set<String> taskDeadlinesSet = taskList.stream().map(deadline -> deadline.getString("deadline")).collect(Collectors.toSet());
                        String[] taskDeadlines = taskDeadlinesSet.toArray(new String[taskDeadlinesSet.size()]);
                        Arrays.sort(taskDeadlines); //sort array by date
                        
                        // create drop down list with deadlines
                        JComboBox<String> taskDeadlineDropList = new JComboBox<>(taskDeadlines);
                        int result = JOptionPane.showConfirmDialog(null, taskDeadlineDropList, "Find by Deadline", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                        if(result == JOptionPane.OK_OPTION) {
                            String selectedTaskName = (String) taskDeadlineDropList.getSelectedItem();
                            
                            textBoxLabel.setText("* Found Task List *");
                            textBoxLabel.setForeground(Color.magenta);
                            findTasksByDeadline(taskList, foundTasks, selectedTaskName);
                            textArea.setText("");
                            outputFoundList(foundTasks, textBoxLabel);
                        }
                    }
            }
        });
        panel.add(findTaskButton);
    }
    private void changeTask(JTextArea textArea, JPanel panel, JLabel textBoxLabel) {
        // create a button to change task
        JButton changeTaskButton = new JButton("Change Task");
        changeTaskButton.addActionListener(e -> {
            if (taskList.isEmpty()) { // if list is empty, display error message
            JOptionPane.showMessageDialog(null, "Error: List is empty. Cannot change anything.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            else {   
                JTextField positionField = new JTextField();

                JLabel positionErrorLabel = new JLabel();
                positionErrorLabel.setForeground(Color.RED);

                Object[] positionSelector = {
                    "Which position to change?", positionField,
                    positionErrorLabel,
                };

                JOptionPane positionPane = new JOptionPane(positionSelector, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
                JDialog positionDialog = positionPane.createDialog("Change Task");

                while (true) {
                    
                    positionDialog.pack();
                    positionDialog.setVisible(true);
            
                    Object selectedValue = positionPane.getValue(); 
                    // check if user clicked cancel or X
                    if (selectedValue == null || !(selectedValue instanceof Integer) || (Integer) selectedValue != JOptionPane.OK_OPTION) {
                        break;
                    }
                    
                    // parse the task number from string to int
                    int taskNumber;
                    try {
                        taskNumber = Integer.parseInt(positionField.getText());
                    } catch (NumberFormatException ex3) {
                        positionErrorLabel.setText("Invalid task number.");
                        continue;
                    }

                    // check if the task number is valid
                    if (taskNumber < 1 || taskNumber > taskList.size()) {
                        positionErrorLabel.setText("Invalid position. Please enter a number between 1 and " + taskList.size() + ".");
                        continue;
                    }

                    // get the task at the entered position
                    JSONObject taskToChange = taskList.get(taskNumber - 1);

                    JLabel nameErrorLabel = new JLabel();
                    nameErrorLabel.setForeground(Color.RED);
                    JLabel descErrorLabel = new JLabel();
                    descErrorLabel.setForeground(Color.RED);
                    JLabel dlErrorLabel = new JLabel();
                    dlErrorLabel.setForeground(Color.RED);
                    JLabel subjErrorLabel = new JLabel();
                    subjErrorLabel.setForeground(Color.RED);

                    // Create fields pre-filled with the task's information
                    JTextField nameField = new JTextField(taskToChange.getString("name"));
                    JTextField descriptionField = new JTextField(taskToChange.getString("description"));
                    JTextField deadlineField = new JTextField(taskToChange.getString("deadline"));
                    JTextField subjectField = new JTextField(taskToChange.getString("subject"));

                    Object[] changeMessage = {
                        "Name:", nameField,
                        nameErrorLabel,
                        "Subject:", subjectField,
                        subjErrorLabel,
                        "Deadline:", deadlineField,
                        dlErrorLabel,
                        "Description:", descriptionField,
                        descErrorLabel,
                    };

                    JOptionPane changePane = new JOptionPane(changeMessage, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
                    JDialog changeDialog = changePane.createDialog("Change" + positionField + "Task");

                    while (true) {
                    changeDialog.pack();
                    changeDialog.setVisible(true);

                    if (changePane.getValue() == null || JOptionPane.OK_OPTION != (int) changePane.getValue()) {
                        break;
                    }

                    try {
                        String name = nameField.getText();
                        String description = descriptionField.getText();
                        String deadline = deadlineField.getText();
                        String subject = subjectField.getText();

                        if (name == null || name.trim().isEmpty()) {
                            nameErrorLabel.setText("Name cannot be empty.");
                            continue;
                        }
                        else {
                            nameErrorLabel.setText("");
                        }
                        if (description == null || description.trim().isEmpty()) {
                            descErrorLabel.setText("Description cannot be empty.");
                            continue;
                        }
                        else {
                            descErrorLabel.setText("");
                        }
                        if (deadline == null || deadline.trim().isEmpty()) {
                            dlErrorLabel.setText("Deadline cannot be empty.");
                            continue;
                        }
                        else {
                            dlErrorLabel.setText("");
                        }

                        try {
                            LocalDate today = LocalDate.now();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                            LocalDate deadlineDate = LocalDate.parse(deadline, formatter);
                            if (deadlineDate.isBefore(today)) {
                                dlErrorLabel.setText("Invalid deadline. Deadline cannot be older than today.");
                                continue;
                            }
                        } catch (DateTimeParseException ex1) {
                            dlErrorLabel.setText("Invalid deadline. Please enter a date in the format yyyy-MM-dd.");
                            continue;
                        }
                        
                        if (subject == null || subject.trim().isEmpty()) {
                            subjErrorLabel.setText("Subject cannot be empty.");
                            continue;
                        }
                    } catch (NumberFormatException ex2) {
                    positionErrorLabel.setText("Invalid position. Please enter a number.");
                    }

                    break;
                }

                    if (selectedValue != null && selectedValue instanceof Integer && (Integer) selectedValue == JOptionPane.OK_OPTION) {
                        // Update the task with the edited information
                        taskToChange.put("name", nameField.getText());
                        taskToChange.put("description", descriptionField.getText());
                        taskToChange.put("deadline", deadlineField.getText());
                        taskToChange.put("subject", subjectField.getText());
                    }

                    textArea.setText("");
                    outputList(taskList, textBoxLabel);
                    break;
                }
            }
        });
        panel.add(changeTaskButton);
    }
    private void listSelection(JTextArea textArea, JLabel textBoxLabel, JPanel listSelectionPanel) {
        JButton viewMainListButton = new JButton("View Main List");
        JButton viewFoundListButton = new JButton("View Found List");
        viewMainListButton.addActionListener(e -> {
            textBoxLabel.setText("* Main Task List *");
            textBoxLabel.setForeground(Color.BLUE);
            textArea.setText("");
            outputList(taskList, textBoxLabel);
        });
        viewFoundListButton.addActionListener(e -> {
            textBoxLabel.setText("* Found Task List *");
            textBoxLabel.setForeground(Color.magenta);
            textArea.setText("");
            outputFoundList(foundTasks, textBoxLabel);
        });
        listSelectionPanel.add(viewMainListButton, BorderLayout.NORTH);
        listSelectionPanel.add(viewFoundListButton, BorderLayout.NORTH);
    }
    private void listDeletion(JTextArea textArea, JPanel listDeletionPanel, JLabel textBoxLabel) {
        JButton deleteMainListButton = new JButton("Delete Main List");
        JButton deleteFoundListButton = new JButton("Delete Found List");
        deleteMainListButton.addActionListener(e -> {
            deleteMainList(taskList);
            textArea.setText("");
            outputList(taskList, textBoxLabel);
        });
        deleteFoundListButton.addActionListener(e -> {
            deleteFoundList(foundTasks);
            textArea.setText("");
            outputFoundList(foundTasks, textBoxLabel);
        });
        listDeletionPanel.add(deleteMainListButton, BorderLayout.NORTH);
        listDeletionPanel.add(deleteFoundListButton, BorderLayout.NORTH);
    }

    public static void readFromFile(LinkedList<JSONObject> taskList)
    {
        String path = "task_file.json";
        try {
            String contents = Files.readString(Paths.get(path), StandardCharsets.UTF_8);
            JSONObject object = new JSONObject(contents);
            JSONArray tasks = object.getJSONArray("tasks");

            for (int i = 0; i < tasks.length(); i++) {
                JSONObject task = tasks.getJSONObject(i);
                if (task.has("name") && task.has("subject") && task.has("deadline") && task.has("description")) {
                    taskList.add(task);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }   
    }
    public static void outputList(LinkedList<JSONObject> taskList, JLabel textBoxLabel) {
        int count = 0;
        textBoxLabel.setText("* Main Task List *");
        textBoxLabel.setForeground(Color.BLUE);

        if (taskList.isEmpty()) {
            System.out.println("Task list is empty.");
        }
        else {
            for (JSONObject task : taskList) {
                count++;
                String name = task.has("name") ? task.getString("name") : "N/A";
                String subject = task.has("subject") ? task.getString("subject") : "N/A";
                String deadline = task.has("deadline") ? task.getString("deadline") : "N/A";
                String description = task.has("description") ? task.getString("description") : "N/A";

                System.out.println("Task " + count + "-------------------------------------------------------");
                System.out.println("Task Name: " + name);
                System.out.println("Subject: " + subject);
                System.out.println("Deadline: " + deadline);
                System.out.println("Description: " + description);
                System.out.println("");
            }
        }
    }
    public static void outputFoundList(Deque<JSONObject> foundTasks, JLabel textBoxLabel) {
        textBoxLabel.setText("* Found Task List *");
        textBoxLabel.setForeground(Color.MAGENTA);

        if (foundTasks.isEmpty()) {
            System.out.println("Found task list is empty.");
        }
        else {
                for (JSONObject task : foundTasks) {
                String name = task.getString("name");
                String subject = task.getString("subject");
                String deadline = task.getString("deadline");
                String description = task.getString("description");

                System.out.println("Task Name: " + name);
                System.out.println("Subject: " + subject);
                System.out.println("Deadline: " + deadline);
                System.out.println("Description: " + description);
                System.out.println("-------------------------------------------------------");
            }
        }
    }
    public static void writeToFile(LinkedList<JSONObject> taskList) {
        JSONObject mainObject = new JSONObject();
        mainObject.put("tasks", new JSONArray(taskList));
        
        try (FileWriter file = new FileWriter("task_file.json")) {
            file.write(mainObject.toString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addTask(int position, LinkedList<JSONObject> taskList, String name, String description, String deadline, String subject)
    {
        JSONObject newTask = new JSONObject();
        newTask.put("name", name);
        newTask.put("description", description);
        newTask.put("deadline", deadline);
        newTask.put("subject", subject);

        if(position == taskList.size()) {
            taskList.addLast(newTask);
        }
        else{
            taskList.add(position, newTask);
        }
    }
    public static void deleteTaskByPosition(LinkedList<JSONObject> taskList, int position) {
        if (position < 0 || position >= taskList.size()) {
          throw new IllegalArgumentException("Invalid position: " + position);
        }
      
        taskList.remove(position);
    }
    public static void deleteTaskByName(LinkedList<JSONObject> taskList, String name) {
        for (int i = 0; i < taskList.size(); i++) {
            JSONObject task = taskList.get(i);
            if (task.getString("name").equals(name)) {
                
                taskList.remove(i);
                break;
            }
        }
    }
    public static void findTasksByDeadline(LinkedList<JSONObject> taskList, Deque<JSONObject> foundTasks, String deadline) {
        for (JSONObject task : taskList) {
            if (task.getString("deadline").equals(deadline)) {
                foundTasks.add(task);
            }
        }
    }
    public static void findTasksBySubject(LinkedList<JSONObject> taskList, Deque<JSONObject> foundTasks, String subject) {
        for (JSONObject task : taskList) {
            if (task.getString("subject").equals(subject)) {
                foundTasks.add(task);
            }
        }
    }
    public static void sortList(LinkedList<JSONObject> taskList) {
        Comparator<JSONObject> deadlineComparator = new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject o1, JSONObject o2) {
                return o1.getString("deadline").compareTo(o2.getString("deadline"));
            }
        };

        Collections.sort(taskList, deadlineComparator);
    }
    public static void changeTask(LinkedList<JSONObject> taskList, int position, String name, String description, String deadline, String subject) {
        JSONObject task = taskList.get(position);
        if (name != null) {
            task.put("name", name);
        }
        if (description != null) {
            task.put("description", description);
        }
        if (deadline != null) {
            task.put("deadline", deadline);
        }
        if (subject != null) {
            task.put("subject", subject);
        }
    }
    public static void deleteMainList(LinkedList<JSONObject> taskList) {
        taskList.clear();
    }
    public static void deleteFoundList(Deque<JSONObject> foundTasks) {
        foundTasks.clear();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Unimplemented method 'actionPerformed'");
    }
}
