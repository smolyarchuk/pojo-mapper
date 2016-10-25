package org.pojomapper.copier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Source {

	private Object from;
	private Map<String, String> mapper = new HashMap<>();
	private List<Function<Object, Object>> converters = new ArrayList<>();
	private boolean rewrite = true;
	private boolean skipNulls;

	public Source(Object from) {
		this.from = from;
	}

	public Object from() {
		return from;
	}

	public Map<String, String> mapper() {
		return mapper;
	}

	public List<Function<Object, Object>> converters() {
		return converters;
	}

	public boolean rewrite() {
		return rewrite;
	}

	public void rewrite(boolean rewrite) {
		this.rewrite = rewrite;
	}

	public boolean skipNulls() {
		return skipNulls;
	}

	public void skipNulls(boolean skipNulls) {
		this.skipNulls = skipNulls;
	}
}
