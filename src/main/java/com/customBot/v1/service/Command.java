package com.customBot.v1.service;

public enum Command {
  START_COMMAND("/start"),
  GET_ALL_PRODUCTS_LIST_COMMAND("/products"),
  GET_TOP5_EXPENSIVE_PRODUCTS_COMMAND("/top5"),
  GET_CHECK_FOR_VIEW_COMMAND("/check"),
  ADD_CHECK_COMMAND("/addCheck"),
  GET_CHECK_FOR_DELETE_COMMAND("/delete1"),
  DELETE_NOTE_COMMAND("/delete2"),
  GET_NOTE_COMMAND("/note"),
  ADD_NOTE_COMMAND("/addNote");

  private String commandText;

  private Command(String commandText) {
    this.commandText = commandText;
  }

  public static Command getCommandByTextCommand(String command) {
    for (Command currentCommand : values()) {
      if(currentCommand.commandText.equals(command)) {
        return currentCommand;
      }
    }
    return START_COMMAND;
  }
}
