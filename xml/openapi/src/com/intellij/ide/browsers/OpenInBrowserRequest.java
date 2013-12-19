package com.intellij.ide.browsers;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.Url;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public abstract class OpenInBrowserRequest {
  private Collection<Url> result;
  protected PsiFile file;

  OpenInBrowserRequest(@NotNull PsiFile file) {
    this.file = file;
  }

  OpenInBrowserRequest() {
  }

  @Nullable
  public static OpenInBrowserRequest createRequest(@NotNull final PsiElement element) {
    PsiFile psiFile = element.getContainingFile();
    if (psiFile == null) {
      return null;
    }

    return new OpenInBrowserRequest(psiFile) {
      @NotNull
      @Override
      public PsiElement getElement() {
        return element;
      }
    };
  }

  @NotNull
  public PsiFile getFile() {
    return file;
  }

  @NotNull
  public VirtualFile getVirtualFile() {
    return file.getVirtualFile();
  }

  @NotNull
  public Project getProject() {
    return file.getProject();
  }

  @NotNull
  public abstract PsiElement getElement();

  public void setResult(@NotNull Collection<Url> result) {
    this.result = result;
  }

  @Nullable
  public Collection<Url> getResult() {
    return result;
  }
}