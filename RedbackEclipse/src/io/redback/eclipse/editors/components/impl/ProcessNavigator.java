package io.redback.eclipse.editors.components.impl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Widget;

import io.firebus.utils.DataList;
import io.firebus.utils.DataMap;

import io.redback.eclipse.editors.components.Manager;
import io.redback.eclipse.editors.components.Navigator;
import io.redback.eclipse.editors.components.NavigatorAction;

public class ProcessNavigator extends Navigator implements PaintListener, MouseListener, MouseMoveListener {
	
	protected Display display;
	protected ScrolledComposite scrollable;
	protected Canvas canvas;
	protected int boxWidth = 85;
	protected int boxHeight = 78;
	protected int canvasWidth = 500;
	protected int canvasHeight = 200;
	protected int scrollHPos = 0;
	protected int scrollVPos = 0;
	protected DataMap selectedNode;
	protected DataMap draggingNode;
	protected int dragOffsetX;
	protected int dragOffsetY;
	protected Map<String, String> typeLabels;
	protected Font headerFont;
	protected Font labelFont;

	
	public ProcessNavigator(DataMap d, Manager m, Composite p, int s) {
		super(d, m, p, s);
		typeLabels = new HashMap<String, String>();
		typeLabels.put("interaction", "Interaction");
		typeLabels.put("action", "Action");
		typeLabels.put("condition", "Condition"); 
		typeLabels.put("rbobjectget", "Rbo Get"); 
		typeLabels.put("rbobjectexecute", "Rbo Execute");
		typeLabels.put("rbobjectupdate", "Rbo Update");
		typeLabels.put("script", "Script");
		typeLabels.put("firebusrequest", "Firebus Req.");
		typeLabels.put("domainservice", "Domain Serv.");
		typeLabels.put("join", "Join");
		FontData fd = new FontData();
		fd.setHeight(7);
		headerFont = new Font(Display.getCurrent(), fd);
		fd = new FontData();
		fd.setHeight(9);
		labelFont = new Font(Display.getCurrent(), fd);
		createUI();
	}

	protected void createUI() {
		display = Display.getCurrent();
		addMenuDetectListener(this);
		setLayout(new FillLayout());
		setBackground(new Color(display, 255, 255, 255));
		
		canvas = new Canvas(this, SWT.NO_REDRAW_RESIZE | SWT.H_SCROLL | SWT.V_SCROLL);
		canvas.setBackground(new Color(display, 255, 255, 255));
		canvas.addPaintListener(this);
		canvas.addMouseListener(this);
		canvas.addMouseMoveListener(this);
		canvas.addListener (SWT.Resize,  new Listener () {
		    public void handleEvent (Event e) {
		    	calcCanvasSize();
		    }
		});
		canvas.getVerticalBar().addListener(SWT.Selection, new Listener() {
	        public void handleEvent(Event e) {
	            int scrollDiff = -((ScrollBar)e.widget).getSelection() - scrollVPos;
	            canvas.scroll(0, scrollDiff, 0, 0, canvasWidth, canvasHeight, false);
	            scrollVPos += scrollDiff;
	        }
	    });
		canvas.getHorizontalBar().addListener(SWT.Selection, new Listener() {
	        public void handleEvent(Event e) {
	            int scrollDiff = -((ScrollBar)e.widget).getSelection() - scrollHPos;
	            canvas.scroll(scrollDiff, 0, 0, 0, canvasWidth, canvasHeight, false);
	            scrollHPos += scrollDiff;
	        }
	    });
		
		calcCanvasSize();
	}
	
	protected void createContextMenu(Menu menu, String type, String name) {
		MenuItem item = null;
		if(type.equals("header")) {
			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Create node");
		    item.setData(new NavigatorAction("create", type, name));

			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Make space");
		    item.setData(new NavigatorAction("space", type, name));

		} else {
			item = new MenuItem(menu, SWT.PUSH);
		    item.setText("Delete node");
		    item.setData(new NavigatorAction("delete", type, name));
		}
	}
	
	protected void calcCanvasSize() {
		canvasWidth = 0;
		canvasHeight = 0;
		for(int i = 0; i < _data.getList("nodes").size(); i++) {
			DataMap node = _data.getList("nodes").getObject(i);
			int x = node.getNumber("position.x").intValue();
			int y = node.getNumber("position.y").intValue();
			if(x > canvasWidth)
				canvasWidth = x;
			if(y > canvasHeight)
				canvasHeight = y;
		}
		canvasHeight += (2 * boxWidth);
		canvasWidth += (2 * boxHeight);
		canvas.getHorizontalBar().setMaximum(canvasWidth);
		canvas.getHorizontalBar().setThumb(canvas.getSize().x);
		canvas.getVerticalBar().setMaximum(canvasHeight);
		canvas.getVerticalBar().setThumb(canvas.getSize().y);
	}

	public void paintControl(PaintEvent event) {
		GC gc = event.gc;
		Display display = Display.getCurrent();
		for(int i = 0; i < _data.getList("nodes").size(); i++) {
			DataMap node = _data.getList("nodes").getObject(i);
			int x = node.getNumber("position.x").intValue() + scrollHPos;
			int y = node.getNumber("position.y").intValue() + scrollVPos;
			String type = node.getString("type");
			if(type.equals("join")) {
				if(node == selectedNode)
					gc.setBackground(new Color(display, 200, 0, 0));
				else
					gc.setBackground(new Color(display, 128, 128, 128));
				gc.fillArc(x, y, 20, 20, 0, 360);
			} else {
				String name = node.getString("name") != null ? node.getString("name") : "No label";
				String id = node.getString("id");
				
				//Box
				if(node == selectedNode) {
					gc.setBackground(new Color(display, 255, 255, 255));
					gc.setForeground(new Color(display, 200, 0, 0));
				} else { 
					gc.setBackground(new Color(display, 255, 255, 255));
					gc.setForeground(new Color(display, 0, 0, 0));
				}
				gc.fillRectangle(x, y, boxWidth, boxHeight);
				
				//Header
				if(node == selectedNode) {
					gc.setBackground(new Color(display, 200, 0, 0));
					gc.setForeground(new Color(display, 255, 255, 255));
				} else {
					gc.setBackground(new Color(display, 128, 128, 128));
					gc.setForeground(new Color(display, 255, 255, 255));
				}
				gc.fillRectangle(x, y, boxWidth, 16);
				gc.setFont(headerFont);
				gc.drawString(typeLabels.get(type), x + 2, y + 1);
			
				//Label
				gc.setFont(labelFont);
				gc.setBackground(new Color(display, 255, 255, 255));
				gc.setForeground(new Color(display, 0, 0, 0));

				String remName = name + " ";
				int maxLineWidth = boxWidth - 3;
				int line = 16;
				while(remName.length() > 0 && line < boxHeight - 10) {
					String thisLine = null;
					if(getWidth(gc, remName) < maxLineWidth) {
						thisLine = remName;
						remName = "";
					} else {
						int cutAt = 0;
						int nextSpace = 0;
						do {
							cutAt = nextSpace;
							nextSpace = remName.indexOf(" ", cutAt + 1);
						} while(nextSpace > -1 && getWidth(gc, remName.substring(0, nextSpace)) < maxLineWidth);
						if(cutAt > 0) {
							thisLine = remName.substring(0, cutAt);
							remName = remName.substring(cutAt + 1);
						} else {
							thisLine = remName;
							while(getWidth(gc, thisLine) > maxLineWidth)
								thisLine = thisLine.substring(0, thisLine.length() - 1);
							remName = "";
						}
					}
					if(thisLine.length() > 0)
						gc.drawString(thisLine, x + 3, y + 2 + line);
					line += gc.getFontMetrics().getHeight();
				}				
			}
			
			//Connectors
			if(type.equals("interaction")) {
				DataList actions = node.getList("actions");
				for(int j = 0; j < actions.size(); j++) {
					DataMap destNode = getNode(actions.getObject(j).getString("nextnode"));
					paintConnector(gc, node, destNode, actions.getObject(j).getString("action"));
				}
			} else if(type.equals("condition")) {
				DataList actions = node.getList("actions");
				DataMap trueNode = getNode(node.getString("truenode"));
				paintConnector(gc, node, trueNode, "true");
				DataMap falseNode = getNode(node.getString("falsenode"));
				gc.setLineStyle(SWT.LINE_DOT);
				paintConnector(gc, node, falseNode, "false");
				gc.setLineStyle(SWT.LINE_SOLID);
			} else {
				DataMap destNode = getNode(node.getString("nextnode"));
				paintConnector(gc, node, destNode, null);
			}
		}		
	}

	protected void paintConnector(GC gc, DataMap start, DataMap end, String n)
	{
		if(start != null && end != null) {
			String startType = start.getString("type");
			String endType = end.getString("type");
			int startX = start.getNumber("position.x").intValue() + (startType.equals("join") ? 20 : boxWidth) + scrollHPos;
			int startY = start.getNumber("position.y").intValue() + (startType.equals("join") ? 10 : (boxHeight / 2)) + scrollVPos;
			int endX = end.getNumber("position.x").intValue() + scrollHPos;
			int endY = end.getNumber("position.y").intValue() + (endType.equals("join") ? 10 : (boxHeight / 2)) + scrollVPos;
			if(endX > startX + 10) {
				int midX = startX + 10;
				gc.drawLine(startX, startY, midX, startY);
				gc.drawLine(midX, startY, midX, endY);
				gc.drawLine(midX, endY, endX, endY);
				if(n != null)
					gc.drawString(n, midX + 3, endY - 20);
			} else {
				int midX1 = startX + 10;
				int midX2 = endX - 10;
				int midY = 0;
				if(endY > startY) {
					midY = startY - ((boxHeight / 2) + 20);
				} else {
					midY = startY + ((boxHeight / 2) + 20);
				}
				gc.drawLine(startX, startY, midX1, startY);
				gc.drawLine(midX1, startY, midX1, midY);
				gc.drawLine(midX1, midY, midX2, midY);
				gc.drawLine(midX2, midY, midX2, endY);
				gc.drawLine(midX2, endY, endX, endY);									
				if(n != null)
					gc.drawString(n, midX2 + 3, midY - 20);
			} 
			gc.drawLine(endX, endY, endX - 6, endY + 3);
			gc.drawLine(endX - 6, endY + 3, endX - 6, endY - 3);
			gc.drawLine(endX - 6, endY - 3, endX, endY);
		}
	}
	
	
	
	public void widgetSelected(SelectionEvent event) {
		Widget source = (Widget)event.getSource();
		Widget item = getEndWidget(source);
		NavigatorAction navAction = (NavigatorAction)item.getData();
		if(navAction.action.equals("space")) {
			String[] parts = navAction.name.split("-");
			int x = Integer.parseInt(parts[0]);
			for(int i = 0; i < _data.getList("nodes").size(); i++) {
				DataMap node = _data.getList("nodes").getObject(i);
				int nodeX = node.getNumber("position.x").intValue();
				if(nodeX > x)
					node.getObject("position").put("x", nodeX + (boxWidth * 2));
			}
			calcCanvasSize();
			canvas.redraw();
		}
		super.widgetSelected(event);
	}

	public void mouseDoubleClick(MouseEvent event) {
		
	}

	public void mouseDown(MouseEvent event) {
		int x = event.x - scrollHPos;
		int y = event.y - scrollVPos;
		DataMap node = getNode(x, y);
		if(event.button == 1) {
			if(node != null) {
				selectedNode = node;
				draggingNode = node;
				dragOffsetX = x - node.getNumber("position.x").intValue();
				dragOffsetY = y - node.getNumber("position.y").intValue();
				sendSelectionEvent(new NavigatorAction("select", node.getString("type"), node.getString("id")));
			} else {
				selectedNode = null;
				sendSelectionEvent(new NavigatorAction("select", "header", x + "-" + y));
			}
			canvas.redraw();
		} else if(event.button == 3) {
			if(node != null)
				sendMenuEvent(new NavigatorAction("select", node.getString("type"), node.getString("id")));
			else
				sendMenuEvent(new NavigatorAction("select", "header", x + "-" + y));
		}
	}

	public void mouseUp(MouseEvent event) {
		if(draggingNode != null) {
			DataMap precNode = getPrecedingNode(draggingNode.getString("id"));
			if(precNode != null) {
				int precY = precNode.getNumber("position.y").intValue(); 
				if(Math.abs(precY - draggingNode.getNumber("position.y").intValue()) < 15) {
					draggingNode.getObject("position").put("y", precY);
					canvas.redraw();
				}
			}
		}
		draggingNode = null;
	}
	
	public void mouseMove(MouseEvent event) {
		if(draggingNode != null) {
			draggingNode.getObject("position").put("x", event.x - dragOffsetX - scrollHPos);
			draggingNode.getObject("position").put("y", event.y - dragOffsetY - scrollVPos);
			canvas.redraw();
			manager.setDataChanged(true);
		}
	}

	protected void sendSelectionEvent(NavigatorAction action) {
		Event selEvent = new Event();
		canvas.setData(action);
		selEvent.widget = canvas;
		widgetSelected(new SelectionEvent(selEvent));
	}
	
	protected void sendMenuEvent(NavigatorAction action) {
		Event selEvent = new Event();
		canvas.setData(action);
		selEvent.widget = canvas;
		menuDetected(new MenuDetectEvent(selEvent));
	}

	
	protected DataMap getNode(int mx, int my) {
		for(int i = 0; i < _data.getList("nodes").size(); i++) {
			DataMap node = _data.getList("nodes").getObject(i);
			int x = node.getNumber("position.x").intValue();
			int y = node.getNumber("position.y").intValue();
			if(mx >= x && mx <= x + boxWidth && my >= y - 15 && my <= y + boxHeight)
				return node;
		}
		return null;
	}
	
	protected DataMap getNode(String id)
	{
		for(int i = 0; i < _data.getList("nodes").size(); i++) {
			DataMap node = _data.getList("nodes").getObject(i);
			if(node.getString("id").equals(id))
				return node;
		}
		return null;
	}

	protected DataMap getPrecedingNode(String id)
	{
		for(int i = 0; i < _data.getList("nodes").size(); i++) {
			DataMap node = _data.getList("nodes").getObject(i);
			if(node.getString("nextnode") != null) {
				if(node.getString("nextnode").equals(id))
					return node;
			} else if(node.getString("truenode") != null) {
				if(node.getString("truenode").equals(id))
					return node;
			} else if(node.getString("falsenode") != null) {
				if(node.getString("falsenode").equals(id))
					return node;
			} else if(node.getList("actions") != null) {
				DataList list = node.getList("actions");
				for(int j = 0; j < list.size(); j++) {
					DataMap action = list.getObject(j);
					if(action.getString("nextnode") != null && action.getString("nextnode").equals(id))
						return node;
				}
			}
		}
		return null;
	}
	
	protected long getWidth(GC gc, String s) {
		int len = 0;
		for(int i = 0; i < s.length(); i++)
			len += gc.getAdvanceWidth(s.charAt(i));
		return len;
	}
	
}
