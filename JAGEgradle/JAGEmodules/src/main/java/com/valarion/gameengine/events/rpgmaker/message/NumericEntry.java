/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Rub�n Tom�s Gracia
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package com.valarion.gameengine.events.rpgmaker.message;

import org.newdawn.slick.Font;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.w3c.dom.Element;

import com.valarion.gameengine.core.interfaces.Event;
import com.valarion.gameengine.core.tiled.SubTiledMap;
import com.valarion.gameengine.events.rpgmaker.SubEventClass;
import com.valarion.gameengine.gamestates.Controls;
import com.valarion.gameengine.gamestates.Database;
import com.valarion.gameengine.util.Util;
import com.valarion.gameengine.util.WindowImage;

/**
 * Class that gives an input as a dialog for a numeric value.
 * @author Rub�n Tom�s Gracia
 *
 */
public class NumericEntry extends SubEventClass {
	public static final int TOP = 0;
	public static final int MID = 1;
	public static final int BOT = 2;

	protected int position;

	protected WindowImage window;

	protected boolean showing = false;

	protected Image select;

	protected int length;
	protected int[] input;
	protected int selected;

	protected int var;

	protected int charw, charh;

	protected Image image = null;
	protected Image resized = null;

	@Override
	public void paralelupdate(GameContainer container, int delta,
			SubTiledMap map) throws SlickException {
		if (showing) {
			window.update(delta);
			if (container.getInput().isKeyPressed(Controls.accept)) {
				long result = 0;
				for (int number : input) {
					result = 10 * result + number;
				}
				Database.instance().getContext().getGlobalVars()[var] = result;
				showing = false;
				Database.instance().playSound("menuaccept");
			} else if (container.getInput().isKeyPressed(Controls.moveLeft)) {
				selected = (selected + length - 1) % length;
				Database.instance().playSound("menumove");
			} else if (container.getInput().isKeyPressed(Controls.moveRight)) {
				selected = (selected + 1) % length;
				Database.instance().playSound("menumove");
			} else if (container.getInput().isKeyPressed(Controls.moveUp)) {
				input[selected] = (input[selected] + 1) % 10;
				Database.instance().playSound("menumove");
			} else if (container.getInput().isKeyPressed(Controls.moveDown)) {
				input[selected] = (input[selected] + 9) % 10;
				Database.instance().playSound("menumove");
			}
		}
	}

	@Override
	public void postrender(GameContainer container, Graphics g, int tilewidth,
			int tileheight) throws SlickException {
		if (showing) {
			window.setShowArrow(false);

			Graphics i = window.getContain().getGraphics();
			i.clear();

			int x = window.getContain().getWidth() / 20;
			int y = window.getContain().getHeight() / 6;

			if (image != null) {
				int h = window.getContain().getHeight() - 2 * y;
				int neww = image.getWidth(), newh = image.getHeight();
				if (image.getHeight() != h) {
					neww = (int) (h / (float) image.getHeight() * image
							.getWidth());
					newh = h;
				}
				
				i.drawImage(image, x, y, x+neww, y+newh, 0, 0, image.getWidth(), image.getHeight());

				x = x + neww + x;
			}

			for (int index = 0; index < length; index++) {
				if (index == selected)
					i.drawImage(select, x - 2, y - 2);
				i.drawString(Integer.toString(input[index]), x, y);
				x += charh * 2;
			}

			switch (position) {
			case TOP:
				y = 0;
				break;
			case MID:
				y = container.getHeight() / 2
						- window.getImage().getHeight() / 2;
				break;
			case BOT:
				y = container.getHeight() - window.getImage().getHeight();
				break;
			}

			i.flush();
			g.drawImage(window.getImage(), 0, y);
		}
	}

	@Override
	public void loadEvent(Element node, Object context) throws SlickException {
		Database instance = Database.instance();
		window = instance.getWindowimages().get("dialog");

		String im = node.getAttribute("image");

		if (!im.equals(""))
			image = instance.getImages().get(im);
		else
			image = null;

		String pos = node.getAttribute("position");

		length = Integer.parseInt(node.getAttribute("length"));

		var = Integer.parseInt(node.getAttribute("var"));

		if ("top".equals(pos)) {
			position = TOP;
		} else if ("mid".equals(pos)) {
			position = MID;
		} else {
			position = BOT;
		}
	}

	@Override
	public boolean isWorking() {
		return showing;
	}

	@Override
	public void performAction(GameContainer container, SubTiledMap map, Event e)
			throws SlickException {
		if (!showing) {
			showing = true;
			input = new int[length];
			Font font = container.getGraphics().getFont();
			charw = font.getWidth("0");
			charh = font.getLineHeight();
			select = Util.getScaled(window.getModel().getImage("selection"),charw + 6, charh + 4);
		}
	}
}
