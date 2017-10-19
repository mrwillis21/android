/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.android.tools.idea.naveditor.scene.draw;

import com.android.tools.adtui.common.SwingCoordinate;
import com.android.tools.idea.common.scene.SceneContext;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * {@linkplain DrawActionHandleDrag} is visible while the user is performing
 * a drag create from the action handle. It consists of a solid circle
 * where the action handle usually is as well as a line from the center
 * of the circle to the current mouse position.
 */
public class DrawActionHandleDrag extends NavBaseDrawCommand {
  public static final Stroke STROKE = new BasicStroke(3.0f);
  @SwingCoordinate private final int myX;
  @SwingCoordinate private final int myY;
  @SwingCoordinate private final int myRadius;

  public DrawActionHandleDrag(@SwingCoordinate int x,
                              @SwingCoordinate int y,
                              @SwingCoordinate int radius) {
    myX = x;
    myY = y;
    myRadius = radius;
  }

  public DrawActionHandleDrag(String s) {
    this(parse(s, 5));
  }

  private DrawActionHandleDrag(String[] sp) {
    this(Integer.parseInt(sp[2]), Integer.parseInt(sp[3]), Integer.parseInt(sp[4]));
  }

  @Override
  public int getLevel() {
    return DRAW_ACTION_HANDLE_DRAG;
  }

  @Override
  @NotNull
  protected Object[] getProperties() {
    return new Object[]{myX, myY, myRadius};
  }

  @Override
  public void paint(@NotNull Graphics2D g, @NotNull SceneContext sceneContext) {
    Graphics2D g2 = (Graphics2D)g.create();

    g2.setColor(sceneContext.getColorSet().getSelectedFrames());
    g2.fillOval(myX - myRadius, myY - myRadius, 2 * myRadius, 2 * myRadius);

    g2.setStroke(STROKE);
    g2.drawLine(myX, myY, sceneContext.getMouseX(), sceneContext.getMouseY());

    g2.dispose();
  }
}