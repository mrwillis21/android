/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.tools.idea.res;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.ide.common.res2.ResourceItem;
import com.android.ide.common.res2.ResourceTable;
import com.android.resources.ResourceType;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.PsiManager;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.facet.AndroidRootUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.android.SdkConstants.FD_SAMPLE_DATA;


public class SampleDataResourceRepository extends LocalResourceRepository {
  private final ResourceTable myFullTable;
  private AndroidFacet myAndroidFacet;

  @Nullable
  public static VirtualFile getSampleDataDir(@NotNull AndroidFacet androidFacet, boolean create) throws IOException {
    VirtualFile contentRoot = AndroidRootUtil.getMainContentRoot(androidFacet);
    if (contentRoot == null) {
      throw new IOException("Unable to find content root");
    }

    VirtualFile sampleDataDir = contentRoot.findFileByRelativePath("/" + FD_SAMPLE_DATA);
    if (sampleDataDir == null && create) {
        sampleDataDir = WriteCommandAction.runWriteCommandAction(androidFacet.getModule().getProject(),
                                                                 (ThrowableComputable<VirtualFile, IOException>)() -> contentRoot.createChildDirectory(androidFacet, FD_SAMPLE_DATA));
    }

    return sampleDataDir;
  }

  protected SampleDataResourceRepository(@NotNull AndroidFacet androidFacet) {
    super("SampleData");

    myFullTable = new ResourceTable();
    myAndroidFacet = androidFacet;

    VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileAdapter() {
      @Override
      public void fileCreated(@NotNull VirtualFileEvent event) {
        filesUpdated(event);
      }

      @Override
      public void fileDeleted(@NotNull VirtualFileEvent event) {
        filesUpdated(event);
      }

      @Override
      public void fileMoved(@NotNull VirtualFileMoveEvent event) {
        filesUpdated(event);
      }
    }, this);
    invalidate();
  }

  /**
   * Invalidates the current sample data of this repository. Call this method after the sample data has been updated to reload the contents.
   */
  private void invalidate() {
    VirtualFile sampleDataDir = null;
    try {
      sampleDataDir = getSampleDataDir(myAndroidFacet, false);
    }
    catch (IOException ignore) {
    }
    myFullTable.clear();
    if (sampleDataDir != null) {
      ImmutableListMultimap.Builder<String, ResourceItem> items = ImmutableListMultimap.builder();
      PsiManager psiManager = PsiManager.getInstance(myAndroidFacet.getModule().getProject());
      Stream<VirtualFile> childrenStream = Arrays.stream(sampleDataDir.getChildren());
      ApplicationManager.getApplication().runReadAction(() -> {
        childrenStream
          .map(psiManager::findFile)
          .filter(Objects::nonNull)
          .forEach(f -> items.put(f.getName(),
                                  new PsiResourceItem(f.getName(), ResourceType.SAMPLE_DATA, null, null, f)));
      });

      myFullTable.put(null, ResourceType.SAMPLE_DATA, items.build());
    }

    invalidateParentCaches(null, ResourceType.SAMPLE_DATA);
  }

  private void filesUpdated(@NotNull VirtualFileEvent event) {
    if (myAndroidFacet.isDisposed()) {
      return;
    }

    VirtualFile sampleDataDir = null;
    try {
      sampleDataDir = getSampleDataDir(myAndroidFacet, false);
    }
    catch (IOException ignored) {
    }

    VirtualFile eventFile = event.getFile();

    // Invalidate the existing cache if the change affects any sampledata directory children or the directory itself
    boolean invalidate = sampleDataDir != null && VfsUtilCore.isAncestor(sampleDataDir, eventFile, false);
    // Also account for the case where the directory itself is being added or removed
    invalidate = invalidate || FD_SAMPLE_DATA.equals(eventFile.getName());

    if (invalidate) {
      invalidate();
    }
  }

  @NonNull
  @Override
  protected ResourceTable getFullTable() {
    return myFullTable;
  }

  @Nullable
  @Override
  protected ListMultimap<String, ResourceItem> getMap(@Nullable String namespace, @NonNull ResourceType type, boolean create) {
    return myFullTable.get(namespace, type);
  }

  @NonNull
  @Override
  public Set<String> getNamespaces() {
    return Collections.emptySet();
  }

  @NotNull
  @Override
  protected Set<VirtualFile> computeResourceDirs() {
    return ImmutableSet.of();
  }

  @Override
  public void dispose() {
    myAndroidFacet = null;
    super.dispose();
  }
}
