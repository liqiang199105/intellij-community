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
package com.intellij.debugger.settings;

import com.intellij.debugger.engine.FullValueEvaluatorProvider;
import com.intellij.debugger.engine.evaluation.EvaluateException;
import com.intellij.debugger.engine.evaluation.EvaluationContext;
import com.intellij.debugger.engine.evaluation.EvaluationContextImpl;
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl;
import com.intellij.debugger.ui.tree.ValueDescriptor;
import com.intellij.debugger.ui.tree.render.CompoundReferenceRenderer;
import com.intellij.debugger.ui.tree.render.DescriptorLabelListener;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.IconUtil;
import com.intellij.xdebugger.frame.XFullValueEvaluator;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
* Created by Egor on 04.10.2014.
*/
class IconObjectRenderer extends CompoundReferenceRenderer implements FullValueEvaluatorProvider {

  public IconObjectRenderer(final NodeRendererSettings rendererSettings) {
    super(rendererSettings, "Icon", null, null);
    setClassName("javax.swing.Icon");
  }

  public String calcLabel(ValueDescriptor descriptor, EvaluationContext evaluationContext, DescriptorLabelListener listener) throws
                                                                                                                             EvaluateException {
    String res = calcToStringLabel(descriptor, evaluationContext, listener);
    if (res != null) {
      return res;
    }
    return super.calcLabel(descriptor, evaluationContext, listener);
  }

  @Override
  public Icon calcValueIcon(ValueDescriptor descriptor, EvaluationContext evaluationContext, DescriptorLabelListener listener)
    throws EvaluateException {
    ImageIcon icon = ImageObjectRenderer.getIcon(evaluationContext, descriptor.getValue(), "iconToBytes");
    if (icon != null) {
      return IconUtil.cropIcon(icon, 16, 16);
    }
    return null;
  }

  @NotNull
  @Override
  public XFullValueEvaluator getFullValueEvaluator(final EvaluationContextImpl evaluationContext, final ValueDescriptorImpl valueDescriptor) {
    return new CustomPopupFullValueEvaluator(" (Show icon)", evaluationContext) {
      @Override
      protected JComponent createComponent() {
        return new JBLabel(ImageObjectRenderer.getIcon(myEvaluationContext, valueDescriptor.getValue(), "iconToBytes"));
      }
    };
  }
}
