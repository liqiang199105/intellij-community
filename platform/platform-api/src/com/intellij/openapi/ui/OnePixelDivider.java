/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.openapi.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.Weighted;
import com.intellij.openapi.wm.IdeGlassPane;
import com.intellij.openapi.wm.IdeGlassPaneUtil;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Konstantin Bulenkov
 */
public class OnePixelDivider extends Divider {
  public static final Color BACKGROUND = new JBColor(0xC5CAD0, 0x494F59);

  private boolean myVertical;
  private Splitter mySplitter;
  private boolean myResizeEnabled;
  private boolean mySwitchOrientationEnabled;
  protected Point myPoint;
  private IdeGlassPane myGlassPane;
  private final MouseAdapter myListener = new MyMouseAdapter();
  private Disposable myDisposable;

  public OnePixelDivider(boolean vertical, Splitter splitter) {
    super(new GridBagLayout());
    mySplitter = splitter;
    myResizeEnabled = true;
    mySwitchOrientationEnabled = false;
    setFocusable(false);
    enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    //setOpaque(false);
    setOrientation(vertical);
    setBackground(BACKGROUND);
  }

  @Override
  public void addNotify() {
    super.addNotify();
    init();
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    if (myDisposable != null && !Disposer.isDisposed(myDisposable)) {
      Disposer.dispose(myDisposable);
    }
  }

  private boolean dragging = false;
  private class MyMouseAdapter extends MouseAdapter implements Weighted {
    @Override
    public void mousePressed(MouseEvent e) {
      dragging = isInDragZone(e);
      _processMouseEvent(e);
    }

    boolean isInDragZone(MouseEvent e) {
      final MouseEvent event = getTargetEvent(e);
      final Point p = event.getPoint();
      final int r = Math.abs(isVertical() ? p.y : p.x);
      return r < 6;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      _processMouseEvent(e);
      dragging = false;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      final OnePixelDivider divider = OnePixelDivider.this;
      if (isInDragZone(e)) {
        myGlassPane.setCursor(divider.getCursor(), divider);
      } else {
        myGlassPane.setCursor(null, divider);
      }
      _processMouseMotionEvent(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
      _processMouseMotionEvent(e);
    }
    @Override
    public double getWeight() {
      return 1;
    }
    private void _processMouseMotionEvent(MouseEvent e) {
      MouseEvent event = getTargetEvent(e);
      if (event == null) {
        myGlassPane.setCursor(null, myListener);
        return;
      }

      processMouseMotionEvent(event);
      if (event.isConsumed()) {
        e.consume();
      }
    }

    private void _processMouseEvent(MouseEvent e) {
      MouseEvent event = getTargetEvent(e);
      if (event == null) {
        myGlassPane.setCursor(null, myListener);
        return;
      }

      processMouseEvent(event);
      if (event.isConsumed()) {
        e.consume();
      }
    }
  }

  private MouseEvent getTargetEvent(MouseEvent e) {
    return SwingUtilities.convertMouseEvent(e.getComponent(), e, this);
  }

  private void init() {
    myGlassPane = IdeGlassPaneUtil.find(this);
    myDisposable = Disposer.newDisposable();
    myGlassPane.addMouseMotionPreprocessor(myListener, myDisposable);
    myGlassPane.addMousePreprocessor(myListener, myDisposable);
  }

  public void setOrientation(boolean vertical) {
    removeAll();
    myVertical = vertical;
    final int cursorType = isVertical() ? Cursor.N_RESIZE_CURSOR : Cursor.W_RESIZE_CURSOR;
    setCursor(Cursor.getPredefinedCursor(cursorType));
  }

  @Override
  protected void processMouseMotionEvent(MouseEvent e) {
    super.processMouseMotionEvent(e);
    if (!myResizeEnabled) return;
    if (MouseEvent.MOUSE_DRAGGED == e.getID() && dragging) {
      myPoint = SwingUtilities.convertPoint(this, e.getPoint(), mySplitter);
      float proportion;
      final float firstMinProportion = getMinProportion(mySplitter.getFirstComponent());
      final float secondMinProportion = getMinProportion(mySplitter.getSecondComponent());
      if (isVertical()) {
        if (getHeight() > 0) {
          proportion = Math.min(1.0f, Math
            .max(.0f, Math.min(Math.max(firstMinProportion, (float)myPoint.y / (float)mySplitter.getHeight()), 1 - secondMinProportion)));
          mySplitter.setProportion(proportion);
        }
      }
      else {
        if (getWidth() > 0) {
          proportion = Math.min(1.0f, Math.max(.0f, Math.min(
            Math.max(firstMinProportion, (float)myPoint.x / (float)mySplitter.getWidth()), 1 - secondMinProportion)));
          mySplitter.setProportion(proportion);
        }
      }
    }
  }

  private float getMinProportion(JComponent component) {
    if (component != null &&
        mySplitter.getFirstComponent() != null &&
        mySplitter.getFirstComponent().isVisible() &&
        mySplitter.getSecondComponent() != null &&
        mySplitter.getSecondComponent().isVisible()) {
      if (isVertical()) {
        return (float)component.getMinimumSize().height / (float)(mySplitter.getHeight() - 1);
      } else {
        return (float)component.getMinimumSize().width / (float)(mySplitter.getWidth() - 1);
      }
    }

    return 0.0f;
  }

  @Override
  protected void processMouseEvent(MouseEvent e) {
    super.processMouseEvent(e);
    if (e.getID() == MouseEvent.MOUSE_CLICKED) {
      if (mySwitchOrientationEnabled
          && e.getClickCount() == 1
          && SwingUtilities.isLeftMouseButton(e) && (SystemInfo.isMac ? e.isMetaDown() : e.isControlDown())) {
        mySplitter.setOrientation(!mySplitter.getOrientation());
      }
      if (myResizeEnabled && e.getClickCount() == 2) {
        mySplitter.setProportion(.5f);
      }
    }
  }

  public void setResizeEnabled(boolean resizeEnabled) {
    myResizeEnabled = resizeEnabled;
    if (!myResizeEnabled) {
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    else {
      setCursor(isVertical() ?
                Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR) :
                Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
    }
  }

  public void setSwitchOrientationEnabled(boolean switchOrientationEnabled) {
    mySwitchOrientationEnabled = switchOrientationEnabled;
  }


  public boolean isVertical() {
    return myVertical;
  }
}
