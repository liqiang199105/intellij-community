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
package git4idea.config;

import com.intellij.openapi.components.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@State(
  name = "GitSharedSettings",
  storages = {
    @Storage(file = StoragePathMacros.PROJECT_FILE),
    @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/vcs.xml", scheme = StorageScheme.DIRECTORY_BASED)
  })
public class GitSharedSettings implements PersistentStateComponent<GitSharedSettings.State> {

  public static class State {
    public List<String> FORCE_PUSH_PROHIBITED_PATTERNS = Arrays.asList("master");
  }

  private State myState = new State();

  @Nullable
  @Override
  public State getState() {
    return myState;
  }

  @Override
  public void loadState(State state) {
    myState = state;
  }

  @NotNull
  public List<String> getForcePushProhibitedPatterns() {
    return Collections.unmodifiableList(myState.FORCE_PUSH_PROHIBITED_PATTERNS);
  }

  public void setForcePushProhibitedPatters(@NotNull List<String> patterns) {
    myState.FORCE_PUSH_PROHIBITED_PATTERNS = new ArrayList<String>(patterns);
  }

}
