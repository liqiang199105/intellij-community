package org.jetbrains.plugins.coursecreator;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.impl.event.DocumentEventImpl;
import org.jetbrains.plugins.coursecreator.format.TaskFile;
import org.jetbrains.plugins.coursecreator.format.TaskWindow;

/**
 * author: liana
 * data: 7/16/14.
 * Listens changes in study files and updates
 * coordinates of all the windows in current task file
 */
public abstract class CCDocumentListener extends DocumentAdapter {
  private final TaskFile myTaskFile;
  private int myOldLine;
  private int myOldLineStartOffset;
  private TaskWindow myTaskWindow;

  public CCDocumentListener(TaskFile taskFile) {
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
          updateTaskWindowLength(e.getNewFragment(), myTaskWindow, change);
        }
        int newEnd = offset + event.getNewLength();
        int newLine = document.getLineNumber(newEnd);
        int lineChange = newLine - myOldLine;
        myTaskFile.incrementLines(myOldLine + 1, lineChange);
        int newEndOffsetInLine = offset + e.getNewLength() - document.getLineStartOffset(newLine);
        int oldEndOffsetInLine = offset + e.getOldLength() - myOldLineStartOffset;
        myTaskFile.updateLine(lineChange, myOldLine, newEndOffsetInLine, oldEndOffsetInLine, useLength());
      }
  }

  protected abstract void updateTaskWindowLength(CharSequence fragment, TaskWindow taskWindow, int change);

  protected abstract  boolean useLength();
}

