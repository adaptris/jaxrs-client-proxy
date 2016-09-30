package com.adaptris.jaxrscp;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.core.PathSegment;

public class PathValueReader {

	public static String read(Object pairValue) {
		PathSegment segment = (PathSegment) pairValue;
		StringBuffer sb = new StringBuffer(segment.getPath()).append(";");
		Set<Entry<String, List<String>>> matrixParameters = segment.getMatrixParameters().entrySet();
		for (Entry<String, List<String>> parameter : matrixParameters) {
			List<String> parameterValues = parameter.getValue();
			for (String value : parameterValues) {
				sb.append(parameter.getKey())
				.append("=")
				.append(value)
				.append(";");
			}
		}
		return removeLastSpecialChar(sb);
	}

	private static String removeLastSpecialChar(StringBuffer sb) {
		return sb.substring(0, sb.length() - 1);
	}

	public static boolean isPathSegment(Object pairValue) {
		return PathSegment.class.isInstance(pairValue);
	}
}
