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
package com.valarion.gameengine.strings;

import org.newdawn.slick.Color;
import org.newdawn.slick.SlickException;
import org.w3c.dom.Element;

import com.valarion.gameengine.core.interfaces.ColoredString;
import com.valarion.gameengine.events.rpgmaker.FlowEventInterface;

/**
 * Wrapper class for map name string.
 * @author Rub�n Tom�s Gracia
 *
 */
public class MapName implements ColoredString {
	protected Color color;
	protected String name;
	
	protected FlowEventInterface parent;

	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public String getString() {
		name = parent.getState().getActive().getMapProperty("name", "");
		return name;
	}

	@Override
	public int length() {
		return name.length();
	}

	@Override
	public void load(Element node, Object context) throws SlickException {
		if(context instanceof FlowEventInterface) {
			parent = (FlowEventInterface) context;
		}
		try {
			color = new Color(Integer.parseInt(node.getAttribute("r")),
					Integer.parseInt(node.getAttribute("g")),
					Integer.parseInt(node.getAttribute("b")));
		} catch (Exception e) {
			color = Color.white;
		}
	}

}
