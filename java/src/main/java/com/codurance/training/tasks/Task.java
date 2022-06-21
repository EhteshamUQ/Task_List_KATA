package com.codurance.training.tasks;

import java.time.LocalDate;

public final class Task {
    private final String id;
    private final String description;
    private boolean done;

    private final LocalDate createdOn;

    private LocalDate deadline;

    public Task(String id, String description, boolean done , LocalDate deadline) {
        this.id = id;
        this.description = description;
        this.done = done;
        this.deadline = deadline;
        this.createdOn = LocalDate.now();
    }

    public Task(String id, String description, boolean done) {
        this.id = id;
        this.description = description;
        this.done = done;
        this.createdOn = LocalDate.now();
    }

    public LocalDate getDeadline(){
        return deadline;
    }

    public LocalDate getCreatedOn(){
        return createdOn;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Boolean isDone() {
        return done;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }
}

// Primitives can be used as:
// local Variables ,Fields and Constructor Params
// Primitives should not be in :
// Method Params ,Return Values.
