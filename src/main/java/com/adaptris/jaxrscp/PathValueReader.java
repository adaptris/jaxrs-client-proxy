package com.adaptris.jaxrscp;

import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.ws.rs.core.PathSegment;

public class PathValueReader {

	public static String read(Object pairValue) {
		PathSegment segment = (PathSegment) pairValue;
		StringBuffer sb = new StringBuffer(segment.getPath()).append(";");
		Set<Entry<String, List<String>>> values = segment.getMatrixParameters().entrySet();
		for (Entry<String, List<String>> entry : values) {
			sb.append(entry.getKey()).append("=");
			sb.append(entry.getValue().stream().map(i -> i.toString()).collect(Collectors.joining("")));
			sb.append(";");
		}
		return sb.toString().replaceAll(";+$", "");
	}

	public static boolean isPathSegment(Object pairValue) {
		return PathSegment.class.isInstance(pairValue);
	}
}
