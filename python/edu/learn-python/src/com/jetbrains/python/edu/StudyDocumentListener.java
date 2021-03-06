package com.jetbrains.python.edu;


import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.impl.event.DocumentEventImpl;
import com.jetbrains.python.edu.course.TaskFile;
import com.jetbrains.python.edu.course.TaskWindow;

/**
 * Listens changes in study files and updates
 * coordinates of all the windows in current task file
 */
public class StudyDocumentListener extends DocumentAdapter {
  private final TaskFile myTaskFile;
  private int myOldLine;
  private int myOldLineStartOffset;
  private TaskWindow myTaskWindow;

  public StudyDocumentListener(TaskFile taskFile) {
    myTaskFile = taskFile;
  }


  //remembering old end before document change because of problems
  // with fragments containing "\n"
  @Override
  public void beforeDocumentChange(DocumentEvent e) {
    int offset = e.getOffset();
    int oldEnd = offset + e.getOldLength();
    Document document = e.getDocument();
    myOldLine = document.getLineNumber(oldEnd);
    myOldLineStartOffset = document.getLineStartOffset(myOldLine);
    int line = document.getLineNumber(offset);
    int offsetInLine = offset - document.getLineStartOffset(line);
    LogicalPosition pos = new LogicalPosition(line, offsetInLine);
    myTaskWindow = myTaskFile.getTaskWindow(document, pos);
  }

  @Override
  public void documentChanged(DocumentEvent e) {
    if (e instanceof DocumentEventImpl) {
      DocumentEventImpl event = (DocumentEventImpl)e;
      Document document = e.getDocument();
      int offset = e.getOffset();
      int change = event.getNewLength() - event.getOldLength();
      if (myTaskWindow != null) {
        int newLength = myTaskWindow.getLength() + change;
        myTaskWindow.setLength(newLength <= 0 ? 0 : newLength);
      }
      int newEnd = offset + event.getNewLength();
      int newLine = document.getLineNumber(newEnd);
      int lineChange = newLine - myOldLine;
      myTaskFile.incrementLines(myOldLine + 1, lineChange);
      int newEndOffsetInLine = offset + e.getNewLength() - document.getLineStartOffset(newLine);
      int oldEndOffsetInLine = offset + e.getOldLength() - myOldLineStartOffset;
      myTaskFile.updateLine(lineChange, myOldLine, newEndOffsetInLine, oldEndOffsetInLine);
    }
  }
}
