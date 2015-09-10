package org.pi4.locutil.trace.positionfilter;

import org.pi4.locutil.GeoPosition;

public class PositionFilterAllowAll implements PositionFilter {

	public boolean contains(GeoPosition pos) {
		return true;
	}

	public boolean isEmpty() {
		return true;
	}

}
