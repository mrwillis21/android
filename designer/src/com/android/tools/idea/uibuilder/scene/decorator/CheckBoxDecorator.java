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
package com.android.tools.idea.uibuilder.scene.decorator;

import com.android.tools.idea.uibuilder.handlers.constraint.ConstraintUtilities; // TODO: remove
import com.android.tools.idea.uibuilder.model.AndroidDpCoordinate;
import com.android.tools.adtui.common.SwingCoordinate;
import com.android.tools.idea.uibuilder.scene.SceneComponent;
import com.android.tools.idea.uibuilder.scene.SceneContext;
import com.android.tools.idea.uibuilder.scene.draw.DisplayList;
import com.android.tools.idea.uibuilder.scene.draw.DrawTextRegion;
import com.android.tools.sherpa.drawing.ColorSet;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * Support Buttons
 */
public class CheckBoxDecorator extends SceneDecorator {
  public static class DrawCheckbox extends DrawTextRegion {
    private float mScale;
    int[] xp = new int[3];
    int[] yp = new int[3];

    @Override
    public int getLevel() {
      return COMPONENT_LEVEL;
    }

    DrawCheckbox(@SwingCoordinate int x,
                 @SwingCoordinate int y,
                 @SwingCoordinate int width,
                 @SwingCoordinate int height,
                 int baselineOffset,
                 float scale,
                 String text) {
      super(x, y, width, height, baselineOffset, text, true, false, DrawTextRegion.TEXT_ALIGNMENT_VIEW_START,
            DrawTextRegion.TEXT_ALIGNMENT_CENTER, 32,scale);
    }

    @NotNull
    public static DrawCheckbox createFromString(@NotNull String s) {
      String[] sp = s.split(",");
      int c = 0;
      int x = Integer.parseInt(sp[c++]);
      int y = Integer.parseInt(sp[c++]);
      int width = Integer.parseInt(sp[c++]);
      int height = Integer.parseInt(sp[c++]);
      int baseLineOffset = Integer.parseInt(sp[c++]);
      float scale = java.lang.Float.parseFloat(sp[c++]);
      String text = s.substring(s.indexOf('\"') + 1, s.lastIndexOf('\"'));

      return new DrawCheckbox(x, y, width, height, baseLineOffset, scale, text);
    }

    @Override
    public String serialize() {
      return this.getClass().getSimpleName() +
             "," +
             x +
             "," +
             y +
             "," +
             width +
             "," +
             height +
             "," +
             myBaseLineOffset +
             "," +
             mScale +
             ",\"" +
             mText +
             "\"";
    }

    @Override
    public void paint(Graphics2D g, SceneContext sceneContext) {
      int margin = height / 5;
      mHorizontalPadding = height;
      super.paint(g, sceneContext);
      ColorSet colorSet = sceneContext.getColorSet();
      if (colorSet.drawBackground()) {
        Stroke stroke = g.getStroke();
        g.setStroke(new BasicStroke(2));
        g.setColor(colorSet.getFakeUI());
        g.drawRoundRect(x + margin, y + margin, height - margin * 2, height - margin * 2, 4, 4);
        margin *= 2;
        int xv = x + margin;
        int yv = y + margin;
        int h = height - margin * 2;
        xp[0] = xv;
        xp[1] = xv + h / 3;
        xp[2] = xv + h;
        yp[0] = yv + h / 2;
        yp[1] = yv + h;
        yp[2] = yv;
        g.drawPolyline(xp, yp, 3);
        g.setStroke(stroke);
      }
    }
  }

  @Override
  public void addContent(@NotNull DisplayList list, long time, @NotNull SceneContext sceneContext, @NotNull SceneComponent component) {
    super.addContent(list, time, sceneContext, component);
    @AndroidDpCoordinate Rectangle rect = new Rectangle();
    component.fillDrawRect(time, rect);
    @SwingCoordinate int l = sceneContext.getSwingX(rect.x);
    @SwingCoordinate int t = sceneContext.getSwingY(rect.y);
    @SwingCoordinate int w = sceneContext.getSwingDimension(rect.width);
    @SwingCoordinate int h = sceneContext.getSwingDimension(rect.height);
    String text = ConstraintUtilities.getResolvedText(component.getNlComponent());
    int baseLineOffset = sceneContext.getSwingDimension(component.getBaseline());
    float scale = (float)sceneContext.getScale();
    list.add(new DrawCheckbox(l, t, w, h, baseLineOffset, scale, text));
  }
}
