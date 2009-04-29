package org.encog.workbench.frames.network;

import java.awt.Point;
import java.awt.Polygon;

import org.encog.neural.networks.layers.Layer;
import org.encog.neural.networks.synapse.Synapse;
import org.encog.workbench.frames.network.NetworkDiagram.Side;

public class CalculateArrow {
	
	private  Point from;
	private  Point to;
	private Side fromSide;
	private Side toSide;
	
	public CalculateArrow(Synapse synapse, boolean moveBack)
	{

		
		int xOffset = 0;
		int yOffset = 0;
		
		
		if( (synapse.getToLayer().getX()>=synapse.getFromLayer().getX()) && 
				(synapse.getToLayer().getX()<=(synapse.getFromLayer().getX()+DrawLayer.LAYER_WIDTH*1.5)) ||
		
				(synapse.getFromLayer().getX()>=synapse.getToLayer().getX()) && 
				(synapse.getFromLayer().getX()<=(synapse.getToLayer().getX()+DrawLayer.LAYER_WIDTH*1.5))
		)
		{
			if(synapse.getToLayer().getY()>synapse.getFromLayer().getY())
			{
				fromSide = Side.Bottom;
				toSide = Side.Top;
			}
			else
			{
				fromSide = Side.Top;
				toSide = Side.Bottom;
			}
		}
		else
		{
			if(synapse.getToLayer().getX()>synapse.getFromLayer().getX())
			{
				fromSide = Side.Right;
				toSide = Side.Left;
				if(isBackConnected(synapse))
					yOffset = (DrawLayer.LAYER_HEIGHT/4);
			}
			else
			{
				fromSide = Side.Left;
				toSide = Side.Right;
				yOffset = -(DrawLayer.LAYER_HEIGHT/4);
			}
		}
		
		from = findSide(fromSide,synapse.getFromLayer());
		to = findSide(toSide,synapse.getToLayer());
		
		// apply offsets
		from.setLocation(from.getX()+xOffset,from.getY()+yOffset);
		to.setLocation(to.getX()+xOffset,to.getY()+yOffset);	
		
		if(moveBack)
			to = moveArrowBack(toSide, to);
	}
	
	/**
	 * Move the "to" part of an arrow back so the arrowhead is outside the layer.
	 * @param side
	 * @param p
	 * @return
	 */
	private Point moveArrowBack(Side side,Point p)
	{
		switch(side)
		{
		case Top:
			return new Point((int)p.getX(),(int)p.getY()-NetworkDiagram.ARROWHEAD_WIDTH);
		case Bottom:
			return new Point((int)p.getX(),(int)p.getY()+NetworkDiagram.ARROWHEAD_WIDTH);
		case Left:
			return new Point((int)p.getX()-NetworkDiagram.ARROWHEAD_WIDTH,(int)p.getY());
		case Right:
			return new Point((int)p.getX()+NetworkDiagram.ARROWHEAD_WIDTH,(int)p.getY());
		}
		return null;
	}
	
	private Point findSide(Side side,Layer layer)
	{
		switch(side)
		{
			case Top:
				return new Point(layer.getX()+(DrawLayer.LAYER_WIDTH/2),layer.getY());
			case Bottom:
				return new Point(layer.getX()+(DrawLayer.LAYER_WIDTH/2),layer.getY()+DrawLayer.LAYER_HEIGHT);
			case Left:
				return new Point(layer.getX(),layer.getY()+(DrawLayer.LAYER_HEIGHT/2));
			case Right:
				return new Point(layer.getX()+DrawLayer.LAYER_WIDTH,layer.getY()+(DrawLayer.LAYER_HEIGHT/2));
		}
		
		return null;
	}
	
	/**
	 * If the passed in synapse connects layers a and b, is there another synapse
	 * that also connects b and a?
	 * @param synapse
	 * @return
	 */
	public boolean isBackConnected(Synapse synapseA)
	{
		for(Synapse synapseB: synapseA.getToLayer().getNext())
		{
			if( synapseB.getToLayer() == synapseA.getFromLayer())
				return true;
		}
		return false;
	}

	public Point getFrom() {
		return from;
	}

	public Point getTo() {
		return to;
	}

	public Side getFromSide() {
		return fromSide;
	}

	public Side getToSide() {
		return toSide;
	}
	
	public Polygon obtainPologygon()
	{
		Polygon result = new Polygon();
		
		if( this.fromSide==Side.Left || this.fromSide==Side.Right)
		{
			result.addPoint((int)this.from.getX(), (int)from.getY()-10);
			result.addPoint((int)this.from.getX(), (int)from.getY()+10);
			result.addPoint((int)this.to.getX(), (int)to.getY()+10);
			result.addPoint((int)this.to.getX(), (int)to.getY()-10);
		}
		else
		{
			result.addPoint((int)this.from.getX()-10, (int)from.getY());
			result.addPoint((int)this.from.getX()+10, (int)from.getY());
			result.addPoint((int)this.to.getX()+10, (int)to.getY());
			result.addPoint((int)this.to.getX()-10, (int)to.getY());
		}
		
		
		return result;
	}
	
	
}
