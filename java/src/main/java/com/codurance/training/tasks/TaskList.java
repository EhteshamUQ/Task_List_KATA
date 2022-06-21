package com.codurance.training.tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TaskList implements Runnable {
    private static final String QUIT = "quit";

    private final Map<String, List<Task>> tasks = new LinkedHashMap<>();
    private final BufferedReader in;
    private final PrintWriter out;

    private long lastId = 0;

    public static void main(String[] args) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);
        new TaskList(in, out).run();
    }

    public TaskList(BufferedReader reader, PrintWriter writer) {
        this.in = reader;
        this.out = writer;
    }

    public void run() {
        while (true) {
            out.print("> ");
            out.flush();
            String command;
            try {
                command = in.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (command.equals(QUIT)) {
                break;
            }
            execute(command);
        }
    }

    private void execute(String commandLine) {
        String[] commandRest = commandLine.split(" ", 2);
        String command = commandRest[0];
        switch (command) {
            case "add" -> add(commandRest[1]);
            case "check" -> check(commandRest[1]);
            case "uncheck" -> uncheck(commandRest[1]);
            case "help" -> help();
            case "deadline" -> addDeadline(commandRest[1]);
            case "today" -> today();
            case "delete" -> delete(commandRest[1]);
            case "view" -> view(commandRest[1]);
            default -> error(command);
        }
    }

    private void show() {
        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            out.println(project.getKey());
            for (Task task : project.getValue()) {
                out.printf("    [%c] %s: %s%n", (Boolean.TRUE.equals(task.isDone()) ? 'x' : ' '), task.getId(), task.getDescription());
            }
            out.println();
        }
    }

    private void view(String input){
        try{
        String formatColumn = input.split(" ")[1];
            switch (formatColumn) {
                case "date" -> viewByDate();
                case "deadline" -> viewByDeadline();
                case "project" -> show();
                default -> out.println("Please enter the correct Syntax");
            }
    }catch (ArrayIndexOutOfBoundsException exception){
            out.println("Please Enter the Correct Syntax");
        }
    }

    private void viewByDate(){
        List<Task> allTasks = new ArrayList<>();
        for(Map.Entry<String , List<Task>> entry : tasks.entrySet()){
            allTasks.addAll(entry.getValue());
        }
       allTasks =  allTasks.stream().sorted(Comparator.comparing(Task::getCreatedOn)).toList();
        for(Task task : allTasks){
            out.printf("    [%c] %s: %s%n", (Boolean.TRUE.equals(task.isDone()) ? 'x' : ' '), task.getId(), task.getDescription());
        }
    out.println();
    }

    private void viewByDeadline(){
        List<Task> allTasks = new ArrayList<>();
        for(Map.Entry<String , List<Task>> entry : tasks.entrySet()){
            allTasks.addAll(entry.getValue());
        }
        allTasks =  allTasks.stream().sorted(Comparator.comparing(Task::getDeadline , Comparator.nullsFirst(Comparator.naturalOrder()))).toList();
        for(Task task : allTasks){
            out.printf("    [%c] %s: %s%n", (Boolean.TRUE.equals(task.isDone()) ? 'x' : ' '), task.getId(), task.getDescription());
        }
        out.println();
    }

    private void add(String commandLine) {
        String[] subcommandRest = commandLine.split(" ", 2);
        String subcommand = subcommandRest[0];
        if (subcommand.equals("project")) {
            addProject(subcommandRest[1]);
        } else if (subcommand.equals("task")) {
            String[] projectTask = subcommandRest[1].split(" ",2);
            addTask(projectTask[0], projectTask[1]);
        }
    }

    private void addProject(String name) {
        tasks.put(name, new ArrayList<>());
    }

    private void addTask(String project, String inputs) {
        List<Task> projectTasks = tasks.get(project);
        if (projectTasks == null) {
            out.printf("Could not find a project with the name \"%s\".", project);
            out.println();
            return;
        }
        String[] values = inputs.split(" ", 2);
        if (values.length == 1)
            projectTasks.add(new Task(String.valueOf(nextId()),inputs, false));
        else{
            String id = values[0];
            String description = values[1];
            if(Boolean.TRUE.equals(isValidId(id)))
                projectTasks.add(new Task(id , description , false ));
            else
                out.println("Please Enter a Valid ID");
        }
    }

    private Boolean isValidId(String id){
        Pattern p = Pattern.compile("[^a-z0-9]" , Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(id);
        return !m.find();
    }

    private void addDeadline(String commandInputs)  {
        String[] inputs = commandInputs.split(" ");
        String id = inputs[0];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyy");
        LocalDate deadline ;
        try {
            deadline = LocalDate.parse(inputs[1] , formatter);
        } catch (DateTimeParseException e) {
            out.println("Please enter a Valid Date");
            return;
        }
        for(Map.Entry<String , List<Task>> entry : tasks.entrySet()){
          Optional<Task> optionalTask =  entry.getValue().stream().filter(task -> task.getId().equalsIgnoreCase(id)).findFirst();
            optionalTask.ifPresent(task -> task.setDeadline(deadline));
        }

    }

    private void check(String idString) {
        setDone(idString, true);
    }

    private void uncheck(String idString) {
        setDone(idString, false);
    }

    private void setDone(String id, Boolean done) {
        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            for (Task task : project.getValue()) {
                if (task.getId().equalsIgnoreCase(id)) {
                    task.setDone(done);
                    return;
                }
            }
        }
        out.printf("Could not find a task with an ID of %s.", id);
        out.println();
    }

    private void help() {
        out.println("Commands:");
        out.println("  show");
        out.println("  add project <project name>");
        out.println("  add task <project name> <task description>");
        out.println("  check <task ID>");
        out.println("  uncheck <task ID>");
        out.println();
    }

    private void error(String command) {
        out.printf("I don't know what the command \"%s\" is.", command);
        out.println();
    }

    private long nextId() {
        return ++lastId;
    }

    private void delete(String id){
        for(Map.Entry<String , List<Task>> entry : tasks.entrySet()){
            entry.getValue().removeIf(task -> task.getId().equalsIgnoreCase(id));
        }
    }

    private void today() {
        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            for (Task task : project.getValue()) {
                if(task.getDeadline() != null && LocalDate.now().compareTo(task.getDeadline()) == 0) {
                    out.printf("    [%c] %s: %s%n", (Boolean.TRUE.equals(task.isDone()) ? 'x' : ' '), task.getId(), task.getDescription());
                }
            }
            out.println();
        }
    }


}
