/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.tools.idea.monitor.gpu;

import com.android.ddmlib.Client;
import com.android.ddmlib.ClientData;
import com.android.ddmlib.IDevice;
import com.android.tools.adtui.TimelineData;
import com.android.tools.idea.monitor.DeviceSampler;
import com.android.tools.idea.monitor.gpu.gfxinfohandlers.*;
import com.intellij.util.ThreeState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GpuSampler extends DeviceSampler {
  private final GfxinfoHandler[] myGfxinfoHandlers = new GfxinfoHandler[]{new JHandler(), new LHandler(), new MHandler(), new NHandler()};
  private volatile GfxinfoHandler myCurrentGfxinfoHandler;
  private volatile int myApiLevel = JHandler.MIN_API_LEVEL;

  @Nullable protected ProfileStateListener myProfileStateListener;
  private boolean myGpuProfileSetting = true; // Flag to determine if the GPU profiling setting on the device is enabled.

  public GpuSampler(int sampleFrequencyMs) {
    super(new TimelineData(3, GfxinfoHandler.SAMPLE_BUFFER_SIZE), sampleFrequencyMs); // Use a dummy TimelineData.
  }

  @NotNull
  @Override
  public String getName() {
    return "GPU Sampler";
  }

  public int getApiLevel() {
    return myApiLevel;
  }

  @Override
  protected boolean requiresSamplerRestart(@Nullable Client client) {
    return super.requiresSamplerRestart(client) || (client != null && decodeApiLevel(client) != myApiLevel);
  }

  @Override
  protected synchronized void prepareSampler() {
    Client client = getClient();
    if (client != null) {
      int newApiLevel = decodeApiLevel(client);

      boolean createNewTimelineData = false;
      if (newApiLevel != myApiLevel) {
        myApiLevel = newApiLevel;
        for (GfxinfoHandler handler : myGfxinfoHandlers) {
          if (handler.accept(client)) {
            createNewTimelineData = true;
            break;
          }
        }
      }

      for (GfxinfoHandler handler : myGfxinfoHandlers) {
        if (handler.accept(client)) {
          myCurrentGfxinfoHandler = handler;
          // Do not break here, as we will rely on the ordering of samplers to find the preferred sampler.
        }
      }

      if (createNewTimelineData) {
        setTimelineData(myCurrentGfxinfoHandler.createTimelineData());
      }
    }
    else {
      myCurrentGfxinfoHandler = null;
    }

    setGpuProfileSetting(true);
    if (myCurrentGfxinfoHandler != null) {
      myCurrentGfxinfoHandler.setClient(client);
    }
    getTimelineData().clear();
  }

  @Override
  protected void sample(boolean forced) throws InterruptedException {
    Client client = getClient();
    assert client != null;

    IDevice device = client.getDevice();
    if (device != null) {
      try {
        ClientData data = client.getClientData();

        ThreeState newGpuProfilingState = myCurrentGfxinfoHandler.getIsEnabledOnDevice(device);
        if (newGpuProfilingState != ThreeState.UNSURE) {
          boolean newGpuBooleanState = newGpuProfilingState.toBoolean();
          setGpuProfileSetting(newGpuBooleanState);
        }

        if (myGpuProfileSetting) {
          myCurrentGfxinfoHandler.sample(device, data, getTimelineData());
        }
      }
      catch (RuntimeException e) {
        throw new InterruptedException("Sample error, interrupting.");
      }
      catch (Exception ignored) {
      }
    }
  }

  private void setGpuProfileSetting(boolean newSetting) {
    if (myGpuProfileSetting != newSetting) {
      myGpuProfileSetting = newSetting;
      Client client = getClient();
      if ((client != null) && (myProfileStateListener != null)) {
        myProfileStateListener.notifyGpuProfileStateChanged(client, myGpuProfileSetting);
      }
    }
  }

  public static int decodeApiLevel(@NotNull Client client) {
    return client.getDevice().getVersion().getFeatureLevel();
  }
}
