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
package com.valarion.gameengine.events;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.w3c.dom.Element;

import com.valarion.gameengine.core.GameCore;
import com.valarion.gameengine.core.interfaces.Event;
import com.valarion.gameengine.core.tiled.SubTiledMap;
import com.valarion.gameengine.events.menu.ingamemenu.MenuMain;
import com.valarion.gameengine.gamestates.Controls;
import com.valarion.gameengine.gamestates.Database;
import com.valarion.gameengine.gamestates.GameContext;
import com.valarion.gameengine.gamestates.SubState;
import com.valarion.gameengine.util.GameSprite;

/**
 * Class representing the player and all it's mechanics.
 * 
 * @author Rub�n Tom�s Gracia
 *
 */
public class Player implements Event, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8157747779077278611L;

	protected transient GameSprite sprite;

	protected int xPos = 10, yPos = 10;
	protected float xOff = 0.0f, yOff = 0.0f;

	protected boolean moving = false;
	protected int wait = 0;
	protected int limit = 150;

	protected float movingspeed = 0.1f;
	protected float spritespeed = 1.0f;

	protected Route movementrouteobject;
	protected Iterator<Integer> movementiterator;

	public Player() {
	}

	@Override
	public void update(GameContainer container, int delta, SubTiledMap map) throws SlickException {
		if (sprite != null) {
			Input input = container.getInput();
			if (!isMoving() && GameCore.getInstance().getActive() instanceof SubState
					&& ((SubState) GameCore.getInstance().getActive()).getActiveEvents().size() == 0
					&& input.isKeyPressed(Controls.cancel)) {
				((SubState) GameCore.getInstance().getActive()).getActiveEvents()
						.add(new MenuMain(((SubState) GameCore.getInstance().getActive())));
			}
			boolean activated = false;
			if (input.isKeyPressed(Controls.accept)) {
				for (Event e : map.getEvents(xPos, yPos)) {
					if (activated == true)
						break;
					activated = true;
					e.onEventActivation(container, map, this);
				}
				switch (sprite.getDirection()) {
				case GameSprite.UP:
					for (Event e : map.getEvents(xPos, yPos - 1)) {
						e.onEventActivation(container, map, this);
						if (activated == true)
							break;
						activated = true;
					}
					break;
				case GameSprite.DOWN:
					for (Event e : map.getEvents(xPos, yPos + 1)) {
						e.onEventActivation(container, map, this);
						if (activated == true)
							break;
						activated = true;
					}
					break;
				case GameSprite.LEFT:
					for (Event e : map.getEvents(xPos - 1, yPos)) {
						e.onEventActivation(container, map, this);
						if (activated == true)
							break;
						activated = true;
					}
					break;
				case GameSprite.RIGHT:
					for (Event e : map.getEvents(xPos + 1, yPos)) {
						e.onEventActivation(container, map, this);
						if (activated == true)
							break;
						activated = true;
					}
					break;
				}
			} else {
				float multiplier = 1.0f;

				if (input.isKeyDown(Controls.speedUp)) {
					multiplier = 2.0f;
				}

				sprite.setMultiplier(multiplier);

				if (moving) {
					sprite.update(delta);
					switch (sprite.getDirection()) {
					case GameSprite.UP:
						// The lower the delta the slowest the sprite will
						// animate.
						yOff -= delta * movingspeed * multiplier;
						if (-yOff >= map.getTileHeight()) {
							yOff = 0.0f;
							map.getEvents(xPos, yPos).remove(this);
							yPos--;
							moving = false;
						}
						break;
					case GameSprite.DOWN:
						// The lower the delta the slowest the sprite will
						// animate.
						yOff += delta * movingspeed * multiplier;

						if (yOff >= map.getTileHeight()) {
							yOff = 0.0f;
							map.getEvents(xPos, yPos).remove(this);
							yPos++;
							moving = false;
						}
						break;
					case GameSprite.LEFT:
						// The lower the delta the slowest the sprite will
						// animate.
						xOff -= delta * movingspeed * multiplier;

						if (-xOff >= map.getTileWidth()) {
							xOff = 0.0f;
							map.getEvents(xPos, yPos).remove(this);
							xPos--;
							moving = false;
						}
						break;
					case GameSprite.RIGHT:
						// The lower the delta the slowest the sprite will
						// animate.
						xOff += delta * movingspeed * multiplier;
						if (xOff >= map.getTileWidth()) {
							xOff = 0.0f;
							map.getEvents(xPos, yPos).remove(this);
							xPos++;
							moving = false;
						}
						break;
					}

					if (moving == false) {
						GameContext c = Database.instance().getContext();
						c.setStat("steps", c.getStat("steps") + 1);
						sprite.setStopped();
						for (Event e : map.getEvents(xPos, yPos)) {
							e.onEventTouch(container, map, this);
							e.onBeingTouched(container, map, this);
						}
					}

				} else {
					if (movementrouteobject != null) {
						if (movementiterator == null) {
							movementiterator = movementrouteobject.getRoute().iterator();
						}
						if(movementiterator.hasNext()) {
							activated = setMovement(movementiterator.next(),container,delta,map,activated);
						}
						else {
							movementrouteobject = null;
							movementiterator = null;
						}
						
					} else {
						if (input.isKeyDown(Controls.moveUp)) {
							if ((wait == 0 && sprite.getDirection() == GameSprite.UP) || wait > limit) {
								if (!map.isBlocked(xPos, yPos - 1)) {
									map.getEvents(xPos, yPos - 1).add(this);
									moving = true;

									yOff = 0.0f;

									wait -= delta;
								} else {
									for (Event e : map.getEvents(xPos, yPos - 1)) {
										if (e.isBlocking()) {
											if (activated == true)
												break;
											activated = true;
											e.onEventTouch(container, map, this);
											e.onBeingTouched(container, map, this);
										}
									}
								}
							}
							sprite.setDirection(GameSprite.UP);
							wait += delta;
						} else if (input.isKeyDown(Controls.moveDown)) {
							if ((wait == 0 && sprite.getDirection() == GameSprite.DOWN) || wait > limit) {
								if (!map.isBlocked(xPos, yPos + 1)) {
									map.getEvents(xPos, yPos + 1).add(this);
									moving = true;

									yOff = 0.0f;

									wait -= delta;
								} else {
									for (Event e : map.getEvents(xPos, yPos + 1)) {
										if (e.isBlocking()) {
											if (activated == true)
												break;
											activated = true;
											e.onEventTouch(container, map, this);
											e.onBeingTouched(container, map, this);
										}
									}
								}
							}
							sprite.setDirection(GameSprite.DOWN);
							wait += delta;
						} else if (input.isKeyDown(Controls.moveLeft)) {
							if ((wait == 0 && sprite.getDirection() == GameSprite.LEFT) || wait > limit) {
								if (!map.isBlocked(xPos - 1, yPos)) {
									map.getEvents(xPos - 1, yPos).add(this);
									moving = true;

									xOff = 0.0f;

									wait -= delta;
								} else {
									for (Event e : map.getEvents(xPos - 1, yPos)) {
										if (e.isBlocking()) {
											if (activated == true)
												break;
											activated = true;
											e.onEventTouch(container, map, this);
											e.onBeingTouched(container, map, this);
										}
									}
								}
							}
							sprite.setDirection(GameSprite.LEFT);
							wait += delta;
						} else if (input.isKeyDown(Controls.moveRight)) {
							if ((wait == 0 && sprite.getDirection() == GameSprite.RIGHT) || wait > limit) {
								if (!map.isBlocked(xPos + 1, yPos)) {
									map.getEvents(xPos + 1, yPos).add(this);
									moving = true;

									xOff = 0.0f;

									wait -= delta;
								} else {
									for (Event e : map.getEvents(xPos + 1, yPos)) {
										if (e.isBlocking()) {
											if (activated == true)
												break;
											activated = true;
											e.onEventTouch(container, map, this);
											e.onBeingTouched(container, map, this);
										}
									}
								}
							}
							sprite.setDirection(GameSprite.RIGHT);
							wait += delta;
						} else if (wait > 0) {
							wait = 0;

							for (Event e : map.getEvents(xPos, yPos)) {
								if (activated == true)
									break;
								activated = true;
								e.onEventTouch(container, map, this);
								e.onBeingTouched(container, map, this);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public String getLayerName() {
		return "player";
	}

	@Override
	public void prerender(GameContainer container, Graphics g, int tilewidth, int tileheight) throws SlickException {

	}

	@Override
	public void render(GameContainer container, Graphics g, int tilewidth, int tileheight) throws SlickException {
		if (sprite != null) {
			sprite.draw((int)getXDraw(tilewidth), (int)getYDraw(tileheight));
		}
	}

	@Override
	public void postrender(GameContainer container, Graphics g, int tilewidth, int tileheight) throws SlickException {

	}

	@Override
	public float getXDraw(int tilewidth) {
		return (xPos * tilewidth + tilewidth / 2 - getWidth() / 2 + xOff);
	}

	@Override
	public float getYDraw(int tileheight) {
		return ((yPos + 1) * tileheight - getHeight() + yOff);
	}

	@Override
	public int getWidth() {
		if (sprite != null) {
			return sprite.getWidth();
		} else {
			return 0;
		}
	}

	@Override
	public int getHeight() {
		if (sprite != null) {
			return sprite.getHeight();
		} else {
			return 0;
		}
	}

	@Override
	public void onMapLoad(GameContainer container, SubTiledMap map) {
	}

	@Override
	public void onMapSetAsActive(GameContainer container, SubTiledMap map) {
	}

	@Override
	public void onEventActivation(GameContainer container, SubTiledMap map, Event e) {
	}

	@Override
	public void onEventTouch(GameContainer container, SubTiledMap map, Event e) {
	}

	@Override
	public void onBeingTouched(GameContainer container, SubTiledMap map, Event e) {
	}

	@Override
	public void loadEvent(Element node, Object context) throws SlickException {
		xPos = Integer.parseInt(node.getAttribute("x"));
		yPos = Integer.parseInt(node.getAttribute("y"));
		spritespeed = Float.parseFloat(node.getAttribute("spritespeed"));
		movingspeed = Float.parseFloat(node.getAttribute("movingspeed"));
		try {
			sprite = Database.instance().getSprites().get(node.getAttribute("sprite")).createSprite(spritespeed,
					movingspeed);
		} catch (Exception e) {

		}
	}

	@Override
	public boolean isBlocking() {
		return true;
	}

	@Override
	public void paralelupdate(GameContainer container, int delta, SubTiledMap map) throws SlickException {
	}

	@Override
	public void setXPos(int newPos) {
		xPos = newPos;
	}

	@Override
	public void setYPos(int newPos) {
		yPos = newPos;
	}

	@Override
	public int getDirection() {
		if (sprite != null) {
			return sprite.getDirection();
		} else {
			return 0;
		}
	}

	@Override
	public void setDirection(int direction) {
		if (sprite != null) {
			sprite.setDirection(direction);
		}
	}

	@Override
	public int getXPos() {
		return xPos;
	}

	@Override
	public int getYPos() {
		return yPos;
	}

	@Override
	public boolean isWorking() {
		return true;
	}

	@Override
	public void performAction(GameContainer container, SubTiledMap map, Event e) throws SlickException {

	}

	@Override
	public void onMapSetAsInactive(GameContainer container, SubTiledMap map) throws SlickException {

	}

	@Override
	public String getId() {
		return null;
	}

	/**
	 * Method for serializing the object.
	 * 
	 * @param stream
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
		if (sprite != null) {
			stream.writeBoolean(true);
			stream.writeObject(sprite.getName());
			stream.writeFloat(sprite.getSpritespeed());
			stream.writeFloat(sprite.getMovingspeed());
			stream.writeInt(sprite.getDirection());
		} else {
			stream.writeBoolean(false);
		}
	}

	/**
	 * MEthod for deserializing the object.
	 * 
	 * @param stream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		if (stream.readBoolean()) {
			sprite = Database.instance().getSprites().get((String) (stream.readObject()))
					.createSprite(stream.readFloat(), stream.readFloat());
			sprite.setDirection(stream.readInt());
		}
	}

	/**
	 * Returns true if the player is moving.
	 * 
	 * @return
	 */
	public boolean isMoving() {
		return moving;
	}

	public void setSprite(GameSprite sprite) {
		this.sprite = sprite;
		if (sprite != null) {
			this.movingspeed = sprite.getMovingspeed();
			this.spritespeed = sprite.getSpritespeed();
		}
	}

	public GameSprite getSprite() {
		return sprite;
	}

	protected boolean setMovement(int movement, GameContainer container, int delta, SubTiledMap map, boolean activated) throws SlickException {
		switch(movement) {
		case Moving.LOOKUP:
			sprite.setDirection(GameSprite.UP);
			break;
		case Moving.LOOKDOWN:
			sprite.setDirection(GameSprite.DOWN);
			break;
		case Moving.LOOKLEFT:
			sprite.setDirection(GameSprite.LEFT);
			break;
		case Moving.LOOKRIGHT:
			sprite.setDirection(GameSprite.RIGHT);
			break;
		case Moving.MOVEUP:
			sprite.setDirection(GameSprite.UP);
			if (!map.isBlocked(xPos, yPos - 1)) {
				map.getEvents(xPos, yPos - 1).add(this);
				moving = true;

				yOff = 0.0f;

				wait -= delta;
			} else {
				for (Event e : map.getEvents(xPos, yPos - 1)) {
					if (e.isBlocking()) {
						if (activated == true)
							break;
						activated = true;
						e.onEventTouch(container, map, this);
						e.onBeingTouched(container, map, this);
					}
				}
			}
			break;
		case Moving.MOVEDOWN:
			sprite.setDirection(GameSprite.DOWN);
			if (!map.isBlocked(xPos, yPos + 1)) {
				map.getEvents(xPos, yPos + 1).add(this);
				moving = true;

				yOff = 0.0f;

				wait -= delta;
			} else {
				for (Event e : map.getEvents(xPos, yPos + 1)) {
					if (e.isBlocking()) {
						if (activated == true)
							break;
						activated = true;
						e.onEventTouch(container, map, this);
						e.onBeingTouched(container, map, this);
					}
				}
			}
			break;
		case Moving.MOVELEFT:
			sprite.setDirection(GameSprite.LEFT);
			if (!map.isBlocked(xPos - 1, yPos)) {
				map.getEvents(xPos - 1, yPos).add(this);
				moving = true;

				xOff = 0.0f;

				wait -= delta;
			} else {
				for (Event e : map.getEvents(xPos - 1, yPos)) {
					if (e.isBlocking()) {
						if (activated == true)
							break;
						activated = true;
						e.onEventTouch(container, map, this);
						e.onBeingTouched(container, map, this);
					}
				}
			}
			break;
		case Moving.MOVERIGHT:
			sprite.setDirection(GameSprite.RIGHT);
			if (!map.isBlocked(xPos + 1, yPos)) {
				map.getEvents(xPos + 1, yPos).add(this);
				moving = true;

				xOff = 0.0f;

				wait -= delta;
			} else {
				for (Event e : map.getEvents(xPos + 1, yPos)) {
					if (e.isBlocking()) {
						if (activated == true)
							break;
						activated = true;
						e.onEventTouch(container, map, this);
						e.onBeingTouched(container, map, this);
					}
				}
			}
			break;

		}
		return activated;
	}
	
	public void setRoute(Route route) {
		this.movementrouteobject = route;
	}
}
