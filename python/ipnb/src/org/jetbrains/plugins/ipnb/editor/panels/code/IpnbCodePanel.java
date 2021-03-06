package org.jetbrains.plugins.ipnb.editor.panels.code;

import com.google.common.collect.Lists;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ipnb.configuration.IpnbConnectionManager;
import org.jetbrains.plugins.ipnb.editor.IpnbEditorUtil;
import org.jetbrains.plugins.ipnb.editor.IpnbFileEditor;
import org.jetbrains.plugins.ipnb.editor.panels.IpnbEditablePanel;
import org.jetbrains.plugins.ipnb.editor.panels.IpnbPanel;
import org.jetbrains.plugins.ipnb.format.cells.IpnbCodeCell;
import org.jetbrains.plugins.ipnb.format.cells.output.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class IpnbCodePanel extends IpnbEditablePanel<JComponent, IpnbCodeCell> {
  private final Project myProject;
  private final Disposable myParent;
  private IpnbCodeSourcePanel myCodeSourcePanel;
  private final List<IpnbPanel> myOutputPanels = Lists.newArrayList();

  public IpnbCodePanel(@NotNull final Project project, @Nullable final Disposable parent, @NotNull final IpnbCodeCell cell) {
    super(cell, new BorderLayout());
    myProject = project;
    myParent = parent;

    myViewPanel = createViewPanel();
    add(myViewPanel);
  }

  public IpnbFileEditor getFileEditor() {
    assert myParent instanceof IpnbFileEditor;
    return (IpnbFileEditor)myParent;
  }

  public Editor getEditor() {
    return myCodeSourcePanel.getEditor();
  }

  public void addPromptPanel(@NotNull final JComponent parent, Integer promptNumber,
                             @NotNull final IpnbEditorUtil.PromptType promptType,
                             @NotNull final IpnbPanel component, @NotNull final GridBagConstraints c) {
    c.gridx = 0;
    c.weightx = 0;
    c.anchor = GridBagConstraints.NORTHWEST;
    final JComponent promptComponent = IpnbEditorUtil.createPromptComponent(promptNumber, promptType);
    c.insets = new Insets(2,2,2,5);
    parent.add(promptComponent, c);

    c.gridx = 1;
    c.weightx = 1;
    c.insets = new Insets(2,2,2,2);
    c.anchor = GridBagConstraints.CENTER;
    parent.add(component, c);
    myOutputPanels.add(component);
  }

  @Override
  protected JComponent createViewPanel() {
    final JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(IpnbEditorUtil.getBackground());

    final GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 1;

    myCodeSourcePanel = new IpnbCodeSourcePanel(myProject, this, myCell);
    if (myParent != null)
      Disposer.register(myParent, new Disposable() {
      @Override
      public void dispose() {
        EditorFactory.getInstance().releaseEditor(myCodeSourcePanel.getEditor());
      }
    });
    addPromptPanel(panel, myCell.getPromptNumber(), IpnbEditorUtil.PromptType.In, myCodeSourcePanel, c);

    c.gridx = 1;

    c.gridy = 0;
    for (IpnbOutputCell outputCell : myCell.getCellOutputs()) {
      c.gridy++;
      addOutputPanel(panel, c, outputCell, true);
    }
    return panel;
  }

  private void addOutputPanel(@NotNull final JComponent panel, @NotNull final GridBagConstraints c,
                              @NotNull final IpnbOutputCell outputCell, boolean addPrompt) {
    final IpnbEditorUtil.PromptType promptType = addPrompt ? IpnbEditorUtil.PromptType.Out : IpnbEditorUtil.PromptType.None;
    if (outputCell instanceof IpnbImageOutputCell) {
      addPromptPanel(panel, myCell.getPromptNumber(), promptType,
                     new IpnbImagePanel((IpnbImageOutputCell)outputCell), c);
    }
    else if (outputCell instanceof IpnbHtmlOutputCell) {
      addPromptPanel(panel, myCell.getPromptNumber(), promptType,
                     new IpnbHtmlPanel((IpnbHtmlOutputCell)outputCell), c);
    }
    else if (outputCell instanceof IpnbLatexOutputCell) {
      addPromptPanel(panel, myCell.getPromptNumber(), promptType,
                     new IpnbLatexPanel((IpnbLatexOutputCell)outputCell), c);
    }
    else if (outputCell instanceof IpnbErrorOutputCell) {
      addPromptPanel(panel, myCell.getPromptNumber(), promptType,
                     new IpnbErrorPanel((IpnbErrorOutputCell)outputCell), c);
    }
    else if (outputCell instanceof IpnbStreamOutputCell) {
      addPromptPanel(panel, myCell.getPromptNumber(), IpnbEditorUtil.PromptType.None,
                     new IpnbStreamPanel((IpnbStreamOutputCell)outputCell), c);
    }
    else if (outputCell.getSourceAsString() != null) {
      addPromptPanel(panel, myCell.getPromptNumber(), promptType,
                     new IpnbCodeOutputPanel<IpnbOutputCell>(outputCell), c);
    }
  }

  @Override
  public void switchToEditing() {
    setEditing(true);
    getParent().repaint();
    UIUtil.requestFocus(myCodeSourcePanel.getEditor().getContentComponent());
  }

  @Override
  public void runCell() {
    super.runCell();
    updateCellSource();
    myCell.setPromptNumber(-1);
    updatePanel(myCell.getCellOutputs());
    final IpnbConnectionManager connectionManager = IpnbConnectionManager.getInstance(myProject);
    connectionManager.executeCell(this);
    setEditing(false);
  }

  @Override
  public boolean isModified() {
    return true;
  }

  @Override
  public void updateCellSource() {
    final Document document = myCodeSourcePanel.getEditor().getDocument();
    final String text = document.getText();
    myCell.setSource(StringUtil.splitByLinesKeepSeparators(text));
  }

  public void updatePanel(@NotNull final List<IpnbOutputCell> outputContent) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        myCell.removeCellOutputs();

        myViewPanel.removeAll();
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;

        addPromptPanel(myViewPanel, myCell.getPromptNumber(), IpnbEditorUtil.PromptType.In, myCodeSourcePanel, c);

        for (IpnbOutputCell output : outputContent) {
          myCell.addCellOutput(output);
          c.gridx = 0;
          c.gridy += 1;

          addOutputPanel(myViewPanel, c, output, output instanceof IpnbOutOutputCell);
        }
        revalidate();
        repaint();
      }
    });
  }

  @SuppressWarnings({"CloneDoesntCallSuperClone", "CloneDoesntDeclareCloneNotSupportedException"})
  @Override
  protected Object clone() {
    return new IpnbCodePanel(myProject, myParent, (IpnbCodeCell)myCell.clone());
  }
}
