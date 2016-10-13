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
package com.android.tools.idea.lint;

import com.android.tools.lint.checks.UnpackedNativeCodeDetector;
import org.jetbrains.android.inspections.lint.AndroidLintInspectionBase;
import org.jetbrains.android.inspections.lint.AndroidLintQuickFix;
import org.jetbrains.android.inspections.lint.SetAttributeQuickFix;
import org.jetbrains.android.util.AndroidBundle;
import org.jetbrains.annotations.NotNull;

import static com.android.SdkConstants.ANDROID_URI;
import static com.android.SdkConstants.VALUE_FALSE;
import static com.android.xml.AndroidManifest.ATTRIBUTE_EXTRACT_NATIVE_LIBS;

public class AndroidLintUnpackedNativeCodeInspection extends AndroidLintInspectionBase {
  public AndroidLintUnpackedNativeCodeInspection() {
    super(AndroidBundle.message("android.lint.inspections.unpacked.native.code"), UnpackedNativeCodeDetector.ISSUE);
  }

  @NotNull
  @Override
  public AndroidLintQuickFix[] getQuickFixes(@NotNull String message) {
    return new AndroidLintQuickFix[]{new SetAttributeQuickFix("Add android:extractNativeLibs attribute",
                                                              ATTRIBUTE_EXTRACT_NATIVE_LIBS,
                                                              ANDROID_URI, VALUE_FALSE)};
  }
}
